package com.example.admissionsystem.auth.security;

import com.example.admissionsystem.auth.model.UserRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-seconds}")
    private long expirationSeconds;

    private final ObjectMapper objectMapper;

    public String generateToken(AuthUserPrincipal principal) {
        try {
            long now = Instant.now().getEpochSecond();
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", principal.getUsername());
            payload.put("uid", principal.getUserId());
            payload.put("role", principal.getRole().name());
            payload.put("schoolCode", principal.getSchoolCode());
            payload.put("iat", now);
            payload.put("exp", now + expirationSeconds);

            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String content = encodedHeader + "." + encodedPayload;
            return content + "." + sign(content);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate JWT token", ex);
        }
    }

    public TokenPayload parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BadCredentialsException("Invalid token format");
            }

            String content = parts[0] + "." + parts[1];
            String expectedSignature = sign(content);
            if (!expectedSignature.equals(parts[2])) {
                throw new BadCredentialsException("Invalid token signature");
            }

            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), MAP_TYPE);
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                throw new BadCredentialsException("Token expired");
            }

            return new TokenPayload(
                    ((Number) payload.get("uid")).longValue(),
                    String.valueOf(payload.get("sub")),
                    UserRole.valueOf(String.valueOf(payload.get("role"))),
                    payload.get("schoolCode") == null ? null : String.valueOf(payload.get("schoolCode"))
            );
        } catch (BadCredentialsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadCredentialsException("Token validation failed", ex);
        }
    }

    private String encodeJson(Map<String, Object> content) throws Exception {
        return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(content));
    }

    private String sign(String content) throws Exception {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT secret must not be empty");
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    public record TokenPayload(
            Long userId,
            String username,
            UserRole role,
            String schoolCode
    ) {
    }
}
