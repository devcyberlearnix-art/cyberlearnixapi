package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseDetailsDTO;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.LectureRepository;
import com.lms.courseservice.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private CacheInvalidationService cacheInvalidationService;

    @InjectMocks
    private SectionService sectionService;

    private Course testCourse;
    private Section testSection;
    private Lecture testLecture;

    @BeforeEach
    void setUp() {
        testCourse = Course.builder()
                .id(1L)
                .title("Test Course")
                .instructorId(UUID.randomUUID())
                .build();

        testSection = Section.builder()
                .id(1L)
                .title("Test Section")
                .orderIndex(1)
                .course(testCourse)
                .build();

        testLecture = Lecture.builder()
                .id(1L)
                .title("Test Lecture")
                .duration(30)
                .section(testSection)
                .build();
    }

    @Test
    void testCreateSection() {
        when(courseRepository.findById(1L)).thenReturn(java.util.Optional.of(testCourse));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);

        Section result = sectionService.createSection(1L, testSection);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Section");
        verify(sectionRepository, times(1)).save(any(Section.class));
        verify(cacheInvalidationService, times(1)).evictCurriculumForCourse(1L);
    }

    @Test
    void testUpdateSection() {
        when(sectionRepository.findById(1L)).thenReturn(java.util.Optional.of(testSection));
        when(sectionRepository.save(any(Section.class))).thenReturn(testSection);

        Section updatedSection = Section.builder()
                .title("Updated Section")
                .orderIndex(2)
                .build();

        Section result = sectionService.updateSection(1L, updatedSection);

        assertThat(result).isNotNull();
        verify(sectionRepository, times(1)).save(any(Section.class));
        verify(cacheInvalidationService, times(1)).evictCurriculumForCourse(testCourse.getId());
    }

    @Test
    void testDeleteSection() {
        when(sectionRepository.findById(1L)).thenReturn(java.util.Optional.of(testSection));
        when(lectureRepository.findBySectionId(1L)).thenReturn(Arrays.asList(testLecture));
        doNothing().when(sectionRepository).delete(testSection);

        sectionService.deleteSection(1L);

        verify(lectureRepository, times(1)).deleteAll(anyList());
        verify(sectionRepository, times(1)).delete(testSection);
        verify(cacheInvalidationService, times(1)).evictCurriculumForCourse(testCourse.getId());
    }
}
