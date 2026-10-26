package com.lms.courseservice.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.lms.courseservice.entity.Course;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
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
                .instructorProfile(details.getInstructorProfile())
                .curriculum(details.getCurriculum())
                .enrollmentStatus(details.getEnrollmentStatus())
                .ratingSummary(details.getRatingSummary())
                .courseStats(details.getCourseStats())
                .requirements(details.getRequirements())
                .learningOutcomes(details.getLearningOutcomes())
                .materials(details.getMaterials())
                .faqs(details.getFaqs())
                .build();
    }
}