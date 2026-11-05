package com.lms.courseservice.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.lms.courseservice.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseByIdResponse {

    @JsonUnwrapped
    private Course course;

    private CourseDetailsDTO.InstructorProfileDTO instructorProfile;
    private CourseDetailsDTO.CourseCurriculumDTO curriculum;
    private CourseDetailsDTO.EnrollmentStatusDTO enrollmentStatus;
    private CourseDetailsDTO.RatingSummaryDTO ratingSummary;
    private CourseDetailsDTO.CourseStatsDTO courseStats;
    private List<CourseDetailsDTO.CourseRequirementsDTO> requirements;
    private List<CourseDetailsDTO.LearningOutcomesDTO> learningOutcomes;
    private List<CourseDetailsDTO.CourseMaterialsDTO> materials;
    private List<CourseDetailsDTO.CourseFAQDTO> faqs;

    public static CourseByIdResponse from(Course course, CourseDetailsDTO details) {
        return CourseByIdResponse.builder()
                .course(course)
                .instructorProfile(details != null ? details.getInstructorProfile() : null)
                .curriculum(details != null ? details.getCurriculum() : null)
                .enrollmentStatus(details != null ? details.getEnrollmentStatus() : null)
                .ratingSummary(details != null ? details.getRatingSummary() : null)
                .courseStats(details != null ? details.getCourseStats() : null)
                .requirements(details != null ? details.getRequirements() : null)
                .learningOutcomes(details != null ? details.getLearningOutcomes() : null)
                .materials(details != null ? details.getMaterials() : null)
                .faqs(details != null ? details.getFaqs() : null)
                .build();
    }
}

