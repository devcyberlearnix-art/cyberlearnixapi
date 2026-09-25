package com.user.register.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private final GoogleDriveService googleDriveService;

    public DocumentStorageService(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    public String store(UUID userId, String fieldName, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String original = file.getOriginalFilename();
        String safeName = (original != null && !original.isBlank())
                ? original.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
        
        String uploadFileName = userId.toString() + "_" + fieldName + "_" + System.currentTimeMillis() + "_" + safeName;
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        return googleDriveService.uploadBytes(file.getBytes(), uploadFileName, contentType);
    }
}
