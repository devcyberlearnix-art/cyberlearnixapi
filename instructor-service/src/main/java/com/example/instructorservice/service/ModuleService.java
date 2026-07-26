package com.example.instructorservice.service;

import com.example.instructorservice.dto.ModuleRequest;
import com.example.instructorservice.dto.ModuleResponse;
import com.example.instructorservice.dto.ResourceResponse;
import com.example.instructorservice.entity.*;
import com.example.instructorservice.entity.Module;
import com.example.instructorservice.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final resourceRepository resourceRepository; // ✅ correct
    private final ContentRepository contentRepository;
    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:cyberlearnix}")
    private String folder;
    @Transactional
    public ModuleResponse addModule(UUID instructorId, UUID courseId, ModuleRequest request) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        Course course = courseRepository.findById(Long.valueOf(courseId.toString()))
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new RuntimeException("Instructor does not own this course");
        }

        LocalDateTime now = LocalDateTime.now();

        // Determine module order via DB
        int moduleOrder = moduleRepository.findByCourseId(course.getId()).size() + 1;

        Module module = new Module();
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setCourse(course);

        Module savedModule = moduleRepository.save(module);

        return ModuleResponse.builder()
                .moduleId(savedModule.getId())
                .moduleTitle(savedModule.getTitle())
                .moduleDescription(savedModule.getDescription())
                .moduleStatus("ACTIVE")
                .moduleOrder(moduleOrder)
                .moduleCreatedAt(now)
                .moduleUpdatedAt(now)

                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getTitle() + " Description")
                .courseStatus("PUBLISHED")
                .courseCreatedAt(course.getCreatedAt()) // if available
                .totalModules(moduleOrder)

                .instructorId(instructor.getId())
                .instructorName(instructor.getName())
                .instructorEmail(instructor.getEmail()) // fetch real email

                .status("success")
                .message("Module added successfully")
                .requestId(UUID.randomUUID().toString())
                .timestamp(now)
                .build();
    }

    @Transactional
    public ModuleResponse updateModule(UUID instructorId, UUID courseId, UUID moduleId, ModuleRequest request) {

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        Course course = courseRepository.findById(Long.valueOf(courseId.toString()))
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new RuntimeException("Instructor does not own this course");
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Module does not belong to this course");
        }

        // Update fields
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setUpdatedAt(LocalDateTime.now());

        Module updatedModule = moduleRepository.save(module);

        LocalDateTime now = LocalDateTime.now();

        return ModuleResponse.builder()
                // Module Info
                .moduleId(updatedModule.getId())
                .moduleTitle(updatedModule.getTitle())
                .moduleDescription(updatedModule.getDescription())
                .moduleStatus(updatedModule.getStatus())
                .moduleOrder(updatedModule.getOrderNumber())
                .moduleCreatedAt(updatedModule.getCreatedAt())
                .moduleUpdatedAt(updatedModule.getUpdatedAt())

                // Course Info
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getTitle() + " Description")
                .courseStatus("PUBLISHED")
                .courseCreatedAt(course.getCreatedAt())
                .totalModules(moduleRepository.findByCourseId(course.getId()).size())

                // Instructor Info
                .instructorId(instructor.getId())
                .instructorName(instructor.getName())
                .instructorEmail(instructor.getEmail())

                // Metadata
                .status("success")
                .message("Module updated successfully")
                .requestId(UUID.randomUUID().toString())
                .timestamp(now)
                .build();
    }

    @Transactional
    public ModuleResponse deleteModule(UUID instructorId, UUID courseId, UUID moduleId) {

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        Course course = courseRepository.findById(Long.valueOf(courseId.toString()))
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new RuntimeException("Instructor does not own this course");
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Module does not belong to this course");
        }

        // Capture data BEFORE delete (for response)
        UUID deletedModuleId = module.getId();
        String deletedTitle = module.getTitle();
        String deletedDescription = module.getDescription();
        Integer moduleOrder = module.getOrderNumber();
        LocalDateTime createdAt = module.getCreatedAt();

        // Delete module
        moduleRepository.delete(module);

        LocalDateTime now = LocalDateTime.now();

        return ModuleResponse.builder()
                // Module Info (deleted)
                .moduleId(deletedModuleId)
                .moduleTitle(deletedTitle)
                .moduleDescription(deletedDescription)
                .moduleStatus("DELETED")
                .moduleOrder(moduleOrder)
                .moduleCreatedAt(createdAt)
                .moduleUpdatedAt(now)

                // Course Info
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseDescription(course.getTitle() + " Description")
                .courseStatus("PUBLISHED")
                .courseCreatedAt(course.getCreatedAt())
                .totalModules(moduleRepository.findByCourseId(course.getId()).size()) // updated count

                // Instructor Info
                .instructorId(instructor.getId())
                .instructorName(instructor.getName())
                .instructorEmail(instructor.getEmail())

                // Meta
                .status("success")
                .message("Module deleted successfully")
                .requestId(UUID.randomUUID().toString())
                .timestamp(now)
                .build();
    }

    @Transactional
    public ResourceResponse uploadResource(
            UUID instructorId,
            UUID courseId,
            MultipartFile file,
            String type
    ) {

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        Course course = courseRepository.findById(Long.valueOf(courseId.toString()))
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new RuntimeException("Instructor does not own this course");
        }

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        String safeName = (fileName != null && !fileName.isBlank())
                ? fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
        String publicId = UUID.randomUUID().toString() + "_" + safeName;
        if (publicId.contains(".")) {
            publicId = publicId.substring(0, publicId.lastIndexOf("."));
        }

        String fileUrl;
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", "auto"
            ));
            fileUrl = (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }

        // 🔥 1. CREATE CONTENT (THIS WAS MISSING)
        Content content = Content.builder()
                .title(fileName)
                .type(type.toUpperCase())
                .course(course)
                .instructor(instructor)
                .status(Course.CourseStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        contentRepository.save(content);

        // 🔥 2. CREATE RESOURCE & LINK TO CONTENT
        Resource resource = Resource.builder()
                .fileName(fileName)
                .fileType(type.toUpperCase())
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .content(content) // ✅ FIXED
                .build();

        Resource saved = resourceRepository.save(resource);

        // 🔥 3. RETURN CONTENT ID (NOT RESOURCE ID)
        return ResourceResponse.builder()
                .contentId(content.getId()) // ✅ FIXED
                .fileName(saved.getFileName())
                .fileType(saved.getFileType())
                .fileUrl(saved.getFileUrl())
                .fileSize(saved.getFileSize())
                .uploadedAt(saved.getCreatedAt())

                .courseId(course.getId())
                .courseTitle(course.getTitle())

                .instructorId(instructor.getId())
                .instructorName(instructor.getName())
                .instructorEmail(instructor.getEmail())

                .status("success")
                .message("Resource uploaded successfully")
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}