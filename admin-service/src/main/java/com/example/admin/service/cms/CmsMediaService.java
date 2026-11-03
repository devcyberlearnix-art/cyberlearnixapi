package com.example.admin.service.cms;

import com.example.admin.entity.cms.CmsMediaAsset;
import com.example.admin.repository.cms.CmsMediaAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsMediaService {

    private final CmsMediaAssetRepository repository;
    private static final String UPLOAD_DIR = "uploads/cms/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml"
    );

    @Transactional
    public CmsMediaAsset uploadMedia(MultipartFile file, String altText, String uploadedBy) {
        if (file == null || file.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "File is required and cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "File size exceeds 5MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Only image formats (JPEG, PNG, WebP, GIF, SVG) are allowed");
        }

        try {
            Path root = Paths.get(UPLOAD_DIR);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "asset";
            String safeName = UUID.randomUUID() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = root.resolve(safeName);

            Files.copy(file.getInputStream(), target);
            String fileUrl = "/api/v1/public/media/" + safeName;

            CmsMediaAsset asset = CmsMediaAsset.builder()
                    .fileName(originalName)
                    .fileUrl(fileUrl)
                    .mimeType(contentType)
                    .fileSize(file.getSize())
                    .altText(altText != null ? altText : originalName)
                    .uploadedBy(uploadedBy != null ? uploadedBy : "ADMIN")
                    .build();

            return repository.save(asset);
        } catch (IOException e) {
            log.error("Failed to store media file: {}", e.getMessage(), e);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
        }
    }

    public Page<CmsMediaAsset> getMediaLibrary(int page, int size) {
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @Transactional
    public void deleteMedia(UUID id) {
        repository.findById(id).ifPresent(asset -> {
            try {
                String fileName = asset.getFileUrl().substring(asset.getFileUrl().lastIndexOf("/") + 1);
                Path target = Paths.get(UPLOAD_DIR).resolve(fileName);
                Files.deleteIfExists(target);
            } catch (Exception e) {
                log.warn("Failed to delete local media file: {}", e.getMessage());
            }
            repository.delete(asset);
        });
    }
}

