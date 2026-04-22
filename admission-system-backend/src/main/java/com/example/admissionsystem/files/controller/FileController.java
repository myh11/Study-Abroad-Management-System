package com.example.admissionsystem.files.controller;

import com.example.admissionsystem.common.response.Result;
import com.example.admissionsystem.files.service.FileService;
import com.example.admissionsystem.files.vo.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/transcripts")
    public Result<FileUploadResponse> uploadTranscript(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.uploadTranscript(file));
    }

    @PostMapping("/attachments")
    public Result<FileUploadResponse> uploadAttachment(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.uploadAttachment(file));
    }

    @GetMapping("/{fileId}/preview")
    public ResponseEntity<Resource> preview(@PathVariable Long fileId) {
        return fileService.preview(fileId);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        return fileService.download(fileId);
    }
}
