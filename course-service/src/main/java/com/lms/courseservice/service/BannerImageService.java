package com.lms.courseservice.service;

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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerImageService {

    private final GoogleDriveService googleDriveService;

    @Value("${google.folder-id}")
    private String googleDriveFolderId;

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
            String fileName = "banners/" + System.currentTimeMillis() + "_" + imageType + ".jpg";
            String imageUrl = googleDriveService.uploadBytes(file.getBytes(), fileName, "image/jpeg");

            BufferedImage image = ImageIO.read(file.getInputStream());
            int width = image.getWidth();
            int height = image.getHeight();
            long bytes = file.getSize();

            return BannerImageUploadRequest.builder()
                    .desktopImageUrl(imageUrl)
                    .mobileImageUrl(imageUrl)
                    .imageWidth(width)
                    .imageHeight(height)
                    .imageFormat("jpg")
                    .imageSize(bytes)
                    .build();

        } catch (IOException e) {
            log.error("Error uploading image to Google Drive", e);
            throw new BannerException("Failed to upload image: " + e.getMessage());
        } catch (Exception e) {
            log.error("Google Drive upload error - Folder ID: {}", googleDriveFolderId, e);
            throw new BannerException("Failed to upload image to cloud storage: " + e.getMessage());
        }
    }

    public String generateMobileImageUrl(String desktopImageUrl) {
        // Google Drive doesn't support dynamic image transformation like Cloudinary
        // Return the same URL for now
        return desktopImageUrl;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null) {
            return;
        }

        try {
            googleDriveService.deleteFile(imageUrl);
            log.info("Successfully deleted image from Google Drive: {}", imageUrl);
        } catch (Exception e) {
            log.error("Error deleting image from Google Drive: {}", imageUrl, e);
        }
    }

    private boolean hasValidExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return ALLOWED_FORMATS.stream().anyMatch(lowerCaseFilename::endsWith);
    }
}
