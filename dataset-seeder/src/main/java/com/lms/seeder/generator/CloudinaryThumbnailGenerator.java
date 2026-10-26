package com.lms.seeder.generator;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloudinaryThumbnailGenerator {

    private final Cloudinary cloudinary;
    private final String folder = "cyberlearnix";

    /**
     * Generate a topic-specific thumbnail URL for a course by uploading to Cloudinary.
     * 
     * This method actually uploads a generated SVG image to Cloudinary with category-specific
     * styling. NO placeholder URLs are used. If upload fails, the seeder will fail.
     * 
     * @param category The course category
     * @param courseId The course ID for unique public_id
     * @return Secure HTTPS URL of the uploaded thumbnail
     * @throws RuntimeException if upload fails (no fallbacks, no placeholders)
     */
    public String generateThumbnail(String category, Long courseId) {
        try {
            String publicId = String.format("%s/course-thumbnails/course_%d", folder, courseId);
            
            // Generate SVG image with category-specific design
            String svgData = generateCategorySVG(category);
            
            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                svgData,
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "image",
                    "folder", folder + "/course-thumbnails",
                    "overwrite", true
                )
            );
            
            String url = (String) uploadResult.get("secure_url");
            
            // Verify the URL is a valid Cloudinary HTTPS URL
            if (url == null || !url.startsWith("https://res.cloudinary.com/")) {
                throw new RuntimeException("Invalid Cloudinary URL returned: " + url);
            }
            
            log.info("Uploaded thumbnail for course {} (category: {}): {}", courseId, category, url);
            return url;
            
        } catch (IOException e) {
            log.error("FAILED to upload thumbnail for course {} (category: {}): {}", 
                    courseId, category, e.getMessage());
            throw new RuntimeException("Cloudinary upload failed for course " + courseId + 
                    ". Cannot proceed without valid thumbnail. Error: " + e.getMessage(), e);
        }
    }

    /**
     * Generate an SVG image with category-specific design.
     * This creates a professional-looking thumbnail with the category name.
     */
    private String generateCategorySVG(String category) {
        String color = getCategoryColor(category);
        String secondaryColor = getSecondaryColor(category);
        String categoryShort = category.replace("/", " ").trim();
        
        String svg = String.format(
            "<svg xmlns='http://www.w3.org/2000/svg' width='800' height='450' viewBox='0 0 800 450'>" +
            "<defs>" +
            "<linearGradient id='grad' x1='0%%' y1='0%%' x2='100%%' y2='100%%'>" +
            "<stop offset='0%%' style='stop-color:%s;stop-opacity:1' />" +
            "<stop offset='100%%' style='stop-color:%s;stop-opacity:1' />" +
            "</linearGradient>" +
            "</defs>" +
            "<rect width='800' height='450' fill='url(#grad)'/>" +
            "<rect x='40' y='40' width='720' height='370' fill='none' stroke='white' stroke-width='4' rx='20' opacity='0.3'/>" +
            "<text x='400' y='200' font-family='Arial, sans-serif' font-size='32' font-weight='bold' fill='white' text-anchor='middle'>%s</text>" +
            "<text x='400' y='250' font-family='Arial, sans-serif' font-size='18' fill='white' text-anchor='middle' opacity='0.9'>CyberLearnix Course</text>" +
            "</svg>",
            color, secondaryColor, categoryShort
        );
        
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    /**
     * Get category-specific primary color for thumbnails
     */
    private String getCategoryColor(String category) {
        switch (category) {
            case "Java/JVM": return "#f89820"; // Java orange
            case "Spring Boot": return "#6db33f"; // Spring green
            case "Microservices": return "#326ce5"; // Blue
            case "Backend Development": return "#00599C"; // Dark blue
            case "SQL/PostgreSQL": return "#336791"; // PostgreSQL blue
            case "Docker/Kubernetes": return "#0db7ed"; // Docker blue
            case "AWS/Cloud": return "#ff9900"; // AWS orange
            case "DevOps/CI-CD": return "#26a69a"; // Teal
            case "Python": return "#3776ab"; // Python blue
            case "Cyber Security": return "#e74c3c"; // Red
            case "React/JavaScript": return "#61dafb"; // React blue
            case "AI/ML": return "#ff6f61"; // Coral
            case "Data Engineering": return "#f39c12"; // Orange
            case "System Design": return "#9b59b6"; // Purple
            case "Software Engineering": return "#34495e"; // Dark gray
            default: return "#3498db"; // Default blue
        }
    }

    /**
     * Get category-specific secondary color for gradient
     */
    private String getSecondaryColor(String category) {
        switch (category) {
            case "Java/JVM": return "#d4880f";
            case "Spring Boot": return "#5a9a2f";
            case "Microservices": return "#2648b0";
            case "Backend Development": return "#00427a";
            case "SQL/PostgreSQL": return "#264d6a";
            case "Docker/Kubernetes": return "#0a9ec7";
            case "AWS/Cloud": return "#cc8800";
            case "DevOps/CI-CD": return "#1e8070";
            case "Python": return "#2b5a88";
            case "Cyber Security": return "#c0392b";
            case "React/JavaScript": return "#4fa8d6";
            case "AI/ML": return "#d65a4d";
            case "Data Engineering": return "#d6860d";
            case "System Design": return "#7d3c98";
            case "Software Engineering": return "#2c3e50";
            default: return "#2980b9";
        }
    }
}
