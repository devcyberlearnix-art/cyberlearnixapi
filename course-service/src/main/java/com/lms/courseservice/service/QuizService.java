package com.lms.courseservice.service;

import com.lms.courseservice.dto.*;
import com.lms.courseservice.entity.*;
import com.lms.courseservice.exception.QuizException;
import com.lms.courseservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    /**
     * Create a new quiz
     */
    @Transactional
    public QuizResponse createQuiz(QuizRequest request, UUID instructorId) {
        validateQuizRequest(request);
        
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new QuizException("Course not found with id: " + request.getCourseId()));
        
        validateInstructorOwnership(course, instructorId);
        
        if (request.getSectionId() != null) {
            Section section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new QuizException("Section not found with id: " + request.getSectionId()));
            
            if (!section.getCourse().getId().equals(request.getCourseId())) {
                throw new QuizException("Section does not belong to the specified course");
            }
        }
        
        Quiz quiz = Quiz.builder()
                .courseId(request.getCourseId())
                .sectionId(request.getSectionId())
                .title(request.getTitle())
                .description(request.getDescription())
                .instructions(request.getInstructions())
                .status(Quiz.QuizStatus.DRAFT)
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .maxAttempts(request.getMaxAttempts())
                .passingScore(request.getPassingScore())
                .totalMarks(0)
                .createdBy(instructorId)
                .build();
        
        quiz = quizRepository.save(quiz);
        log.info("Created quiz with id: {} for course: {} by instructor: {}", 
                quiz.getId(), quiz.getCourseId(), instructorId);
        
        return mapToQuizResponse(quiz);
    }

    /**
     * Update an existing quiz
     */
    @Transactional
    public QuizResponse updateQuiz(Long quizId, QuizRequest request, UUID instructorId) {
        validateQuizRequest(request);
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        Long courseId = quiz.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new QuizException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        if (request.getSectionId() != null) {
            Section section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new QuizException("Section not found with id: " + request.getSectionId()));
            
            if (!section.getCourse().getId().equals(request.getCourseId())) {
                throw new QuizException("Section does not belong to the specified course");
            }
            quiz.setSectionId(request.getSectionId());
        }
        
        if (request.getTitle() != null) {
            quiz.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            quiz.setDescription(request.getDescription());
        }
        if (request.getInstructions() != null) {
            quiz.setInstructions(request.getInstructions());
        }
        if (request.getTimeLimitMinutes() != null) {
            quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        }
        if (request.getMaxAttempts() != null) {
            quiz.setMaxAttempts(request.getMaxAttempts());
        }
        if (request.getPassingScore() != null) {
            quiz.setPassingScore(request.getPassingScore());
        }
        
        quiz = quizRepository.save(quiz);
        log.info("Updated quiz with id: {} by instructor: {}", quizId, instructorId);
        
        return mapToQuizResponse(quiz);
    }

    /**
     * Get quiz by ID
     */
    @Transactional(readOnly = true)
    public QuizResponse getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        return mapToQuizResponse(quiz);
    }

    /**
     * Delete a quiz
     */
    @Transactional
    public void deleteQuiz(Long quizId, UUID instructorId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        Long courseId = quiz.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new QuizException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        quizRepository.delete(quiz);
        log.info("Deleted quiz with id: {} by instructor: {}", quizId, instructorId);
    }

    /**
     * Publish a quiz
     */
    @Transactional
    public QuizResponse publishQuiz(Long quizId, UUID instructorId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        Long courseId = quiz.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new QuizException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        List<Question> questions = questionRepository.findByQuizId(quizId);
        if (questions.isEmpty()) {
            throw new QuizException("Cannot publish quiz without questions");
        }
        
        quiz.setStatus(Quiz.QuizStatus.PUBLISHED);
        quiz = quizRepository.save(quiz);
        log.info("Published quiz with id: {} by instructor: {}", quizId, instructorId);
        
        return mapToQuizResponse(quiz);
    }

    /**
     * Unpublish a quiz
     */
    @Transactional
    public QuizResponse unpublishQuiz(Long quizId, UUID instructorId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        Long courseId = quiz.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new QuizException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        quiz.setStatus(Quiz.QuizStatus.DRAFT);
        quiz = quizRepository.save(quiz);
        log.info("Unpublished quiz with id: {} by instructor: {}", quizId, instructorId);
        
        return mapToQuizResponse(quiz);
    }

    /**
     * List quizzes for a course
     */
    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new QuizException("Course not found with id: " + courseId));
        
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        return quizzes.stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    /**
     * List quizzes for a section
     */
    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesBySection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new QuizException("Section not found with id: " + sectionId));
        
        List<Quiz> quizzes = quizRepository.findBySectionId(sectionId);
        return quizzes.stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get published quizzes for a course (student view)
     */
    @Transactional(readOnly = true)
    public List<StudentQuizResponse> getPublishedQuizzesByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new QuizException("Course not found with id: " + courseId));
        
        List<Quiz> quizzes = quizRepository.findByCourseIdAndStatus(
                courseId, Quiz.QuizStatus.PUBLISHED);
        return quizzes.stream()
                .map(this::mapToStudentQuizResponse)
                .collect(Collectors.toList());
    }

    /**
     * Add question to quiz
     */
    @Transactional
    public QuestionResponse addQuestion(Long quizId, QuestionRequest request, UUID instructorId) {
        validateQuestionRequest(request);
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        Long courseId = quiz.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new QuizException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        if (quiz.getStatus() == Quiz.QuizStatus.PUBLISHED) {
            throw new QuizException("Cannot add questions to a published quiz");
        }
        
        Question.QuestionType questionType;
        try {
            questionType = Question.QuestionType.valueOf(request.getQuestionType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new QuizException("Invalid question type: " + request.getQuestionType());
        }
        
        Question question = Question.builder()
                .quizId(quizId)
                .questionText(request.getQuestionText())
                .questionType(questionType)
                .marks(request.getMarks())
                .orderIndex(request.getOrderIndex())
                .explanation(request.getExplanation())
                .build();
        
        question = questionRepository.save(question);
        
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            int correctCount = 0;
            for (OptionRequest optionRequest : request.getOptions()) {
                if (optionRequest.getIsCorrect() != null && optionRequest.getIsCorrect()) {
                    correctCount++;
                }
                
                Option option = Option.builder()
                        .questionId(question.getId())
                        .optionText(optionRequest.getOptionText())
                        .isCorrect(optionRequest.getIsCorrect() != null ? optionRequest.getIsCorrect() : false)
                        .orderIndex(optionRequest.getOrderIndex())
                        .build();
                optionRepository.save(option);
            }
            
            if (correctCount == 0) {
                throw new QuizException("Question must have at least one correct option");
            }
            
            if (questionType == Question.QuestionType.SINGLE_CHOICE && correctCount > 1) {
                throw new QuizException("Single choice question can only have one correct option");
            }
            
            if (questionType == Question.QuestionType.TRUE_FALSE && correctCount != 1) {
                throw new QuizException("True/False question must have exactly one correct option");
            }
        }
        
        updateQuizTotalMarks(quizId);
        
        log.info("Added question with id: {} to quiz: {} by instructor: {}", 
                question.getId(), quizId, instructorId);
        
        return mapToQuestionResponse(question);
    }

    /**
     * Update question
     */
    @Transactional
    public QuestionResponse updateQuestion(Long questionId, QuestionRequest request, UUID instructorId) {
        validateQuestionRequest(request);
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuizException("Question not found with id: " + questionId));
        
        Long quizId = question.getQuizId();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        Long courseId = quiz.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new QuizException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        if (quiz.getStatus() == Quiz.QuizStatus.PUBLISHED) {
            throw new QuizException("Cannot update questions in a published quiz");
        }
        
        if (request.getQuestionText() != null) {
            question.setQuestionText(request.getQuestionText());
        }
        if (request.getQuestionType() != null) {
            try {
                question.setQuestionType(Question.QuestionType.valueOf(request.getQuestionType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new QuizException("Invalid question type: " + request.getQuestionType());
            }
        }
        if (request.getMarks() != null) {
            question.setMarks(request.getMarks());
        }
        if (request.getOrderIndex() != null) {
            question.setOrderIndex(request.getOrderIndex());
        }
        if (request.getExplanation() != null) {
            question.setExplanation(request.getExplanation());
        }
        
        question = questionRepository.save(question);
        
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            List<Option> existingOptions = optionRepository.findByQuestionId(questionId);
            optionRepository.deleteAll(existingOptions);
            
            int correctCount = 0;
            for (OptionRequest optionRequest : request.getOptions()) {
                if (optionRequest.getIsCorrect() != null && optionRequest.getIsCorrect()) {
                    correctCount++;
                }
                
                Option option = Option.builder()
                        .questionId(question.getId())
                        .optionText(optionRequest.getOptionText())
                        .isCorrect(optionRequest.getIsCorrect() != null ? optionRequest.getIsCorrect() : false)
                        .orderIndex(optionRequest.getOrderIndex())
                        .build();
                optionRepository.save(option);
            }
            
            if (correctCount == 0) {
                throw new QuizException("Question must have at least one correct option");
            }
        }
        
        updateQuizTotalMarks(quizId);
        
        log.info("Updated question with id: {} by instructor: {}", questionId, instructorId);
        
        return mapToQuestionResponse(question);
    }

    /**
     * Delete question
     */
    @Transactional
    public void deleteQuestion(Long questionId, UUID instructorId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuizException("Question not found with id: " + questionId));
        
        Long quizId = question.getQuizId();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        Long courseId = quiz.getCourseId();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new QuizException("Course not found with id: " + courseId));
        
        validateInstructorOwnership(course, instructorId);
        
        if (quiz.getStatus() == Quiz.QuizStatus.PUBLISHED) {
            throw new QuizException("Cannot delete questions from a published quiz");
        }
        
        List<Option> options = optionRepository.findByQuestionId(questionId);
        optionRepository.deleteAll(options);
        questionRepository.delete(question);
        
        updateQuizTotalMarks(quizId);
        
        log.info("Deleted question with id: {} by instructor: {}", questionId, instructorId);
    }

    /**
     * Get student quiz (without correct answers)
     */
    @Transactional(readOnly = true)
    public StudentQuizResponse getStudentQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        if (quiz.getStatus() != Quiz.QuizStatus.PUBLISHED) {
            throw new QuizException("Quiz is not published");
        }
        
        return mapToStudentQuizResponse(quiz);
    }

    /**
     * Get student quiz with questions (without correct answers)
     */
    @Transactional(readOnly = true)
    public StudentQuizResponse getStudentQuizWithQuestions(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        if (quiz.getStatus() != Quiz.QuizStatus.PUBLISHED) {
            throw new QuizException("Quiz is not published");
        }
        
        StudentQuizResponse response = mapToStudentQuizResponse(quiz);
        
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
        List<StudentQuestionResponse> questionResponses = questions.stream()
                .map(this::mapToStudentQuestionResponse)
                .collect(Collectors.toList());
        
        response.setQuestions(questionResponses);
        
        return response;
    }

    // Helper methods

    private void validateQuizRequest(QuizRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new QuizException("Quiz title is required");
        }
        if (request.getCourseId() == null) {
            throw new QuizException("Course ID is required");
        }
        if (request.getPassingScore() != null && request.getPassingScore() < 0) {
            throw new QuizException("Passing score must be non-negative");
        }
        if (request.getTimeLimitMinutes() != null && request.getTimeLimitMinutes() <= 0) {
            throw new QuizException("Time limit must be greater than 0");
        }
        if (request.getMaxAttempts() != null && request.getMaxAttempts() <= 0) {
            throw new QuizException("Max attempts must be greater than 0");
        }
    }

    private void validateQuestionRequest(QuestionRequest request) {
        if (request.getQuestionText() == null || request.getQuestionText().trim().isEmpty()) {
            throw new QuizException("Question text is required");
        }
        if (request.getQuestionType() == null || request.getQuestionType().trim().isEmpty()) {
            throw new QuizException("Question type is required");
        }
        if (request.getMarks() == null || request.getMarks() <= 0) {
            throw new QuizException("Marks must be greater than 0");
        }
    }

    private void validateInstructorOwnership(Course course, UUID instructorId) {
        if (!course.getInstructorId().equals(instructorId)) {
            throw new QuizException("Instructor does not own this course");
        }
    }

    private void updateQuizTotalMarks(Long quizId) {
        List<Question> questions = questionRepository.findByQuizId(quizId);
        int totalMarks = questions.stream()
                .mapToInt(Question::getMarks)
                .sum();
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        quiz.setTotalMarks(totalMarks);
        quizRepository.save(quiz);
    }

    private QuizResponse mapToQuizResponse(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .courseId(quiz.getCourseId())
                .sectionId(quiz.getSectionId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .instructions(quiz.getInstructions())
                .status(quiz.getStatus().name())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .maxAttempts(quiz.getMaxAttempts())
                .passingScore(quiz.getPassingScore())
                .totalMarks(quiz.getTotalMarks())
                .createdBy(quiz.getCreatedBy())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    private StudentQuizResponse mapToStudentQuizResponse(Quiz quiz) {
        return StudentQuizResponse.builder()
                .id(quiz.getId())
                .courseId(quiz.getCourseId())
                .sectionId(quiz.getSectionId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .instructions(quiz.getInstructions())
                .status(quiz.getStatus().name())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .maxAttempts(quiz.getMaxAttempts())
                .passingScore(quiz.getPassingScore())
                .totalMarks(quiz.getTotalMarks())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    private QuestionResponse mapToQuestionResponse(Question question) {
        List<Option> options = optionRepository.findByQuestionIdOrderByOrderIndexAsc(question.getId());
        List<OptionResponse> optionResponses = options.stream()
                .map(this::mapToOptionResponse)
                .collect(Collectors.toList());
        
        return QuestionResponse.builder()
                .id(question.getId())
                .quizId(question.getQuizId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .marks(question.getMarks())
                .orderIndex(question.getOrderIndex())
                .explanation(question.getExplanation())
                .options(optionResponses)
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }

    private StudentQuestionResponse mapToStudentQuestionResponse(Question question) {
        List<Option> options = optionRepository.findByQuestionIdOrderByOrderIndexAsc(question.getId());
        List<StudentOptionResponse> optionResponses = options.stream()
                .map(this::mapToStudentOptionResponse)
                .collect(Collectors.toList());
        
        return StudentQuestionResponse.builder()
                .id(question.getId())
                .quizId(question.getQuizId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .marks(question.getMarks())
                .orderIndex(question.getOrderIndex())
                .explanation(question.getExplanation())
                .options(optionResponses)
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }

    private OptionResponse mapToOptionResponse(Option option) {
        return OptionResponse.builder()
                .id(option.getId())
                .questionId(option.getQuestionId())
                .optionText(option.getOptionText())
                .isCorrect(option.getIsCorrect())
                .orderIndex(option.getOrderIndex())
                .createdAt(option.getCreatedAt())
                .updatedAt(option.getUpdatedAt())
                .build();
    }

    private StudentOptionResponse mapToStudentOptionResponse(Option option) {
        return StudentOptionResponse.builder()
                .id(option.getId())
                .questionId(option.getQuestionId())
                .optionText(option.getOptionText())
                .orderIndex(option.getOrderIndex())
                .createdAt(option.getCreatedAt())
                .updatedAt(option.getUpdatedAt())
                .build();
    }
}
