package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.QuestionRequest;
import com.lms.courseservice.dto.QuestionResponse;
import com.lms.courseservice.dto.QuizRequest;
import com.lms.courseservice.dto.QuizResponse;
import com.lms.courseservice.dto.StudentQuizResponse;
import com.lms.courseservice.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    private UUID extractUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }

    private String getCurrentTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    // ==================== INSTRUCTOR/ADMIN APIs ====================

    /**
     * Create quiz
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(@RequestBody QuizRequest request) {
        UUID instructorId = extractUserId();
        QuizResponse response = quizService.createQuiz(request, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz created successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Update quiz
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PutMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizRequest request) {
        UUID instructorId = extractUserId();
        QuizResponse response = quizService.updateQuiz(quizId, request, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz updated successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get quiz by ID
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(@PathVariable Long quizId) {
        QuizResponse response = quizService.getQuiz(quizId);
        return ResponseEntity.ok(
                ApiResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Delete quiz
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long quizId) {
        UUID instructorId = extractUserId();
        quizService.deleteQuiz(quizId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Quiz deleted successfully")
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Publish quiz
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/{quizId}/publish")
    public ResponseEntity<ApiResponse<QuizResponse>> publishQuiz(@PathVariable Long quizId) {
        UUID instructorId = extractUserId();
        QuizResponse response = quizService.publishQuiz(quizId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz published successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Unpublish quiz
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PatchMapping("/{quizId}/unpublish")
    public ResponseEntity<ApiResponse<QuizResponse>> unpublishQuiz(@PathVariable Long quizId) {
        UUID instructorId = extractUserId();
        QuizResponse response = quizService.unpublishQuiz(quizId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<QuizResponse>builder()
                        .success(true)
                        .message("Quiz unpublished successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * List quizzes by course
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzesByCourse(@PathVariable Long courseId) {
        List<QuizResponse> response = quizService.getQuizzesByCourse(courseId);
        return ResponseEntity.ok(
                ApiResponse.<List<QuizResponse>>builder()
                        .success(true)
                        .message("Quizzes retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * List quizzes by section
     */
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzesBySection(@PathVariable Long sectionId) {
        List<QuizResponse> response = quizService.getQuizzesBySection(sectionId);
        return ResponseEntity.ok(
                ApiResponse.<List<QuizResponse>>builder()
                        .success(true)
                        .message("Quizzes retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // ==================== STUDENT APIs ====================

    /**
     * Get published quizzes for a course (student view)
     */
    @GetMapping("/course/{courseId}/published")
    public ResponseEntity<ApiResponse<List<StudentQuizResponse>>> getPublishedQuizzesByCourse(@PathVariable Long courseId) {
        List<StudentQuizResponse> response = quizService.getPublishedQuizzesByCourse(courseId);
        return ResponseEntity.ok(
                ApiResponse.<List<StudentQuizResponse>>builder()
                        .success(true)
                        .message("Published quizzes retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // ==================== QUESTION MANAGEMENT ====================

    /**
     * Add question to quiz
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PostMapping("/{quizId}/questions")
    public ResponseEntity<ApiResponse<QuestionResponse>> addQuestion(
            @PathVariable Long quizId,
            @RequestBody QuestionRequest request) {
        UUID instructorId = extractUserId();
        QuestionResponse response = quizService.addQuestion(quizId, request, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<QuestionResponse>builder()
                        .success(true)
                        .message("Question added successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Update question
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionRequest request) {
        UUID instructorId = extractUserId();
        QuestionResponse response = quizService.updateQuestion(questionId, request, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<QuestionResponse>builder()
                        .success(true)
                        .message("Question updated successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Delete question
     */
    @PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long questionId) {
        UUID instructorId = extractUserId();
        quizService.deleteQuestion(questionId, instructorId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Question deleted successfully")
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }
}
