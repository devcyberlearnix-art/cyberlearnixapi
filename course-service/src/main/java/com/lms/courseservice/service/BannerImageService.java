package com.lms.courseservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lms.courseservice.dto.BannerImageUploadRequest;
import com.lms.courseservice.dto.ImageValidationResult;
import com.lms.courseservice.exception.BannerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerImageService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:cyberlearnix}")
    private String cloudinaryFolder;

    private static final long MAX_FILE_SIZE = 1 * 1024 * 1024; // 1MB
    private static final int RECOMMENDED_WIDTH = 1920;
    private static final int RECOMMENDED_HEIGHT = 1080;
    private static final double ASPECT_RATIO_TOLERANCE = 0.1;
    private static final List<String> ALLOWED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "webp");

    public ImageValidationResult validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            return ImageValidationResult.builder()
                    .valid(false)
                    .errorMessage("File is empty")
                    .recommendedAction("Please select a valid image file")
                    .build();
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ImageValidationResult.builder()
                    .valid(false)
                    .errorMessage("File size exceeds 1MB limit")
                    .recommendedAction("Compress the image or choose a smaller file")
                    .build();
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            return ImageValidationResult.builder()
                    .valid(false)
                    .errorMessage("Invalid file format. Only JPG, PNG, and WebP are allowed")
                    .recommendedAction("Convert the image to JPG, PNG, or WebP format")
                    .build();
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return ImageValidationResult.builder()
                        .valid(false)
                        .errorMessage("File is not a valid image")
                        .recommendedAction("Please upload a valid image file")
                        .build();
            }

            int width = image.getWidth();
            int height = image.getHeight();
            double aspectRatio = (double) width / height;
            double targetAspectRatio = (double) RECOMMENDED_WIDTH / RECOMMENDED_HEIGHT;

            if (Math.abs(aspectRatio - targetAspectRatio) > ASPECT_RATIO_TOLERANCE) {
                return ImageValidationResult.builder()
                        .valid(false)
                        .errorMessage("Image aspect ratio is not 16:9")
                        .recommendedAction("Resize image to 16:9 aspect ratio (recommended: 1920x1080)")
                        .build();
            }

            if (width < 800 || height < 450) {
                return ImageValidationResult.builder()
                        .valid(false)
                        .errorMessage("Image resolution is too low")
                        .recommendedAction("Use higher resolution image (minimum 800x450, recommended 1920x1080)")
                        .build();
            }

            return ImageValidationResult.builder()
                    .valid(true)
                    .errorMessage(null)
                    .recommendedAction(null)
                    .build();

        } catch (IOException e) {
            log.error("Error reading image file", e);
            return ImageValidationResult.builder()
                    .valid(false)
                    .errorMessage("Error processing image file")
                    .recommendedAction("Try uploading the image again")
                    .build();
        }
    }

    public BannerImageUploadRequest uploadImage(MultipartFile file, String imageType) {
        ImageValidationResult validation = validateImage(file);
        if (!validation.isValid()) {
            throw new BannerException(validation.getErrorMessage() + ". " + validation.getRecommendedAction());
        }

        try {
            String publicId = "banners/" + System.currentTimeMillis() + "_" + imageType;

            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("public_id", publicId);
            uploadOptions.put("folder", cloudinaryFolder);
            uploadOptions.put("resource_type", "image");
            uploadOptions.put("quality", "auto");
            uploadOptions.put("fetch_format", "auto");

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            String imageUrl = (String) uploadResult.get("secure_url");
            String format = (String) uploadResult.get("format");
            int width = (Integer) uploadResult.get("width");
            int height = (Integer) uploadResult.get("height");
            long bytes = ((Number) uploadResult.get("bytes")).longValue();

            return BannerImageUploadRequest.builder()
                    .desktopImageUrl(imageUrl)
                    .mobileImageUrl(imageUrl)
                    .imageWidth(width)
                    .imageHeight(height)
                    .imageFormat(format)
                    .imageSize(bytes)
                    .build();

        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary", e);
            throw new BannerException("Failed to upload image: " + e.getMessage());
        } catch (Exception e) {
            log.error("Cloudinary upload error - CloudName: {}, API Key: {}, Folder: {}",
                cloudinary.config.cloudName, cloudinary.config.apiKey, cloudinaryFolder, e);
            throw new BannerException("Failed to upload image to cloud storage: " + e.getMessage());
        }
    }

    public String generateMobileImageUrl(String desktopImageUrl) {
        if (desktopImageUrl == null || !desktopImageUrl.contains("cloudinary")) {
            return desktopImageUrl;
        }

        try {
            return desktopImageUrl.replace("/upload/", "/upload/c_scale,w_768,q_auto,f_auto/");
        } catch (Exception e) {
            log.error("Error generating mobile image URL", e);
            return desktopImageUrl;
        }
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary")) {
            return;
        }

        try {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Successfully deleted image from Cloudinary: {}", publicId);
            }
        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary: {}", imageUrl, e);
        }
    }

    private boolean hasValidExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return ALLOWED_FORMATS.stream().anyMatch(lowerCaseFilename::endsWith);
    }

    private String extractPublicId(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/");
            String filename = parts[parts.length - 1];
            return filename.substring(0, filename.lastIndexOf('.'));
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", imageUrl, e);
            return null;
        }
    }
}
