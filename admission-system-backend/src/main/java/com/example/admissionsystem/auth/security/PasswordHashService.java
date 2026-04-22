package com.example.admissionsystem.auth.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordHashService {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final String PREFIX = "pbkdf2";

    public String hash(String rawPassword) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);
            byte[] hash = hash(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            return PREFIX + "$" + ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash password", ex);
        }
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        if (encodedPassword.startsWith("{noop}")) {
            return MessageDigest.isEqual(
                    rawPassword.getBytes(StandardCharsets.UTF_8),
                    encodedPassword.substring(6).getBytes(StandardCharsets.UTF_8)
            );
        }

        String[] parts = encodedPassword.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            throw new BadCredentialsException("Unsupported password hash format");
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = hash(rawPassword.toCharArray(), salt, iterations, expected.length * 8);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception ex) {
            throw new BadCredentialsException("Password verification failed", ex);
        }
    }

    private byte[] hash(char[] rawPassword, byte[] salt, int iterations, int keyLength) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(rawPassword, salt, iterations, keyLength);
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
    }
}
