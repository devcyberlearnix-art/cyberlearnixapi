package com.lms.courseservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.courseservice.entity.Course;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourseByIdResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesCourseFieldsAlongsidePageDetails() throws Exception {
        Course course = Course.builder()
                .id(253L)
                .title("AI Engineer Bootcamp")
                .subtitle("Complete AI Engineer Training")
                .build();
        CourseDetailsDTO details = CourseDetailsDTO.builder()
                .curriculum(CourseDetailsDTO.CourseCurriculumDTO.builder()
                        .courseId(253L)
                        .totalSections(1)
                        .totalLectures(2)
                        .sections(List.of())
                        .build())
                .requirements(List.of())
                .learningOutcomes(List.of())
                .build();

        JsonNode response = objectMapper.valueToTree(CourseByIdResponse.from(course, details));

        assertThat(response.get("id").asLong()).isEqualTo(253L);
        assertThat(response.get("title").asText()).isEqualTo("AI Engineer Bootcamp");
        assertThat(response.has("course")).isFalse();
        assertThat(response.get("curriculum").get("totalLectures").asInt()).isEqualTo(2);
        assertThat(response.get("requirements").isArray()).isTrue();
    }
}