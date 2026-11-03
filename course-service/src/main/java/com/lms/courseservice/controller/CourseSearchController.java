package com.lms.courseservice.controller;

import com.lms.courseservice.dto.SearchSuggestionProjection;
import com.lms.courseservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseSearchController {

    private final CourseRepository courseRepository;

    @GetMapping("/suggestions")
    public ResponseEntity<List<SearchSuggestionProjection>> getPredictions(
            @RequestParam(value = "q", required = false) String q) {
        long startTime = System.currentTimeMillis();

        if (q == null || q.strip().length() < 2) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        String cleanQuery = q.strip();
        List<SearchSuggestionProjection> results = courseRepository.fetchFastSuggestions(cleanQuery);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[SEARCH-TELEMETRY] Query='{}' | Hits={} | ExecTime={}ms | Status=200", 
                 cleanQuery, results.size(), duration);

        return ResponseEntity.ok(results);
    }
}

