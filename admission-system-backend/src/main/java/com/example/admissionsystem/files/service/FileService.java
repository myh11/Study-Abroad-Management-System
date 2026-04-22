package com.example.admissionsystem.files.service;

import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import com.example.admissionsystem.files.vo.FileUploadResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
@RequiredArgsConstructor
public class FileService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.file.base-dir}")
    private String baseDir;

    private Path transcriptDir;
    private Path attachmentDir;

    @PostConstruct
    public void initStorage() {
        try {
            Path root = Path.of(baseDir).toAbsolutePath().normalize();
            transcriptDir = root.resolve("transcripts");
            attachmentDir = root.resolve("attachments");
            Files.createDirectories(transcriptDir);
            Files.createDirectories(attachmentDir);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to initialize local file storage", ex);
        }
    }

    public FileUploadResponse uploadTranscript(MultipartFile file) {
        AuthUserPrincipal currentUser = requireCurrentUser();
        ensureUploadAllowed(currentUser);
        return store(file, "transcript", transcriptDir);
    }

    public FileUploadResponse uploadAttachment(MultipartFile file) {
        AuthUserPrincipal currentUser = requireCurrentUser();
        ensureUploadAllowed(currentUser);
        return store(file, "attachment", attachmentDir);
    }

    public ResponseEntity<Resource> preview(Long fileId) {
        FileRecord storedFile = getStoredFile(fileId);
        return buildFileResponse(storedFile, false);
    }

    public ResponseEntity<Resource> download(Long fileId) {
        FileRecord storedFile = getStoredFile(fileId);
        return buildFileResponse(storedFile, true);
    }

    private FileUploadResponse store(MultipartFile file, String businessType, Path targetDir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file must not be empty");
        }
        AuthUserPrincipal currentUser = requireCurrentUser();

        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "unknown");
        String fileKey = UUID.randomUUID().toString().replace("-", "");
        String safeFileName = fileKey + "_" + originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
        Path targetPath = targetDir.resolve(safeFileName).normalize();

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to store file", ex);
        }

        Long fileId = insertFileRecord(fileKey, originalName, targetPath, file.getContentType(), file.getSize(), businessType, currentUser.getUserId());

        return FileUploadResponse.builder()
                .fileId(fileId)
                .originalName(originalName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();
    }

    private FileRecord getStoredFile(Long fileId) {
        FileRecord storedFile = jdbcTemplate.query("""
                SELECT id, file_key, original_name, storage_path, file_type, file_size, business_type, uploaded_by
                FROM files
                WHERE id = ?
                """, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new FileRecord(
                    rs.getLong("id"),
                    rs.getString("file_key"),
                    rs.getString("original_name"),
                    rs.getString("storage_path"),
                    rs.getString("file_type"),
                    rs.getLong("file_size"),
                    rs.getString("business_type"),
                    rs.getObject("uploaded_by") == null ? null : rs.getLong("uploaded_by")
            );
        }, fileId);
        if (storedFile == null) {
            throw new IllegalArgumentException("file not found: " + fileId);
        }
        ensureFileReadable(storedFile);
        return storedFile;
    }

    private ResponseEntity<Resource> buildFileResponse(FileRecord storedFile, boolean attachment) {
        Path path = Path.of(storedFile.storagePath()).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("file content not found: " + storedFile.id());
        }
        Resource resource = new FileSystemResource(path);
        MediaType mediaType = resolveMediaType(storedFile.fileType());
        ContentDisposition disposition = (attachment
                ? ContentDisposition.attachment()
                : ContentDisposition.inline())
                .filename(storedFile.originalName())
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(storedFile.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    private Long insertFileRecord(
            String fileKey,
            String originalName,
            Path targetPath,
            String fileType,
            long fileSize,
            String businessType,
            Long uploadedBy
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO files(file_key, original_name, storage_path, file_type, file_size, business_type, uploaded_by)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, fileKey);
            ps.setString(2, originalName);
            ps.setString(3, targetPath.toString());
            ps.setString(4, fileType);
            ps.setLong(5, fileSize);
            ps.setString(6, businessType);
            ps.setLong(7, uploadedBy);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to persist file metadata");
        }
        return key.longValue();
    }

    private void ensureUploadAllowed(AuthUserPrincipal principal) {
        if (principal.getRole() != UserRole.AGENT && principal.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only AGENT or ADMIN can upload files");
        }
    }

    private void ensureFileReadable(FileRecord fileRecord) {
        AuthUserPrincipal principal = requireCurrentUser();
        if (principal.getRole() == UserRole.ADMIN) {
            return;
        }
        if (Objects.equals(fileRecord.uploadedBy(), principal.getUserId())) {
            return;
        }

        AccessibleApplication application = jdbcTemplate.query("""
                SELECT a.id, a.created_by_agent_id, a.target_school_code
                FROM applications a
                JOIN transcripts t ON t.id = a.transcript_id
                WHERE t.file_id = ?
                ORDER BY a.id DESC
                LIMIT 1
                """, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new AccessibleApplication(
                    rs.getLong("id"),
                    rs.getLong("created_by_agent_id"),
                    rs.getString("target_school_code")
            );
        }, fileRecord.id());

        if (application == null) {
            throw new IllegalArgumentException("No permission to access this file");
        }

        if (principal.getRole() == UserRole.AGENT && Objects.equals(application.createdByAgentId(), principal.getUserId())) {
            return;
        }
        if (principal.getRole() == UserRole.DOMESTIC_REVIEWER) {
            return;
        }
        if (principal.getRole() == UserRole.SCHOOL_REVIEWER && Objects.equals(application.targetSchoolCode(), principal.getSchoolCode())) {
            return;
        }
        throw new IllegalArgumentException("No permission to access this file");
    }

    private AuthUserPrincipal requireCurrentUser() {
        Object principal = getContext().getAuthentication() == null ? null : getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal) {
            return authUserPrincipal;
        }
        throw new IllegalArgumentException("Current user not found");
    }

    private MediaType resolveMediaType(String fileType) {
        if (fileType == null || fileType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(fileType);
        } catch (IllegalArgumentException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private record FileRecord(
            Long id,
            String fileKey,
            String originalName,
            String storagePath,
            String fileType,
            Long fileSize,
            String businessType,
            Long uploadedBy
    ) {
    }

    private record AccessibleApplication(
            Long id,
            Long createdByAgentId,
            String targetSchoolCode
    ) {
    }
}
