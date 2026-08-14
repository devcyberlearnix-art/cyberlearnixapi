package com.lms.courseservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final String folder;

    public CloudinaryService(Cloudinary cloudinary,
                             @Qualifier("cloudinaryFolder") String folder) {
        this.cloudinary = cloudinary;
        this.folder = folder;
    }

    /**
     * Uploads a video file to Cloudinary under "{folder}/course-previews/" subfolder.
     *
     * @param file the multipart video file
     * @return the secure HTTPS URL of the uploaded video
     */
    @SuppressWarnings("unchecked")
    public String uploadVideo(MultipartFile file) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder",        folder + "/course-previews",
                            "overwrite",     true
                    )
            );
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload video to Cloudinary: " + e.getMessage(), e);
        }
    }
}

