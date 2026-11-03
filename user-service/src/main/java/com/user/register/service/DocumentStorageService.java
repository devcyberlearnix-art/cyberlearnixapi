package com.user.register.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class DocumentStorageService {

    private final GoogleDriveService googleDriveService;
    private final Path uploadRoot;

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "jpg", "jpeg", "png", "webp", "ppt", "pptx", "mp4"
    );

    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
            "exe", "sh", "bat", "cmd", "js", "jsp", "php", "html", "htm", "vbs", "ps1", "jar", "py", "pl", "com", "scr", "dll", "bin", "msi"
    );

    public DocumentStorageService(
            GoogleDriveService googleDriveService,
            @Value("${app.upload.base-dir:uploads/instructor-applications}") String uploadBaseDir) {
        this.googleDriveService = googleDriveService;
        this.uploadRoot = Paths.get(uploadBaseDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            log.error("Failed to initialize document upload directory: {}", e.getMessage());
        }
    }

    public String store(UUID userId, String fieldName, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document");
        
        // Anti-Path Traversal Check
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path traversal detected in filename");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        validateFileContent(file, extension);
        inspectDeepContent(file, extension);

        if (extension.equals("docx") || extension.equals("pptx")) {
            checkArchiveBomb(file);
        }

        String safeBasename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String uniqueFileName = userId + "_" + fieldName + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;

        Path userDir = this.uploadRoot.resolve(userId.toString());
        Files.createDirectories(userDir);

        Path targetLocation = userDir.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        log.info("Securely stored sensitive document {} on disk at: {}", fieldName, targetLocation);

        // Protected local identifier path ensuring zero public internet exposure
        return "local:" + userId + "/" + uniqueFileName;
    }

    public Resource loadAsResource(String pathStr) {
        try {
            if (pathStr == null || pathStr.isBlank()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document path is empty");
            }

            // 1. Local private storage
            if (pathStr.startsWith("local:")) {
                String subPath = pathStr.substring("local:".length());
                Path filePath = this.uploadRoot.resolve(subPath).normalize();
                if (Files.exists(filePath) && filePath.startsWith(this.uploadRoot)) {
                    return new UrlResource(filePath.toUri());
                }
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file not found on disk");
            }

            // 2. Google Drive fallback for existing uploaded records
            if (pathStr.contains("drive.google.com") && googleDriveService != null) {
                String fileId = googleDriveService.extractFileId(pathStr);
                if (fileId != null) {
                    try {
                        byte[] bytes = googleDriveService.downloadFileBytes(fileId);
                        String mime = googleDriveService.getFileMimeType(fileId);
                        String ext = (mime != null && mime.contains("pdf")) ? "pdf" : "jpg";
                        return new org.springframework.core.io.ByteArrayResource(bytes) {
                            @Override
                            public String getFilename() {
                                return "document." + ext;
                            }
                        };
                    } catch (Exception ex) {
                        log.warn("Failed to download bytes from Google Drive for {}: {}", fileId, ex.getMessage());
                    }
                }
            }

            // 3. Direct filesystem path fallback
            try {
                Path filePath = Paths.get(pathStr).toAbsolutePath().normalize();
                if (Files.exists(filePath)) {
                    return new UrlResource(filePath.toUri());
                }
            } catch (Exception ignored) {}

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file could not be located");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error loading document: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds maximum allowed limit of 25MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file must have a valid filename");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Executable or script files are strictly forbidden: ." + extension);
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File format ." + extension + " is not supported. Allowed formats: PDF, DOC, DOCX, JPG, PNG, WEBP");
        }
    }

    private void validateFileContent(MultipartFile file, String extension) throws IOException {
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is corrupted or too small to be valid.");
            }
        }

        // Anti-executable signatures check:
        // MZ (Windows PE Executable)
        if (header[0] == 0x4D && header[1] == 0x5A) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Executable binary files (.exe / .dll) are strictly forbidden.");
        }
        // ELF (Linux executable)
        if (header[0] == 0x7F && header[1] == 0x45 && header[2] == 0x4C && header[3] == 0x46) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Linux binary executables are strictly forbidden.");
        }
        // Script header (#!)
        if (header[0] == 0x23 && header[1] == 0x21) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Script files (#!/bin/sh) are strictly forbidden.");
        }
        // Java bytecode (CAFEBABE)
        if ((header[0] & 0xFF) == 0xCA && (header[1] & 0xFF) == 0xFE && (header[2] & 0xFF) == 0xBA && (header[3] & 0xFF) == 0xBE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Java bytecode class files are strictly forbidden.");
        }

        switch (extension) {
            case "pdf" -> {
                if (header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46 || header[4] != 0x2D) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file content: Expected a valid PDF document header (%PDF-).");
                }
            }
            case "jpg", "jpeg" -> {
                if ((header[0] & 0xFF) != 0xFF || (header[1] & 0xFF) != 0xD8 || (header[2] & 0xFF) != 0xFF) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file content: Expected a valid JPEG image header.");
                }
            }
            case "png" -> {
                if ((header[0] & 0xFF) != 0x89 || header[1] != 0x50 || header[2] != 0x4E || header[3] != 0x47) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file content: Expected a valid PNG image header.");
                }
            }
            case "doc", "docx", "ppt", "pptx" -> {
                boolean isZip = header[0] == 0x50 && header[1] == 0x4B;
                boolean isOle = (header[0] & 0xFF) == 0xD0 && (header[1] & 0xFF) == 0xCF;
                if (!isZip && !isOle) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file content: Expected a valid Microsoft Office document structure.");
                }
            }
            case "webp" -> {
                if (header[0] != 'R' || header[1] != 'I' || header[2] != 'F' || header[3] != 'F') {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file content: Expected a valid WebP image header.");
                }
            }
        }
    }

    private void inspectDeepContent(MultipartFile file, String extension) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        try (InputStream is = file.getInputStream()) {
            bytesRead = is.read(buffer);
        }
        if (bytesRead > 0) {
            String contentSample = new String(buffer, 0, bytesRead, StandardCharsets.ISO_8859_1).toLowerCase();
            List<String> forbiddenPatterns = List.of(
                    "<?php",
                    "<script",
                    "javascript:",
                    "vbscript:",
                    "eval(",
                    "base64_decode(",
                    "system(",
                    "passthru(",
                    "shell_exec(",
                    "powershell",
                    "cmd.exe",
                    "/bin/sh",
                    "/bin/bash"
            );
            for (String pattern : forbiddenPatterns) {
                if (contentSample.contains(pattern)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malicious code or script execution pattern detected in file content: " + pattern);
                }
            }
        }
    }

    private void checkArchiveBomb(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            int entryCount = 0;
            long totalUncompressedBytes = 0;
            byte[] buffer = new byte[8192];

            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > 1000) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archive bomb detected: Exceeded maximum allowed entry count.");
                }

                String entryName = entry.getName();
                if (entryName.contains("..") || entryName.startsWith("/") || entryName.startsWith("\\")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malicious path traversal detected inside archive.");
                }

                int bytesRead;
                while ((bytesRead = zis.read(buffer)) != -1) {
                    totalUncompressedBytes += bytesRead;
                    if (totalUncompressedBytes > 100 * 1024 * 1024) { // 100MB limit
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archive bomb detected: Uncompressed size exceeds 100MB.");
                    }
                }
                zis.closeEntry();
            }

            long compressedSize = Math.max(1, file.getSize());
            double compressionRatio = (double) totalUncompressedBytes / (double) compressedSize;
            if (compressionRatio > 100.0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archive bomb detected: Abnormal compression ratio (" + String.format("%.1f", compressionRatio) + ":1).");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception ex) {
            log.warn("Archive inspection exception: {}", ex.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "bin";
        }
        return filename.substring(dotIndex + 1);
    }
}
