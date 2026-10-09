package com.lms.courseservice.service;


import com.lms.courseservice.entity.Lecture;

import com.lms.courseservice.entity.Section;

import com.lms.courseservice.repository.LectureRepository;

import com.lms.courseservice.repository.SectionRepository;

import com.lms.courseservice.repository.EnrollmentRepository;


import lombok.RequiredArgsConstructor;


import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;


import java.util.List;

import java.util.UUID;


@Service

@RequiredArgsConstructor

public class LectureService {


    private final LectureRepository lectureRepository;

    private final SectionRepository sectionRepository;

    private final EnrollmentRepository enrollmentRepository;

    private final SectionService sectionService;


    // 🔒 Common method to validate enrollment
    // Temporarily disabled for testing - re-enable entire method for production

    private void validateEnrollment(Long sectionId) {


        // 🔥 Get UUID from JWT

        // Temporarily skip validation for testing

        /*

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null || "anonymousUser".equals(principal)) {

            return; // Skip validation for anonymous users

        }

        String userId = principal.toString();

        UUID studentId = UUID.fromString(userId);


        Long courseId = sectionService.getCourseIdBySection(sectionId);


        boolean enrolled = enrollmentRepository

            .existsByStudentIdAndCourseId(studentId, courseId);


        if (!enrolled) {

            throw new AccessDeniedException("You are not enrolled in this course");

        }

        */

    }


    // ✅ Create Lecture

    public Lecture createLecture(Long sectionId, Lecture lecture) {


        Section section = sectionRepository.findById(sectionId)

                .orElseThrow(() -> new RuntimeException("Section not found"));

        // Validate lecture title
        if (lecture.getTitle() == null || lecture.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Lecture title cannot be blank");
        }

        // Validate duration
        if (lecture.getDuration() != null && lecture.getDuration() < 0) {
            throw new RuntimeException("Lecture duration cannot be negative");
        }

        // Validate order index
        if (lecture.getOrderIndex() != null && lecture.getOrderIndex() < 0) {
            throw new RuntimeException("Lecture order index must be a non-negative integer");
        }

        // Prevent duplicate lecture title in same section

        lectureRepository.findByTitleAndSectionId(lecture.getTitle(), sectionId)

                .ifPresent(l -> {

                    throw new RuntimeException("Lecture with this title already exists in this section");

                });


        lecture.setSection(section);


        return lectureRepository.save(lecture);

    }


    // 🔒 Get Lectures by Section (ONLY ENROLLED USERS)
    // Completely public for testing - re-enable validateEnrollment(sectionId) for production

    public List<Lecture> getLecturesBySection(Long sectionId) {


        // validateEnrollment(sectionId); // Temporarily disabled for testing


        return lectureRepository.findBySectionId(sectionId);

    }


    // 🔒 Update Lecture (OPTIONAL: restrict to enrolled or admin/instructor)

    public Lecture updateLecture(Long lectureId, Lecture updatedLecture) {


        Lecture lecture = lectureRepository.findById(lectureId)

                .orElseThrow(() -> new RuntimeException("Lecture not found"));


        if (updatedLecture.getTitle() != null) {
            if (updatedLecture.getTitle().trim().isEmpty()) {
                throw new RuntimeException("Lecture title cannot be blank");
            }
            lecture.setTitle(updatedLecture.getTitle());
        }

        if (updatedLecture.getDescription() != null)

            lecture.setDescription(updatedLecture.getDescription());


        if (updatedLecture.getVideoUrl() != null)

            lecture.setVideoUrl(updatedLecture.getVideoUrl());


        if (updatedLecture.getDuration() != null) {
            if (updatedLecture.getDuration() < 0) {
                throw new RuntimeException("Lecture duration cannot be negative");
            }
            lecture.setDuration(updatedLecture.getDuration());
        }

        if (updatedLecture.getOrderIndex() != null) {
            if (updatedLecture.getOrderIndex() < 0) {
                throw new RuntimeException("Lecture order index must be a non-negative integer");
            }
            lecture.setOrderIndex(updatedLecture.getOrderIndex());
        }

        return lectureRepository.save(lecture);

    }


    // 🔒 Delete Lecture
    public Lecture deleteLecture(Long sectionId, Long lectureId) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        if (!lecture.getSection().getId().equals(sectionId)) {
            throw new RuntimeException("Lecture does not belong to this section");
        }

        // ❌ REMOVE enrollment validation here

        lectureRepository.delete(lecture);
        return lecture;
    }

    // Get Lecture by ID
    public Lecture getLectureById(Long lectureId) {
        return lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));
    }

}
