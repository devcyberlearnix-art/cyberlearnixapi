package com.lms.courseservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationService {

    private final CacheManager cacheManager;

    public void evictCourseDetailsForCourse(Long courseId) {
        try {
            // Clear all course details cache entries
            if (cacheManager.getCache("courseDetails") != null) {
                cacheManager.getCache("courseDetails").clear();
                log.info("Evicted course details cache for course: {}", courseId);
            }
        } catch (Exception e) {
            log.error("Error evicting course details cache for course: {}", courseId, e);
        }
    }

    public void evictCurriculumForCourse(Long courseId) {
        try {
            // Clear all curriculum cache entries
            if (cacheManager.getCache("courseCurriculum") != null) {
                cacheManager.getCache("courseCurriculum").clear();
                log.info("Evicted curriculum cache for course: {}", courseId);
            }
        } catch (Exception e) {
            log.error("Error evicting curriculum cache for course: {}", courseId, e);
        }
    }

    public void evictAllCourseCaches() {
        try {
            if (cacheManager.getCache("courseDetails") != null) {
                cacheManager.getCache("courseDetails").clear();
            }
            if (cacheManager.getCache("courseCurriculum") != null) {
                cacheManager.getCache("courseCurriculum").clear();
            }
            log.info("Evicted all course-related caches");
        } catch (Exception e) {
            log.error("Error evicting all course caches", e);
        }
    }
}