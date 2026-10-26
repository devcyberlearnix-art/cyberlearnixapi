package com.lms.courseservice.service;

import com.lms.courseservice.dto.*;
import com.lms.courseservice.entity.*;
import com.lms.courseservice.exception.QuizException;
import com.lms.courseservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Start a quiz attempt
     */
    @Transactional
    public QuizAttemptResponse startQuizAttempt(Long quizId, UUID studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        if (quiz.getStatus() != Quiz.QuizStatus.PUBLISHED) {
            throw new QuizException("Quiz is not published");
        }
        
        validateEnrollment(studentId, quiz.getCourseId());
        
        Optional<QuizAttempt> existingAttempt = quizAttemptRepository
                .findByQuizIdAndStudentIdAndStatus(quizId, studentId, QuizAttempt.AttemptStatus.IN_PROGRESS);
        
        if (existingAttempt.isPresent()) {
            throw new QuizException("You already have an attempt in progress");
        }
        
        if (quiz.getMaxAttempts() != null) {
            Long attemptCount = quizAttemptRepository.countByQuizIdAndStudentId(quizId, studentId);
            if (attemptCount >= quiz.getMaxAttempts()) {
                throw new QuizException("You have reached the maximum number of attempts for this quiz");
            }
        }
        
        int nextAttemptNumber = 1;
        Optional<QuizAttempt> lastAttempt = quizAttemptRepository
                .findFirstByQuizIdAndStudentIdOrderByAttemptNumberDesc(quizId, studentId);
        if (lastAttempt.isPresent()) {
            nextAttemptNumber = lastAttempt.get().getAttemptNumber() + 1;
        }
        
        QuizAttempt attempt = QuizAttempt.builder()
                .quizId(quizId)
                .studentId(studentId)
                .attemptNumber(nextAttemptNumber)
                .status(QuizAttempt.AttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        
        attempt = quizAttemptRepository.save(attempt);
        log.info("Started quiz attempt: {} for quiz: {} by student: {}", 
                attempt.getId(), quizId, studentId);
        
        return mapToQuizAttemptResponse(attempt);
    }

    /**
     * Get current attempt for a student
     */
    @Transactional(readOnly = true)
    public QuizAttemptResponse getCurrentAttempt(Long quizId, UUID studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        validateEnrollment(studentId, quiz.getCourseId());
        
        QuizAttempt attempt = quizAttemptRepository
                .findByQuizIdAndStudentIdAndStatus(quizId, studentId, QuizAttempt.AttemptStatus.IN_PROGRESS)
                .orElseThrow(() -> new QuizException("No active attempt found. Start a new attempt."));
        
        return mapToQuizAttemptResponse(attempt);
    }

    /**
     * Submit answer(s) for a question
     */
    @Transactional
    public QuizAttemptResponse submitAnswer(Long quizId, Long attemptId, AnswerRequest request, UUID studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        validateEnrollment(studentId, quiz.getCourseId());
        
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new QuizException("Attempt not found with id: " + attemptId));
        
        if (!attempt.getQuizId().equals(quizId)) {
            throw new QuizException("Attempt does not belong to this quiz");
        }
        
        if (!attempt.getStudentId().equals(studentId)) {
            throw new QuizException("You can only submit answers for your own attempts");
        }
        
        if (attempt.getStatus() != QuizAttempt.AttemptStatus.IN_PROGRESS) {
            throw new QuizException("Cannot submit answers for a submitted or graded attempt");
        }
        
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new QuizException("Question not found with id: " + request.getQuestionId()));
        
        if (!question.getQuizId().equals(quizId)) {
            throw new QuizException("Question does not belong to this quiz");
        }
        
        if (quizAnswerRepository.existsByAttemptIdAndQuestionId(attemptId, request.getQuestionId())) {
            throw new QuizException("Answer already submitted for this question");
        }
        
        List<Option> options = optionRepository.findByQuestionId(request.getQuestionId());
        if (options.isEmpty()) {
            throw new QuizException("Question has no options");
        }
        
        String selectedOptionIdsStr = String.join(",", 
                request.getSelectedOptionIds().stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList()));
        
        QuizAnswer answer = QuizAnswer.builder()
                .attemptId(attemptId)
                .questionId(request.getQuestionId())
                .selectedOptionIds(selectedOptionIdsStr)
                .build();
        
        answer = quizAnswerRepository.save(answer);
        log.info("Submitted answer for question: {} in attempt: {} by student: {}", 
                request.getQuestionId(), attemptId, studentId);
        
        return mapToQuizAttemptResponse(attempt);
    }

    /**
     * Submit quiz attempt
     */
    @Transactional
    public QuizAttemptResponse submitQuizAttempt(Long quizId, Long attemptId, UUID studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        validateEnrollment(studentId, quiz.getCourseId());
        
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new QuizException("Attempt not found with id: " + attemptId));
        
        if (!attempt.getQuizId().equals(quizId)) {
            throw new QuizException("Attempt does not belong to this quiz");
        }
        
        if (!attempt.getStudentId().equals(studentId)) {
            throw new QuizException("You can only submit your own attempts");
        }
        
        if (attempt.getStatus() != QuizAttempt.AttemptStatus.IN_PROGRESS) {
            throw new QuizException("Attempt has already been submitted");
        }
        
        List<Question> questions = questionRepository.findByQuizId(quizId);
        if (questions.isEmpty()) {
            throw new QuizException("Quiz has no questions");
        }
        
        gradeAttempt(attempt, quiz, questions);
        
        attempt.setStatus(QuizAttempt.AttemptStatus.GRADED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt = quizAttemptRepository.save(attempt);
        
        log.info("Submitted and graded quiz attempt: {} for quiz: {} by student: {}", 
                attemptId, quizId, studentId);
        
        return mapToQuizAttemptResponse(attempt);
    }

    /**
     * Get attempt result
     */
    @Transactional(readOnly = true)
    public QuizResultResponse getAttemptResult(Long quizId, Long attemptId, UUID studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        validateEnrollment(studentId, quiz.getCourseId());
        
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new QuizException("Attempt not found with id: " + attemptId));
        
        if (!attempt.getQuizId().equals(quizId)) {
            throw new QuizException("Attempt does not belong to this quiz");
        }
        
        if (!attempt.getStudentId().equals(studentId)) {
            throw new QuizException("You can only view your own attempts");
        }
        
        if (attempt.getStatus() == QuizAttempt.AttemptStatus.IN_PROGRESS) {
            throw new QuizException("Attempt has not been submitted yet");
        }
        
        List<QuizAnswer> answers = quizAnswerRepository.findByAttemptId(attemptId);
        List<QuestionResultResponse> questionResults = answers.stream()
                .map(answer -> {
                    Question question = questionRepository.findById(answer.getQuestionId())
                            .orElse(null);
                    if (question == null) return null;
                    
                    return QuestionResultResponse.builder()
                            .questionId(answer.getQuestionId())
                            .questionText(question.getQuestionText())
                            .marks(question.getMarks())
                            .marksObtained(answer.getMarksObtained())
                            .isCorrect(answer.getIsCorrect())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        return QuizResultResponse.builder()
                .id(attempt.getId())
                .quizId(quizId)
                .quizTitle(quiz.getTitle())
                .attemptId(attemptId)
                .attemptNumber(attempt.getAttemptNumber())
                .score(attempt.getScore())
                .totalMarks(quiz.getTotalMarks())
                .percentage(attempt.getPercentage())
                .passed(attempt.getPassed())
                .submittedAt(attempt.getSubmittedAt())
                .questionResults(questionResults)
                .build();
    }

    /**
     * Get all attempts for a student for a quiz
     */
    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getStudentAttempts(Long quizId, UUID studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        validateEnrollment(studentId, quiz.getCourseId());
        
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizIdAndStudentId(quizId, studentId);
        return attempts.stream()
                .map(this::mapToQuizAttemptResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private void validateEnrollment(UUID studentId, Long courseId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new QuizException("Student is not enrolled in this course");
        }
    }

    private void gradeAttempt(QuizAttempt attempt, Quiz quiz, List<Question> questions) {
        int totalScore = 0;
        List<QuizAnswer> answers = quizAnswerRepository.findByAttemptId(attempt.getId());
        
        for (QuizAnswer answer : answers) {
            Question question = questions.stream()
                    .filter(q -> q.getId().equals(answer.getQuestionId()))
                    .findFirst()
                    .orElse(null);
            
            if (question == null) continue;
            
            List<Option> options = optionRepository.findByQuestionId(question.getId());
            List<Long> correctOptionIds = options.stream()
                    .filter(Option::getIsCorrect)
                    .map(Option::getId)
                    .collect(Collectors.toList());
            
            Set<Long> selectedOptionIds = new HashSet<>();
            if (answer.getSelectedOptionIds() != null && !answer.getSelectedOptionIds().isEmpty()) {
                selectedOptionIds = Arrays.stream(answer.getSelectedOptionIds().split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
            }
            
            boolean isCorrect = selectedOptionIds.equals(new HashSet<>(correctOptionIds));
            int marksObtained = isCorrect ? question.getMarks() : 0;
            
            answer.setIsCorrect(isCorrect);
            answer.setMarksObtained(marksObtained);
            quizAnswerRepository.save(answer);
            
            totalScore += marksObtained;
        }
        
        attempt.setScore(totalScore);
        
        if (quiz.getTotalMarks() != null && quiz.getTotalMarks() > 0) {
            int percentage = (totalScore * 100) / quiz.getTotalMarks();
            attempt.setPercentage(percentage);
            
            if (quiz.getPassingScore() != null) {
                attempt.setPassed(percentage >= quiz.getPassingScore());
            } else {
                attempt.setPassed(percentage >= 50);
            }
        } else {
            attempt.setPercentage(0);
            attempt.setPassed(false);
        }
    }

    private QuizAttemptResponse mapToQuizAttemptResponse(QuizAttempt attempt) {
        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuizId())
                .studentId(attempt.getStudentId())
                .attemptNumber(attempt.getAttemptNumber())
                .status(attempt.getStatus().name())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .score(attempt.getScore())
                .percentage(attempt.getPercentage())
                .passed(attempt.getPassed())
                .createdAt(attempt.getCreatedAt())
                .updatedAt(attempt.getUpdatedAt())
                .build();
    }
}
