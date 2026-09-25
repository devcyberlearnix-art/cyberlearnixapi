package com.example.instructorservice.service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.example.instructorservice.config.GoogleDriveProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveService {

    private final Drive driveService;
    private final GoogleDriveProperties driveProperties;

    public String uploadFile(MultipartFile file) {
        try {
            return uploadBytes(
                file.getBytes(),
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file",
                file.getContentType() != null ? file.getContentType() : "application/octet-stream"
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Google Drive: " + e.getMessage(), e);
        }
    }

    public String uploadBytes(byte[] fileBytes, String fileName, String contentType) {
        log.info("Uploading '{}' ({} bytes, type: {}) to Google Drive folder: {}",
                fileName, fileBytes.length, contentType, driveProperties.getFolderId());

        try {
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(driveProperties.getFolderId()));

            InputStreamContent mediaContent = new InputStreamContent(
                    contentType,
                    new ByteArrayInputStream(fileBytes)
            );
            mediaContent.setLength(fileBytes.length);

            Drive.Files.Create createRequest = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id, name, mimeType, size");

            MediaHttpUploader uploader = createRequest.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false);
            uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);

            File uploadedFile = createRequest.execute();
            String fileId = uploadedFile.getId();
            log.info("✅ File uploaded to Google Drive — id={}, name={}", fileId, uploadedFile.getName());

            setPublicReadPermission(fileId);

            return getFileUrl(fileId);
        } catch (IOException e) {
            log.error("Google Drive upload failed for '{}': {}", fileName, e.getMessage(), e);
            throw new RuntimeException("Google Drive upload failed: " + e.getMessage(), e);
        }
    }

    private void setPublicReadPermission(String fileId) {
        try {
            Permission permission = new Permission();
            permission.setType("anyone");
            permission.setRole("reader");

            driveService.permissions().create(fileId, permission).execute();
        } catch (IOException e) {
            log.warn("Failed to set public permission for Google Drive file {}: {}", fileId, e.getMessage());
        }
    }

    public String getFileUrl(String fileId) {
        return "https://drive.google.com/uc?id=" + fileId;
    }
}
