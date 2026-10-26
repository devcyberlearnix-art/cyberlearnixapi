package com.lms.courseservice.service;

import com.lms.courseservice.dto.OptionRequest;
import com.lms.courseservice.dto.QuestionRequest;
import com.lms.courseservice.dto.QuestionResponse;
import com.lms.courseservice.dto.QuizRequest;
import com.lms.courseservice.dto.QuizResponse;
import com.lms.courseservice.dto.StudentQuizResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Option;
import com.lms.courseservice.entity.Question;
import com.lms.courseservice.entity.Quiz;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.exception.QuizException;
import com.lms.courseservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private OptionRepository optionRepository;

    @InjectMocks
    private QuizService quizService;

    private UUID testInstructorId;
    private Long testCourseId;
    private Long testSectionId;
    private Long testQuizId;
    private Long testQuestionId;
    private Long testOptionId1;
    private Long testOptionId2;
    private Course testCourse;
    private Section testSection;
    private Quiz testQuiz;
    private Question testQuestion;
    private Option testOption1;
    private Option testOption2;

    @BeforeEach
    void setUp() {
        testInstructorId = UUID.randomUUID();
        testCourseId = 1L;
        testSectionId = 100L;
        testQuizId = 10L;
        testQuestionId = 20L;
        testOptionId1 = 30L;
        testOptionId2 = 31L;

        testSection = Section.builder()
                .id(testSectionId)
                .title("Test Section")
                .orderIndex(1)
                .build();

        testCourse = Course.builder()
                .id(testCourseId)
                .title("Test Course")
                .instructorId(testInstructorId)
                .sections(new ArrayList<>(List.of(testSection)))
                .build();

        testSection.setCourse(testCourse);

        testQuiz = Quiz.builder()
                .id(testQuizId)
                .courseId(testCourseId)
                .sectionId(null)
                .title("Test Quiz")
                .description("Test Description")
                .instructions("Test Instructions")
                .status(Quiz.QuizStatus.DRAFT)
                .timeLimitMinutes(30)
                .maxAttempts(3)
                .passingScore(70)
                .totalMarks(0)
                .createdBy(testInstructorId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
                .explanation("Basic math")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createQuiz_ValidRequest_CreatesQuiz() {
        QuizRequest request = QuizRequest.builder()
                .courseId(testCourseId)
                .sectionId(testSectionId)
                .title("New Quiz")
                .description("New Description")
                .instructions("New Instructions")
                .timeLimitMinutes(45)
                .maxAttempts(5)
                .passingScore(80)
                .build();

        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        QuizResponse response = quizService.createQuiz(request, testInstructorId);

        assertNotNull(response);
        assertEquals(testQuizId, response.getId());
        assertEquals("Test Quiz", response.getTitle());
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    void createQuiz_InvalidCourse_ThrowsException() {
        QuizRequest request = QuizRequest.builder()
                .courseId(999L)
                .title("New Quiz")
                .build();

        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(QuizException.class, () -> quizService.createQuiz(request, testInstructorId));
    }

    @Test
    void createQuiz_UnauthorizedInstructor_ThrowsException() {
        UUID unauthorizedInstructorId = UUID.randomUUID();
        QuizRequest request = QuizRequest.builder()
                .courseId(testCourseId)
                .title("New Quiz")
                .build();

        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));

        assertThrows(QuizException.class, () -> quizService.createQuiz(request, unauthorizedInstructorId));
    }

    @Test
    void createQuiz_InvalidSection_ThrowsException() {
        QuizRequest request = QuizRequest.builder()
                .courseId(testCourseId)
                .sectionId(999L)
                .title("New Quiz")
                .build();

        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(QuizException.class, () -> quizService.createQuiz(request, testInstructorId));
    }

    @Test
    void createQuiz_SectionNotInCourse_ThrowsException() {
        Section otherSection = Section.builder()
                .id(200L)
                .title("Other Section")
                .orderIndex(1)
                .build();

        Course otherCourse = Course.builder()
                .id(2L)
                .title("Other Course")
                .instructorId(testInstructorId)
                .build();

        otherSection.setCourse(otherCourse);

        QuizRequest request = QuizRequest.builder()
                .courseId(testCourseId)
                .sectionId(200L)
                .title("New Quiz")
                .build();

        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(sectionRepository.findById(200L)).thenReturn(Optional.of(otherSection));

        assertThrows(QuizException.class, () -> quizService.createQuiz(request, testInstructorId));
    }

    @Test
    void updateQuiz_ValidRequest_UpdatesQuiz() {
        QuizRequest request = QuizRequest.builder()
                .courseId(testCourseId)
                .sectionId(null)
                .title("Updated Quiz")
                .description("Updated Description")
                .timeLimitMinutes(60)
                .maxAttempts(10)
                .passingScore(90)
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        QuizResponse response = quizService.updateQuiz(testQuizId, request, testInstructorId);

        assertNotNull(response);
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    void updateQuiz_InvalidQuiz_ThrowsException() {
        QuizRequest request = QuizRequest.builder()
                .title("Updated Quiz")
                .build();

        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(QuizException.class, () -> quizService.updateQuiz(999L, request, testInstructorId));
    }

    @Test
    void getQuiz_ValidId_ReturnsQuiz() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));

        QuizResponse response = quizService.getQuiz(testQuizId);

        assertNotNull(response);
        assertEquals(testQuizId, response.getId());
        assertEquals("Test Quiz", response.getTitle());
    }

    @Test
    void getQuiz_InvalidId_ThrowsException() {
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(QuizException.class, () -> quizService.getQuiz(999L));
    }

    @Test
    void deleteQuiz_ValidId_DeletesQuiz() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));

        quizService.deleteQuiz(testQuizId, testInstructorId);

        verify(quizRepository, times(1)).delete(testQuiz);
    }

    @Test
    void deleteQuiz_UnauthorizedInstructor_ThrowsException() {
        UUID unauthorizedInstructorId = UUID.randomUUID();
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));

        assertThrows(QuizException.class, () -> quizService.deleteQuiz(testQuizId, unauthorizedInstructorId));
    }

    @Test
    void publishQuiz_ValidId_PublishesQuiz() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        QuizResponse response = quizService.publishQuiz(testQuizId, testInstructorId);

        assertNotNull(response);
        assertEquals("PUBLISHED", response.getStatus());
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    void publishQuiz_NoQuestions_ThrowsException() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of());

        assertThrows(QuizException.class, () -> quizService.publishQuiz(testQuizId, testInstructorId));
    }

    @Test
    void unpublishQuiz_ValidId_UnpublishesQuiz() {
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        QuizResponse response = quizService.unpublishQuiz(testQuizId, testInstructorId);

        assertNotNull(response);
        assertEquals("DRAFT", response.getStatus());
        verify(quizRepository, times(1)).save(any(Quiz.class));
    }

    @Test
    void getQuizzesByCourse_ValidId_ReturnsQuizzes() {
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(quizRepository.findByCourseId(testCourseId)).thenReturn(List.of(testQuiz));

        List<QuizResponse> response = quizService.getQuizzesByCourse(testCourseId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testQuizId, response.get(0).getId());
    }

    @Test
    void getQuizzesBySection_ValidId_ReturnsQuizzes() {
        when(sectionRepository.findById(testSectionId)).thenReturn(Optional.of(testSection));
        when(quizRepository.findBySectionId(testSectionId)).thenReturn(List.of(testQuiz));

        List<QuizResponse> response = quizService.getQuizzesBySection(testSectionId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(testQuizId, response.get(0).getId());
    }

    @Test
    void getPublishedQuizzesByCourse_ValidId_ReturnsPublishedQuizzes() {
        testQuiz.setStatus(Quiz.QuizStatus.PUBLISHED);
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(quizRepository.findByCourseIdAndStatus(testCourseId, Quiz.QuizStatus.PUBLISHED))
                .thenReturn(List.of(testQuiz));

        List<StudentQuizResponse> response = quizService.getPublishedQuizzesByCourse(testCourseId);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void addQuestion_ValidRequest_AddsQuestion() {
        QuestionRequest request = QuestionRequest.builder()
                .questionText("What is 2+2?")
                .questionType("SINGLE_CHOICE")
                .marks(5)
                .orderIndex(1)
                .explanation("Basic math")
                .options(List.of(
                        OptionRequest.builder()
                                .optionText("4")
                                .isCorrect(true)
                                .orderIndex(1)
                                .build(),
                        OptionRequest.builder()
                                .optionText("5")
                                .isCorrect(false)
                                .orderIndex(2)
                                .build()
                ))
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);
        when(optionRepository.save(any(Option.class))).thenReturn(any(Option.class));
        when(optionRepository.findByQuestionIdOrderByOrderIndexAsc(testQuestionId)).thenReturn(List.of(testOption1, testOption2));
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        QuestionResponse response = quizService.addQuestion(testQuizId, request, testInstructorId);

        assertNotNull(response);
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    void addQuestion_InvalidQuiz_ThrowsException() {
        QuestionRequest request = QuestionRequest.builder()
                .questionText("What is 2+2?")
                .questionType("SINGLE_CHOICE")
                .marks(5)
                .build();

        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(QuizException.class, () -> quizService.addQuestion(999L, request, testInstructorId));
    }

    @Test
    void addQuestion_PublishedQuiz_ThrowsException() {
        testQuiz.setStatus(Quiz.QuizStatus.PUBLISHED);
        QuestionRequest request = QuestionRequest.builder()
                .questionText("What is 2+2?")
                .questionType("SINGLE_CHOICE")
                .marks(5)
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));

        assertThrows(QuizException.class, () -> quizService.addQuestion(testQuizId, request, testInstructorId));
    }

    @Test
    void addQuestion_NoCorrectOption_ThrowsException() {
        QuestionRequest request = QuestionRequest.builder()
                .questionText("What is 2+2?")
                .questionType("SINGLE_CHOICE")
                .marks(5)
                .options(List.of(
                        OptionRequest.builder()
                                .optionText("4")
                                .isCorrect(false)
                                .orderIndex(1)
                                .build()
                ))
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);
        when(optionRepository.save(any(Option.class))).thenReturn(any(Option.class));

        assertThrows(QuizException.class, () -> quizService.addQuestion(testQuizId, request, testInstructorId));
    }

    @Test
    void addQuestion_SingleChoiceMultipleCorrect_ThrowsException() {
        QuestionRequest request = QuestionRequest.builder()
                .questionText("What is 2+2?")
                .questionType("SINGLE_CHOICE")
                .marks(5)
                .options(List.of(
                        OptionRequest.builder()
                                .optionText("4")
                                .isCorrect(true)
                                .orderIndex(1)
                                .build(),
                        OptionRequest.builder()
                                .optionText("5")
                                .isCorrect(true)
                                .orderIndex(2)
                                .build()
                ))
                .build();

        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);
        when(optionRepository.save(any(Option.class))).thenReturn(any(Option.class));

        assertThrows(QuizException.class, () -> quizService.addQuestion(testQuizId, request, testInstructorId));
    }

    @Test
    void updateQuestion_ValidRequest_UpdatesQuestion() {
        QuestionRequest request = QuestionRequest.builder()
                .options(null)
                .questionText("Updated question")
                .questionType("MULTIPLE_CHOICE")
                .marks(10)
                .build();

        when(questionRepository.findById(testQuestionId)).thenReturn(Optional.of(testQuestion));
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of(testQuestion));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        QuestionResponse response = quizService.updateQuestion(testQuestionId, request, testInstructorId);

        assertNotNull(response);
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    void deleteQuestion_ValidId_DeletesQuestion() {
        when(questionRepository.findById(testQuestionId)).thenReturn(Optional.of(testQuestion));
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(courseRepository.findById(testCourseId)).thenReturn(Optional.of(testCourse));
        when(optionRepository.findByQuestionId(testQuestionId)).thenReturn(List.of());
        when(questionRepository.findByQuizId(testQuizId)).thenReturn(List.of());
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        quizService.deleteQuestion(testQuestionId, testInstructorId);

        verify(questionRepository, times(1)).delete(testQuestion);
    }

    @Test
    void getStudentQuiz_ValidId_ReturnsStudentQuiz() {
        testQuiz.setStatus(Quiz.QuizStatus.PUBLISHED);
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));

        var response = quizService.getStudentQuiz(testQuizId);

        assertNotNull(response);
        assertEquals(testQuizId, response.getId());
        assertEquals("Test Quiz", response.getTitle());
    }

    @Test
    void getStudentQuiz_NotPublished_ThrowsException() {
        testQuiz.setStatus(Quiz.QuizStatus.DRAFT);
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));

        assertThrows(QuizException.class, () -> quizService.getStudentQuiz(testQuizId));
    }

    @Test
    void getStudentQuizWithQuestions_ValidId_ReturnsQuizWithQuestions() {
        testQuiz.setStatus(Quiz.QuizStatus.PUBLISHED);
        when(quizRepository.findById(testQuizId)).thenReturn(Optional.of(testQuiz));
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(testQuizId)).thenReturn(List.of(testQuestion));
        when(optionRepository.findByQuestionIdOrderByOrderIndexAsc(testQuestionId)).thenReturn(List.of());

        StudentQuizResponse response = quizService.getStudentQuizWithQuestions(testQuizId);

        assertNotNull(response);
        assertEquals(testQuizId, response.getId());
        assertNotNull(response.getQuestions());
        assertEquals(1, response.getQuestions().size());
    }
}
