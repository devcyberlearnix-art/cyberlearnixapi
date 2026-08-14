package com.lms.courseservice.controller;

import com.lms.courseservice.dto.CreateSectionResponse;
import com.lms.courseservice.dto.DeleteSectionResponse;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.security.JwtUtil;
import com.lms.courseservice.service.LectureService;
import com.lms.courseservice.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    private final JwtUtil jwtUtil;
    private final LectureService lectureService;
    private final EnrollmentRepository enrollmentRepository;

    // Instructor/Admin only
    @PostMapping("/{courseId}/sections")
    public CreateSectionResponse createSection(@PathVariable Long courseId,
            @RequestBody Section section) {
        Section saved = sectionService.createSection(courseId, section);

        CreateSectionResponse.SectionInfo info = new CreateSectionResponse.SectionInfo(
                saved.getId(),
                saved.getTitle(),
                saved.getOrderIndex(),
                saved.getCourse().getId(),
                saved.getCourse().getTitle()
        );

        return new CreateSectionResponse(
                true,
                "Section created successfully",
                info
        );
    }

    // Everyone can view
    @GetMapping("/{courseId}/sections")
    public List<Section> getSections(@PathVariable Long courseId) {
        return sectionService.getSectionsByCourseId(courseId);
    }

    // Instructor/Admin only
    @PatchMapping("/sections/{sectionId}")
    public Section updateSection(@PathVariable Long sectionId,
            @RequestBody Section section) {
        return sectionService.updateSection(sectionId, section);
    }

    // Instructor/Admin only
    @DeleteMapping("/sections/{sectionId}")
    public DeleteSectionResponse deleteSection(@PathVariable Long sectionId) {
        Section section = sectionService.deleteSection(sectionId);

        DeleteSectionResponse.SectionInfo info = new DeleteSectionResponse.SectionInfo(
                section.getId(),
                section.getTitle(),
                section.getCourse() != null ? section.getCourse().getId() : null,
                true
        );

        return new DeleteSectionResponse(
                true,
                "Section deleted successfully",
                info
        );
    }
}