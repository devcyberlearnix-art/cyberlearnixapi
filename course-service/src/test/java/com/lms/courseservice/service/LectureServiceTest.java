package com.lms.courseservice.service;

import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.LectureRepository;
import com.lms.courseservice.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SectionService sectionService;

    @InjectMocks
    private LectureService lectureService;

    private Section testSection;
    private Lecture testLecture;

    @BeforeEach
    void setUp() {
        testSection = Section.builder()
                .id(1L)
                .title("Test Section")
                .orderIndex(1)
                .build();

        testLecture = Lecture.builder()
                .id(1L)
                .title("Test Lecture")
                .description("Test Description")
                .videoUrl("http://example.com/video.mp4")
                .duration(30)
                .orderIndex(1)
                .previewEnabled(false)
                .section(testSection)
                .build();
    }

    @Test
    void testCreateLecture() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        when(lectureRepository.findByTitleAndSectionId("Test Lecture", 1L)).thenReturn(Optional.empty());
        when(lectureRepository.save(any(Lecture.class))).thenReturn(testLecture);

        Lecture result = lectureService.createLecture(1L, testLecture);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Lecture");
        verify(lectureRepository, times(1)).save(any(Lecture.class));
    }

    @Test
    void testCreateLectureWithBlankTitle() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(testSection));

        Lecture blankLecture = Lecture.builder()
                .title("")
                .duration(30)
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> lectureService.createLecture(1L, blankLecture)
        );
    }

    @Test
    void testCreateLectureWithNullTitle() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(testSection));

        Lecture nullLecture = Lecture.builder()
                .title(null)
                .duration(30)
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> lectureService.createLecture(1L, nullLecture)
        );
    }

    @Test
    void testCreateLectureWithNegativeDuration() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(testSection));

        Lecture negativeDurationLecture = Lecture.builder()
                .title("Test Lecture")
                .duration(-10)
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> lectureService.createLecture(1L, negativeDurationLecture)
        );
    }

    @Test
    void testCreateLectureWithNegativeOrder() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(testSection));

        Lecture negativeOrderLecture = Lecture.builder()
                .title("Test Lecture")
                .duration(30)
                .orderIndex(-1)
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> lectureService.createLecture(1L, negativeOrderLecture)
        );
    }

    @Test
    void testCreateLectureDuplicateTitle() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        when(lectureRepository.findByTitleAndSectionId("Test Lecture", 1L)).thenReturn(Optional.of(testLecture));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> lectureService.createLecture(1L, testLecture)
        );
    }

    @Test
    void testUpdateLecture() {
        when(lectureRepository.findById(1L)).thenReturn(Optional.of(testLecture));
        when(lectureRepository.save(any(Lecture.class))).thenReturn(testLecture);

        Lecture updatedLecture = Lecture.builder()
                .title("Updated Lecture")
                .description("Updated Description")
                .duration(45)
                .orderIndex(2)
                .build();

        Lecture result = lectureService.updateLecture(1L, updatedLecture);

        assertThat(result).isNotNull();
        verify(lectureRepository, times(1)).save(any(Lecture.class));
    }

    @Test
    void testUpdateLectureWithBlankTitle() {
        when(lectureRepository.findById(1L)).thenReturn(Optional.of(testLecture));

        Lecture blankUpdate = Lecture.builder()
                .title("")
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> lectureService.updateLecture(1L, blankUpdate)
        );
    }

    @Test
    void testUpdateLectureWithNegativeDuration() {
        when(lectureRepository.findById(1L)).thenReturn(Optional.of(testLecture));

        Lecture negativeDurationUpdate = Lecture.builder()
                .duration(-10)
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> lectureService.updateLecture(1L, negativeDurationUpdate)
        );
    }

    @Test
    void testUpdateLectureWithNegativeOrder() {
        when(lectureRepository.findById(1L)).thenReturn(Optional.of(testLecture));

        Lecture negativeOrderUpdate = Lecture.builder()
                .orderIndex(-1)
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> lectureService.updateLecture(1L, negativeOrderUpdate)
        );
    }

    @Test
    void testDeleteLecture() {
        when(lectureRepository.findById(1L)).thenReturn(Optional.of(testLecture));
        doNothing().when(lectureRepository).delete(testLecture);

        Lecture result = lectureService.deleteLecture(1L, 1L);

        assertThat(result).isNotNull();
        verify(lectureRepository, times(1)).delete(testLecture);
    }

    @Test
    void testDeleteLectureWrongSection() {
        Section otherSection = Section.builder()
                .id(2L)
                .title("Other Section")
                .build();

        Lecture otherLecture = Lecture.builder()
                .id(1L)
                .title("Test Lecture")
                .section(otherSection)
                .build();

        when(lectureRepository.findById(1L)).thenReturn(Optional.of(otherLecture));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> lectureService.deleteLecture(1L, 1L)
        );
    }

    @Test
    void testGetLecturesBySection() {
        when(lectureRepository.findBySectionId(1L)).thenReturn(java.util.Arrays.asList(testLecture));

        var result = lectureService.getLecturesBySection(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Lecture");
    }

    @Test
    void testGetLectureById() {
        when(lectureRepository.findById(1L)).thenReturn(Optional.of(testLecture));

        Lecture result = lectureService.getLectureById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Lecture");
    }

    @Test
    void testGetLectureByIdNotFound() {
        when(lectureRepository.findById(999L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> lectureService.getLectureById(999L)
        );
    }
}
