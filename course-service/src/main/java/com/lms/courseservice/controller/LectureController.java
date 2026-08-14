package com.lms.courseservice.controller;

import com.lms.courseservice.dto.CreateLectureResponse;
import com.lms.courseservice.dto.DeleteLectureResponse;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.service.LectureService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    // ✅ Instructor/Admin only
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN','ADMIN')")
    @PostMapping("/{sectionId}/lectures")
    public CreateLectureResponse createLecture(@PathVariable Long sectionId,
                                 @RequestBody Lecture lecture) {
        Lecture saved = lectureService.createLecture(sectionId, lecture);

        CreateLectureResponse.LectureInfo info = new CreateLectureResponse.LectureInfo(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getVideoUrl(),
                saved.getDuration(),
                saved.getOrderIndex(),
                saved.getPreviewEnabled(),
                saved.getResources(),
                saved.getSection().getId(),
                saved.getSection().getTitle(),
                saved.getSection().getCourse() != null ? saved.getSection().getCourse().getId() : null
        );

        return new CreateLectureResponse(
                true,
                "Lecture created successfully",
                info
        );
    }

    // ✅ Instructor/Admin only
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN','ADMIN')")
    @PatchMapping("/{sectionId}/lectures/{lectureId}")
    public Lecture updateLecture(@PathVariable Long sectionId,
                                 @PathVariable Long lectureId,
                                 @RequestBody Lecture lecture) {

        return lectureService.updateLecture(lectureId, lecture);
    }

    // ✅ Instructor/Admin only
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN','ADMIN')")
    @DeleteMapping("/{sectionId}/lectures/{lectureId}")
    public DeleteLectureResponse deleteLecture(@PathVariable Long sectionId,
                                             @PathVariable Long lectureId) {
        Lecture lecture = lectureService.deleteLecture(sectionId, lectureId);

        DeleteLectureResponse.DeletedLectureInfo info = new DeleteLectureResponse.DeletedLectureInfo(
                lecture.getId(),
                lecture.getTitle(),
                lecture.getSection() != null ? lecture.getSection().getId() : null,
                true
        );

        return new DeleteLectureResponse(
                true,
                "Lecture deleted successfully",
                info
        );
    }

    // 🔒 Only enrolled users (handled in service)
    @GetMapping("/{sectionId}/lectures")
    public List<Lecture> getLectures(@PathVariable Long sectionId) {
        return lectureService.getLecturesBySection(sectionId);
    }
}