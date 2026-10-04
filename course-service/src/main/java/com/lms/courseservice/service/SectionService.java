package com.lms.courseservice.service;

import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.SectionRepository;
import com.lms.courseservice.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final LectureRepository lectureRepository;
    private final CacheInvalidationService cacheInvalidationService;

    // Create Section
    public Section createSection(Long courseId, Section section) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Validate section title
        if (section.getTitle() == null || section.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Section title cannot be blank");
        }

        // Validate order index
        if (section.getOrderIndex() == null || section.getOrderIndex() < 0) {
            throw new RuntimeException("Section order index must be a non-negative integer");
        }

        section.setCourse(course);

        Section savedSection = sectionRepository.save(section);

        // Evict cache for this course
        cacheInvalidationService.evictCurriculumForCourse(courseId);

        return savedSection;
    }

    // Get Sections by Course
    public List<Section> getSectionsByCourseId(Long courseId) {
        return sectionRepository.findByCourseId(courseId);
    }

    // Update Section
    public Section updateSection(Long sectionId, Section updatedSection) {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        if (updatedSection.getTitle() != null) {
            if (updatedSection.getTitle().trim().isEmpty()) {
                throw new RuntimeException("Section title cannot be blank");
            }
            section.setTitle(updatedSection.getTitle());
        }

        if (updatedSection.getOrderIndex() != null) {
            if (updatedSection.getOrderIndex() < 0) {
                throw new RuntimeException("Section order index must be a non-negative integer");
            }
            section.setOrderIndex(updatedSection.getOrderIndex());
        }

        Section savedSection = sectionRepository.save(section);

        // Evict cache for this course
        Long courseId = section.getCourse().getId();
        cacheInvalidationService.evictCurriculumForCourse(courseId);

        return savedSection;
    }

    public Long getCourseIdBySection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        return section.getCourse().getId();
    }

    // Get Section by ID
    public Section getSectionById(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
    }

    // Delete Section
    @Transactional
    public Section deleteSection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Long courseId = section.getCourse().getId();

        // Delete lectures in this section first to avoid FK violation
        List<Lecture> lectures = lectureRepository.findBySectionId(sectionId);
        if (!lectures.isEmpty()) {
            lectureRepository.deleteAll(lectures);
        }

        sectionRepository.delete(section);

        // Evict cache for this course
        cacheInvalidationService.evictCurriculumForCourse(courseId);

        return section;
    }
}
