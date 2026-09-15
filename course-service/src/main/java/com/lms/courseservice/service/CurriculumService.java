package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseDetailsDTO;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.LectureRepository;
import com.lms.courseservice.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LectureRepository lectureRepository;

    @Cacheable(value = "courseCurriculum", key = "#courseId", unless = "#result == null")
    @Transactional(readOnly = true)
    public CourseDetailsDTO.CourseCurriculumDTO getCourseCurriculum(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Use optimized query to prevent N+1 problem
        List<Section> sections = sectionRepository.findByCourseIdWithLectures(courseId);
        
        List<CourseDetailsDTO.SectionDTO> sectionDTOs = sections.stream()
                .map(section -> {
                    // Lectures are already loaded via JOIN FETCH
                    List<Lecture> lectures = section.getLectures();
                    Integer sectionDuration = lectures.stream()
                            .mapToInt(Lecture::getDuration)
                            .sum();
                    
                    List<CourseDetailsDTO.LectureDTO> lectureDTOs = lectures.stream()
                            .map(this::buildLectureDTO)
                            .collect(Collectors.toList());
                    
                    return CourseDetailsDTO.SectionDTO.builder()
                            .sectionId(section.getId())
                            .title(section.getTitle())
                            .orderIndex(section.getOrderIndex())
                            .sectionDuration(sectionDuration)
                            .lectureCount(lectures.size())
                            .lectures(lectureDTOs)
                            .build();
                })
                .sorted(Comparator.comparing(CourseDetailsDTO.SectionDTO::getOrderIndex))
                .collect(Collectors.toList());

        Integer totalDuration = sectionDTOs.stream()
                .mapToInt(CourseDetailsDTO.SectionDTO::getSectionDuration)
                .sum();
        
        Integer totalLectures = sectionDTOs.stream()
                .mapToInt(CourseDetailsDTO.SectionDTO::getLectureCount)
                .sum();

        return CourseDetailsDTO.CourseCurriculumDTO.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .totalDuration(totalDuration)
                .totalSections(sectionDTOs.size())
                .totalLectures(totalLectures)
                .sections(sectionDTOs)
                .build();
    }

    private CourseDetailsDTO.LectureDTO buildLectureDTO(Lecture lecture) {
        return CourseDetailsDTO.LectureDTO.builder()
                .lectureId(lecture.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .videoUrl(lecture.getVideoUrl())
                .duration(lecture.getDuration())
                .orderIndex(lecture.getOrderIndex())
                .previewEnabled(lecture.getPreviewEnabled())
                .resources(lecture.getResources())
                .build();
    }

    @CacheEvict(value = "courseCurriculum", allEntries = true)
    public void evictCurriculumCache() {
        log.info("Evicting all curriculum cache");
    }

    @CacheEvict(value = "courseCurriculum", key = "#courseId")
    public void evictCurriculumForCourse(Long courseId) {
        log.info("Evicting curriculum cache for course: {}", courseId);
    }
}