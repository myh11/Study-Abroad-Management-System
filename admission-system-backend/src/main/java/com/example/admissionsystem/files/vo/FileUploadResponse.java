package com.example.admissionsystem.files.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private Long fileId;
    private String originalName;
    private String fileType;
    private Long fileSize;
}
