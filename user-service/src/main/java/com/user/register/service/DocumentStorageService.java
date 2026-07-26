package com.user.register.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:cyberlearnix}")
    private String folder;

    public DocumentStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String store(UUID userId, String fieldName, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String original = file.getOriginalFilename();
        String safeName = (original != null && !original.isBlank())
                ? original.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
        
        // Use a unique name including fieldName, userId, and timestamp to avoid conflicts in that folder
        String publicId = userId.toString() + "_" + fieldName + "_" + System.currentTimeMillis() + "_" + safeName;
        // Strip file extension from publicId since Cloudinary handles extensions automatically
        if (publicId.contains(".")) {
            publicId = publicId.substring(0, publicId.lastIndexOf("."));
        }

        Map<?, ?> options = ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId,
                "resource_type", "auto"
        );

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) uploadResult.get("secure_url");
    }
}
