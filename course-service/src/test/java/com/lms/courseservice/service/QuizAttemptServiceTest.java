package com.lms.courseservice.service;

import com.lms.courseservice.dto.AnswerRequest;
import com.lms.courseservice.dto.QuizAttemptResponse;
import com.lms.courseservice.dto.QuizResultResponse;
import com.lms.courseservice.entity.*;
import com.lms.courseservice.exception.QuizException;
import com.lms.courseservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizAttemptServiceTest {

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private QuizAnswerRepository quizAnswerRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private QuizAttemptService quizAttemptService;

    private UUID testStudentId;
    private UUID otherStudentId;
    private Long testCourseId;
    private Long testQuizId;
    private Long testAttemptId;
    private Long testQuestionId;
    private Long testOptionId1;
    private Long testOptionId2;
    private Quiz testQuiz;
    private QuizAttempt testAttempt;
    private Question testQuestion;
    private Option testOption1;
    private Option testOption2;
    private QuizAnswer testAnswer;

    @BeforeEach
    void setUp() {
        testStudentId = UUID.randomUUID();
        otherStudentId = UUID.randomUUID();
        testCourseId = 1L;
        testQuizId = 10L;
        testAttemptId = 100L;
        testQuestionId = 20L;
        testOptionId1 = 30L;
        testOptionId2 = 31L;

        testQuiz = Quiz.builder()
                .id(testQuizId)
                .courseId(testCourseId)
                .title("Test Quiz")
                .status(Quiz.QuizStatus.PUBLISHED)
                .totalMarks(10)
                .passingScore(70)
                .maxAttempts(3)
                .build();

        testAttempt = QuizAttempt.builder()
                .id(testAttemptId)
                .quizId(testQuizId)
                .studentId(testStudentId)
                .attemptNumber(1)
                .status(QuizAttempt.AttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();

        testOption1 = Option.builder()
                .id(testOptionId1)
                .questionId(testQuestionId)
                .optionText("Option 1")
                .isCorrect(true)
                .orderIndex(1)
                .build();

        testOption2 = Option.builder()
                .id(testOptionId2)
                .questionId(testQuestionId)
                .optionText("Option 2")
                .isCorrect(false)
                .orderIndex(2)
                .build();

        testQuestion = Question.builder()
                .id(testQuestionId)
                .quizId(testQuizId)
                .questionText("What is 2+2?")
                .questionType(Question.QuestionType.SINGLE_CHOICE)
                .marks(5)
                .orderIndex(1)
                .build();

        testAnswer = QuizAnswer.builder()
                .id(1L)
                .attemptId(testAttemptId)
                .questionId(testQuestionId)
                .selectedOptionIds("30")
                .isCorrect(true)
                .marksObtained(5)
                .build();
    }

    @Test
    void startQuizAttempt_EnrolledStudent_StartsAttempt() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findByQuizIdAndStudentIdAndStatus(testQuizId, testStudentId, QuizAttempt.AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(quizAttemptRepository.countByQuizIdAndStudentId(testQuizId, testStudentId)).thenReturn(0L);
        when(quizAttemptRepository.findFirstByQuizIdAndStudentIdOrderByAttemptNumberDesc(testQuizId, testStudentId))
                .thenReturn(Optional.empty());
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(testAttempt);

        QuizAttemptResponse response = quizAttemptService.startQuizAttempt(testQuizId, testStudentId);

        assertNotNull(response);
        assertEquals(testAttemptId, response.getId());
        assertEquals(QuizAttempt.AttemptStatus.IN_PROGRESS.name(), response.getStatus());
        verify(quizAttemptRepository, times(1)).save(any(QuizAttempt.class));
    }

    @Test
    void startQuizAttempt_NonEnrolledStudent_ThrowsException() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(false);

        assertThrows(QuizException.class, () -> quizAttemptService.startQuizAttempt(testQuizId, testStudentId));
    }

    @Test
    void startQuizAttempt_InvalidQuiz_ThrowsException() {
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(QuizException.class, () -> quizAttemptService.startQuizAttempt(999L, testStudentId));
    }

    @Test
    void startQuizAttempt_NotPublished_ThrowsException() {
        testQuiz.setStatus(Quiz.QuizStatus.DRAFT);
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));

        assertThrows(QuizException.class, () -> quizAttemptService.startQuizAttempt(testQuizId, testStudentId));
    }

    @Test
    void startQuizAttempt_ExistingInProgressAttempt_ThrowsException() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findByQuizIdAndStudentIdAndStatus(testQuizId, testStudentId, QuizAttempt.AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.of(testAttempt));

        assertThrows(QuizException.class, () -> quizAttemptService.startQuizAttempt(testQuizId, testStudentId));
    }

    @Test
    void startQuizAttempt_MaxAttemptsReached_ThrowsException() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findByQuizIdAndStudentIdAndStatus(testQuizId, testStudentId, QuizAttempt.AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(quizAttemptRepository.countByQuizIdAndStudentId(testQuizId, testStudentId)).thenReturn(3L);

        assertThrows(QuizException.class, () -> quizAttemptService.startQuizAttempt(testQuizId, testStudentId));
    }

    @Test
    void getCurrentAttempt_ValidAttempt_ReturnsAttempt() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findByQuizIdAndStudentIdAndStatus(testQuizId, testStudentId, QuizAttempt.AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.of(testAttempt));

        QuizAttemptResponse response = quizAttemptService.getCurrentAttempt(testQuizId, testStudentId);

        assertNotNull(response);
        assertEquals(testAttemptId, response.getId());
    }

    @Test
    void getCurrentAttempt_NoActiveAttempt_ThrowsException() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findByQuizIdAndStudentIdAndStatus(testQuizId, testStudentId, QuizAttempt.AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());

        assertThrows(QuizException.class, () -> quizAttemptService.getCurrentAttempt(testQuizId, testStudentId));
    }

    @Test
    void submitAnswer_ValidAnswer_SubmitsAnswer() {
        AnswerRequest request = AnswerRequest.builder()
                .questionId(testQuestionId)
                .selectedOptionIds(List.of(testOptionId1))
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findById(testQuestionId)).thenReturn(Optional.of(testQuestion));
        when(quizAnswerRepository.existsByAttemptIdAndQuestionId(testAttemptId, testQuestionId)).thenReturn(false);
        when(optionRepository.findByQuestionId(testQuestionId)).thenReturn(List.of(testOption1, testOption2));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(testAnswer);

        QuizAttemptResponse response = quizAttemptService.submitAnswer(testQuizId, testAttemptId, request, testStudentId);

        assertNotNull(response);
        verify(quizAnswerRepository, times(1)).save(any(QuizAnswer.class));
    }

    @Test
    void submitAnswer_InvalidQuestion_ThrowsException() {
        AnswerRequest request = AnswerRequest.builder()
                .questionId(999L)
                .selectedOptionIds(List.of(testOptionId1))
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(QuizException.class, () -> quizAttemptService.submitAnswer(testQuizId, testAttemptId, request, testStudentId));
    }

    @Test
    void submitAnswer_QuestionNotInQuiz_ThrowsException() {
        Question otherQuestion = Question.builder()
                .id(999L)
                .quizId(99L)
                .questionText("Other question")
                .build();

        AnswerRequest request = AnswerRequest.builder()
                .questionId(999L)
                .selectedOptionIds(List.of(testOptionId1))
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findById(999L)).thenReturn(Optional.of(otherQuestion));

        assertThrows(QuizException.class, () -> quizAttemptService.submitAnswer(testQuizId, testAttemptId, request, testStudentId));
    }

    @Test
    void submitAnswer_AlreadySubmitted_ThrowsException() {
        AnswerRequest request = AnswerRequest.builder()
                .questionId(testQuestionId)
                .selectedOptionIds(List.of(testOptionId1))
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findById(testQuestionId)).thenReturn(Optional.of(testQuestion));
        when(quizAnswerRepository.existsByAttemptIdAndQuestionId(testAttemptId, testQuestionId)).thenReturn(true);

        assertThrows(QuizException.class, () -> quizAttemptService.submitAnswer(testQuizId, testAttemptId, request, testStudentId));
    }

    @Test
    void submitAnswer_NotOwner_ThrowsException() {
        AnswerRequest request = AnswerRequest.builder()
                .questionId(testQuestionId)
                .selectedOptionIds(List.of(testOptionId1))
                .build();

        QuizAttempt otherAttempt = QuizAttempt.builder()
                .id(testAttemptId)
                .quizId(testQuizId)
                .studentId(otherStudentId)
                .attemptNumber(1)
                .status(QuizAttempt.AttemptStatus.IN_PROGRESS)
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(otherAttempt));

        assertThrows(QuizException.class, () -> quizAttemptService.submitAnswer(testQuizId, testAttemptId, request, testStudentId));
    }

    @Test
    void submitAnswer_SubmittedAttempt_ThrowsException() {
        AnswerRequest request = AnswerRequest.builder()
                .questionId(testQuestionId)
                .selectedOptionIds(List.of(testOptionId1))
                .build();

        testAttempt.setStatus(QuizAttempt.AttemptStatus.SUBMITTED);

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));

        assertThrows(QuizException.class, () -> quizAttemptService.submitAnswer(testQuizId, testAttemptId, request, testStudentId));
    }

    @Test
    void submitQuizAttempt_ValidAttempt_SubmitsAndGrades() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizAnswerRepository.findByAttemptId(testAttemptId)).thenReturn(List.of(testAnswer));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(testAnswer);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(testAttempt);

        QuizAttemptResponse response = quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId);

        assertNotNull(response);
        verify(quizAttemptRepository, times(1)).save(any(QuizAttempt.class));
    }

    @Test
    void submitQuizAttempt_AlreadySubmitted_ThrowsException() {
        testAttempt.setStatus(QuizAttempt.AttemptStatus.SUBMITTED);

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));

        assertThrows(QuizException.class, () -> quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId));
    }

    @Test
    void submitQuizAttempt_NotOwner_ThrowsException() {
        QuizAttempt otherAttempt = QuizAttempt.builder()
                .id(testAttemptId)
                .quizId(testQuizId)
                .studentId(otherStudentId)
                .attemptNumber(1)
                .status(QuizAttempt.AttemptStatus.IN_PROGRESS)
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(otherAttempt));

        assertThrows(QuizException.class, () -> quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId));
    }

    @Test
    void submitQuizAttempt_NoQuestions_ThrowsException() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of());

        assertThrows(QuizException.class, () -> quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId));
    }

    @Test
    void getAttemptResult_ValidAttempt_ReturnsResult() {
        testAttempt.setStatus(QuizAttempt.AttemptStatus.GRADED);
        testAttempt.setScore(5);
        testAttempt.setPercentage(50);
        testAttempt.setPassed(false);
        testAttempt.setSubmittedAt(LocalDateTime.now());

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(quizAnswerRepository.findByAttemptId(testAttemptId)).thenReturn(List.of(testAnswer));
        when(questionRepository.findById(testQuestionId)).thenReturn(Optional.of(testQuestion));

        QuizResultResponse response = quizAttemptService.getAttemptResult(testQuizId, testAttemptId, testStudentId);

        assertNotNull(response);
        assertEquals(testAttemptId, response.getAttemptId());
        assertEquals(5, response.getScore());
        assertEquals(50, response.getPercentage());
        assertEquals(false, response.getPassed());
    }

    @Test
    void getAttemptResult_InProgressAttempt_ThrowsException() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));

        assertThrows(QuizException.class, () -> quizAttemptService.getAttemptResult(testQuizId, testAttemptId, testStudentId));
    }

    @Test
    void getAttemptResult_NotOwner_ThrowsException() {
        QuizAttempt otherAttempt = QuizAttempt.builder()
                .id(testAttemptId)
                .quizId(testQuizId)
                .studentId(otherStudentId)
                .attemptNumber(1)
                .status(QuizAttempt.AttemptStatus.GRADED)
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(otherAttempt));

        assertThrows(QuizException.class, () -> quizAttemptService.getAttemptResult(testQuizId, testAttemptId, testStudentId));
    }

    @Test
    void getStudentAttempts_ValidQuiz_ReturnsAttempts() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findByQuizIdAndStudentId(testQuizId, testStudentId)).thenReturn(List.of(testAttempt));

        List<QuizAttemptResponse> response = quizAttemptService.getStudentAttempts(testQuizId, testStudentId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testAttemptId, response.get(0).getId());
    }

    @Test
    void getStudentAttempts_NonEnrolled_ThrowsException() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(false);

        assertThrows(QuizException.class, () -> quizAttemptService.getStudentAttempts(testQuizId, testStudentId));
    }

    // Grading Tests

    @Test
    void grading_SingleChoiceCorrect_GradesCorrectly() {
        testAnswer.setSelectedOptionIds("30");
        testOption1.setIsCorrect(true);
        testOption2.setIsCorrect(false);

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizAnswerRepository.findByAttemptId(testAttemptId)).thenReturn(List.of(testAnswer));
        when(optionRepository.findByQuestionId(testQuestionId)).thenReturn(List.of(testOption1, testOption2));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(testAnswer);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(testAttempt);

        quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId);

        assertTrue(testAnswer.getIsCorrect());
        assertEquals(5, testAnswer.getMarksObtained());
    }

    @Test
    void grading_SingleChoiceIncorrect_GradesIncorrectly() {
        testAnswer.setSelectedOptionIds("31");
        testOption1.setIsCorrect(true);
        testOption2.setIsCorrect(false);

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizAnswerRepository.findByAttemptId(testAttemptId)).thenReturn(List.of(testAnswer));
        when(optionRepository.findByQuestionId(testQuestionId)).thenReturn(List.of(testOption1, testOption2));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(testAnswer);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(testAttempt);

        quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId);

        assertFalse(testAnswer.getIsCorrect());
        assertEquals(0, testAnswer.getMarksObtained());
    }

    @Test
    void grading_MultipleChoiceExactMatch_GradesCorrectly() {
        testQuestion.setQuestionType(Question.QuestionType.MULTIPLE_CHOICE);
        testOption1.setIsCorrect(true);
        testOption2.setIsCorrect(true);
        testAnswer.setSelectedOptionIds("30,31");

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizAnswerRepository.findByAttemptId(testAttemptId)).thenReturn(List.of(testAnswer));
        when(optionRepository.findByQuestionId(testQuestionId)).thenReturn(List.of(testOption1, testOption2));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(testAnswer);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(testAttempt);

        quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId);

        assertTrue(testAnswer.getIsCorrect());
        assertEquals(5, testAnswer.getMarksObtained());
    }

    @Test
    void grading_MultipleChoicePartialMatch_GradesIncorrectly() {
        testQuestion.setQuestionType(Question.QuestionType.MULTIPLE_CHOICE);
        testOption1.setIsCorrect(true);
        testOption2.setIsCorrect(true);
        testAnswer.setSelectedOptionIds("30");

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizAnswerRepository.findByAttemptId(testAttemptId)).thenReturn(List.of(testAnswer));
        when(optionRepository.findByQuestionId(testQuestionId)).thenReturn(List.of(testOption1, testOption2));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(testAnswer);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(testAttempt);

        quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId);

        assertFalse(testAnswer.getIsCorrect());
        assertEquals(0, testAnswer.getMarksObtained());
    }

    @Test
    void grading_TrueFalseCorrect_GradesCorrectly() {
        testQuestion.setQuestionType(Question.QuestionType.TRUE_FALSE);
        testOption1.setIsCorrect(true);
        testOption2.setIsCorrect(false);
        testAnswer.setSelectedOptionIds("30");

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizAnswerRepository.findByAttemptId(testAttemptId)).thenReturn(List.of(testAnswer));
        when(optionRepository.findByQuestionId(testQuestionId)).thenReturn(List.of(testOption1, testOption2));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(testAnswer);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(testAttempt);

        quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId);

        assertTrue(testAnswer.getIsCorrect());
        assertEquals(5, testAnswer.getMarksObtained());
    }

    @Test
    void grading_CalculatesTotalScoreAndPercentage() {
        testQuiz.setTotalMarks(10);
        testAnswer.setSelectedOptionIds("30");
        testOption1.setIsCorrect(true);
        testOption2.setIsCorrect(false);

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizAnswerRepository.findByAttemptId(testAttemptId)).thenReturn(List.of(testAnswer));
        when(optionRepository.findByQuestionId(testQuestionId)).thenReturn(List.of(testOption1, testOption2));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(testAnswer);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(testAttempt);

        quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId);

        assertEquals(5, testAttempt.getScore());
        assertEquals(50, testAttempt.getPercentage());
    }

    @Test
    void grading_PassFailBasedOnPassingScore() {
        testQuiz.setTotalMarks(10);
        testQuiz.setPassingScore(40);
        testAnswer.setSelectedOptionIds("30");
        testOption1.setIsCorrect(true);
        testOption2.setIsCorrect(false);

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(testStudentId, testCourseId)).thenReturn(true);
        when(quizAttemptRepository.findById(testAttemptId)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizAnswerRepository.findByAttemptId(testAttemptId)).thenReturn(List.of(testAnswer));
        when(optionRepository.findByQuestionId(testQuestionId)).thenReturn(List.of(testOption1, testOption2));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(testAnswer);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenReturn(testAttempt);

        quizAttemptService.submitQuizAttempt(testQuizId, testAttemptId, testStudentId);

        assertTrue(testAttempt.getPassed());
    }
}
