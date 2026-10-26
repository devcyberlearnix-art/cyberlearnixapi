package com.lms.courseservice.controller;

import com.lms.courseservice.dto.*;
import com.lms.courseservice.service.QuizAttemptService;
import com.lms.courseservice.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;
    private final QuizService quizService;

    private UUID extractUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return principal instanceof UUID ? (UUID) principal : UUID.fromString(principal.toString());
    }

    private String getCurrentTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }

    // ==================== STUDENT QUIZ APIs ====================

    /**
     * Get student quiz (without correct answers)
     */
    @GetMapping("/{quizId}/student")
    public ResponseEntity<ApiResponse<StudentQuizResponse>> getStudentQuiz(@PathVariable Long quizId) {
        StudentQuizResponse response = quizService.getStudentQuiz(quizId);
        return ResponseEntity.ok(
                ApiResponse.<StudentQuizResponse>builder()
                        .success(true)
                        .message("Quiz retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get student quiz with questions (without correct answers)
     */
    @GetMapping("/{quizId}/student/questions")
    public ResponseEntity<ApiResponse<StudentQuizResponse>> getStudentQuizWithQuestions(@PathVariable Long quizId) {
        StudentQuizResponse response = quizService.getStudentQuizWithQuestions(quizId);
        return ResponseEntity.ok(
                ApiResponse.<StudentQuizResponse>builder()
                        .success(true)
                        .message("Quiz with questions retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    // ==================== QUIZ ATTEMPT FLOW ====================

    /**
     * Start quiz attempt
     */
    @PostMapping("/{quizId}/attempts")
    public ResponseEntity<ApiResponse<QuizAttemptResponse>> startQuizAttempt(@PathVariable Long quizId) {
        UUID studentId = extractUserId();
        QuizAttemptResponse response = quizAttemptService.startQuizAttempt(quizId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<QuizAttemptResponse>builder()
                        .success(true)
                        .message("Quiz attempt started successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get current attempt
     */
    @GetMapping("/{quizId}/attempts/current")
    public ResponseEntity<ApiResponse<QuizAttemptResponse>> getCurrentAttempt(@PathVariable Long quizId) {
        UUID studentId = extractUserId();
        QuizAttemptResponse response = quizAttemptService.getCurrentAttempt(quizId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<QuizAttemptResponse>builder()
                        .success(true)
                        .message("Current attempt retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Submit answer(s) for a question
     */
    @PostMapping("/{quizId}/attempts/{attemptId}/answers")
    public ResponseEntity<ApiResponse<QuizAttemptResponse>> submitAnswer(
            @PathVariable Long quizId,
            @PathVariable Long attemptId,
            @RequestBody AnswerRequest request) {
        UUID studentId = extractUserId();
        QuizAttemptResponse response = quizAttemptService.submitAnswer(quizId, attemptId, request, studentId);
        return ResponseEntity.ok(
                ApiResponse.<QuizAttemptResponse>builder()
                        .success(true)
                        .message("Answer submitted successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Submit quiz attempt
     */
    @PostMapping("/{quizId}/attempts/{attemptId}/submit")
    public ResponseEntity<ApiResponse<QuizAttemptResponse>> submitQuizAttempt(
            @PathVariable Long quizId,
            @PathVariable Long attemptId) {
        UUID studentId = extractUserId();
        QuizAttemptResponse response = quizAttemptService.submitQuizAttempt(quizId, attemptId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<QuizAttemptResponse>builder()
                        .success(true)
                        .message("Quiz attempt submitted successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get attempt result
     */
    @GetMapping("/{quizId}/attempts/{attemptId}/result")
    public ResponseEntity<ApiResponse<QuizResultResponse>> getAttemptResult(
            @PathVariable Long quizId,
            @PathVariable Long attemptId) {
        UUID studentId = extractUserId();
        QuizResultResponse response = quizAttemptService.getAttemptResult(quizId, attemptId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<QuizResultResponse>builder()
                        .success(true)
                        .message("Attempt result retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }

    /**
     * Get student's attempts for a quiz
     */
    @GetMapping("/{quizId}/attempts")
    public ResponseEntity<ApiResponse<List<QuizAttemptResponse>>> getStudentAttempts(@PathVariable Long quizId) {
        UUID studentId = extractUserId();
        List<QuizAttemptResponse> response = quizAttemptService.getStudentAttempts(quizId, studentId);
        return ResponseEntity.ok(
                ApiResponse.<List<QuizAttemptResponse>>builder()
                        .success(true)
                        .message("Student attempts retrieved successfully")
                        .data(response)
                        .timestamp(getCurrentTimestamp())
                        .build()
        );
    }
}
