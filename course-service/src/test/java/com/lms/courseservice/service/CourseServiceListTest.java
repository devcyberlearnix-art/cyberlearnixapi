package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseListResponse;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceListTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void getCourseListWithPagination_defaultValues() {
        // Given
        Course course1 = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .price(new BigDecimal("49.99"))
                .searchCount(100L)
                .viewCount(200L)
                .build();

        Course course2 = Course.builder()
                .id(2L)
                .title("Python Course")
                .status("PUBLISHED")
                .price(new BigDecimal("39.99"))
                .searchCount(50L)
                .viewCount(150L)
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course1, course2), PageRequest.of(0, 10), 2);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCourses()).hasSize(2);
        assertThat(response.getData().getPagination()).isNotNull();
        assertThat(response.getData().getPagination().getPage()).isEqualTo(0);
        assertThat(response.getData().getPagination().getSize()).isEqualTo(10);
        assertThat(response.getData().getPagination().getTotalElements()).isEqualTo(2);
    }

    @Test
    void getCourseListWithPagination_customPageAndSize() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(1, 20), 25);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, null, 1, 20);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCourses()).hasSize(1);
        assertThat(response.getData().getPagination()).isNotNull();
        assertThat(response.getData().getPagination().getPage()).isEqualTo(1);
        assertThat(response.getData().getPagination().getSize()).isEqualTo(20);
    }

    @Test
    void getCourseListWithInvalidPage_throwsException() {
        // When/Then
        assertThatThrownBy(() -> courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, null, -1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Page must be >= 0");
    }

    @Test
    void getCourseListWithInvalidSize_throwsException() {
        // When/Then
        assertThatThrownBy(() -> courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, null, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Size must be between 1 and 50");
    }

    @Test
    void getCourseListWithSizeTooLarge_throwsException() {
        // When/Then
        assertThatThrownBy(() -> courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, null, 0, 51))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Size must be between 1 and 50");
    }

    @Test
    void getCourseListWithInvalidSortOption_throwsException() {
        // When/Then
        assertThatThrownBy(() -> courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, "INVALID_SORT", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid sort option: INVALID_SORT");
    }

    @Test
    void getCourseListWithValidSortOptions() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When/Then - Test each valid sort option
        String[] validSorts = {"MOST_VIEWED", "MOST_SEARCHED", "PRICE_LOW_HIGH", "PRICE_HIGH_LOW", "TITLE_AZ", "TITLE_ZA"};

        for (String sort : validSorts) {
            CourseListResponse response = courseService.getCourseList(
                    null, null, null, null, null, null, null, null, null, sort, null, null);
            assertThat(response.getSuccess()).isTrue();
        }
    }

    @Test
    void getCourseListWithPopularSort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .searchCount(100L)
                .viewCount(200L)
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findPopularCoursesWithFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, "POPULAR", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithMostEnrolledSort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findMostEnrolledCoursesWithFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, "MOST_ENROLLED", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithFiltersAndSort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Programming")
                .status("PUBLISHED")
                .category("programming")
                .level("beginner")
                .price(new BigDecimal("29.99"))
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                "java", "programming", "beginner", null, null, null, null, null, null,
                "MOST_VIEWED", 0, 10);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
        assertThat(response.getData().getCourses().get(0).getTitle()).isEqualTo("Java Programming");
    }

    // PHASE 1 FILTERING TESTS

    @Test
    void getCourseListWithSearchFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Programming")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                "java", null, null, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithCategoryFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .category("programming")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, "programming", null, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithMultiCategoryFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .category("programming")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, "programming,design", null, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithLevelFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .level("beginner")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, "beginner", null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithMultiLevelFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .level("beginner")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, "beginner,intermediate", null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithLanguageFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .language("english")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, "english", null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithMultiLanguageFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .language("english")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, "english,spanish", null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithMinPriceFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .price(new BigDecimal("50.00"))
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, new BigDecimal("25.00"), null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithMaxPriceFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .price(new BigDecimal("50.00"))
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, new BigDecimal("100.00"), null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithPriceRangeFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .price(new BigDecimal("50.00"))
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, new BigDecimal("25.00"), new BigDecimal("100.00"), null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithPremiumFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .premium(true)
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, true, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithFreeFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .price(BigDecimal.ZERO)
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, true, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithPaidFilter() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .price(new BigDecimal("50.00"))
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, true, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithFreeAndPaidFilters_throwsException() {
        // When/Then
        assertThatThrownBy(() -> courseService.getCourseList(
                null, null, null, null, null, null, null, true, true, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Free and paid filters cannot both be true");
    }

    @Test
    void getCourseListWithInvalidMinPrice_throwsException() {
        // When/Then
        assertThatThrownBy(() -> courseService.getCourseList(
                null, null, null, null, new BigDecimal("-10.00"), null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minPrice must be >= 0");
    }

    @Test
    void getCourseListWithInvalidMaxPrice_throwsException() {
        // When/Then
        assertThatThrownBy(() -> courseService.getCourseList(
                null, null, null, null, null, new BigDecimal("-10.00"), null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxPrice must be >= 0");
    }

    @Test
    void getCourseListWithMinPriceGreaterThanMaxPrice_throwsException() {
        // When/Then
        assertThatThrownBy(() -> courseService.getCourseList(
                null, null, null, null, new BigDecimal("100.00"), new BigDecimal("50.00"), null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("minPrice must be <= maxPrice");
    }

    @Test
    void getCourseListWithCaseInsensitiveSearch() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Programming")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When - search with different case
        CourseListResponse response = courseService.getCourseList(
                "JAVA", null, null, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithCaseInsensitiveCategory() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .category("PROGRAMMING")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When - filter with different case
        CourseListResponse response = courseService.getCourseList(
                null, "programming", null, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithEmptyResults() {
        // Given
        Page<Course> mockPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                "nonexistent", null, null, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).isEmpty();
        assertThat(response.getData().getPagination().getTotalElements()).isEqualTo(0);
    }

    // PHASE 2 SORTING TESTS

    @Test
    void getCourseListWithMostViewedSort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .viewCount(1000L)
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, "MOST_VIEWED", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithMostSearchedSort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .searchCount(500L)
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, "MOST_SEARCHED", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithPriceLowHighSort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .price(new BigDecimal("29.99"))
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, "PRICE_LOW_HIGH", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithPriceHighLowSort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .price(new BigDecimal("99.99"))
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, "PRICE_HIGH_LOW", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithTitleAZSort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Advanced Java")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, "TITLE_AZ", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithTitleZASort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Advanced Java")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, "TITLE_ZA", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    // PAGINATION TESTS

    @Test
    void getCourseListWithPageZero() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 5);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, null, 0, 10);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getPagination().getPage()).isEqualTo(0);
        assertThat(response.getData().getPagination().isHasPrevious()).isFalse();
    }

    @Test
    void getCourseListWithNextPage() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(1, 10), 25);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, null, 1, 10);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getPagination().getPage()).isEqualTo(1);
        assertThat(response.getData().getPagination().isHasPrevious()).isTrue();
        assertThat(response.getData().getPagination().isHasNext()).isTrue();
    }

    @Test
    void getCourseListWithLastPage() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(2, 10), 25);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, null, 2, 10);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getPagination().getPage()).isEqualTo(2);
        assertThat(response.getData().getPagination().isHasNext()).isFalse();
        assertThat(response.getData().getPagination().isHasPrevious()).isTrue();
    }

    @Test
    void getCourseListPaginationMetadata() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(1, 10), 35);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, null, null, null, null, null, null, 1, 10);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getPagination().getTotalElements()).isEqualTo(35);
        assertThat(response.getData().getPagination().getTotalPages()).isEqualTo(4);
        assertThat(response.getData().getPagination().isHasNext()).isTrue();
        assertThat(response.getData().getPagination().isHasPrevious()).isTrue();
    }

    // COMBINATION TESTS

    @Test
    void getCourseListWithSearchAndPopularSort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Programming")
                .status("PUBLISHED")
                .searchCount(100L)
                .viewCount(200L)
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findPopularCoursesWithFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                "java", null, null, null, null, null, null, null, null, "POPULAR", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithSearchAndMostEnrolledSort() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Programming")
                .status("PUBLISHED")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findMostEnrolledCoursesWithFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                "java", null, null, null, null, null, null, null, null, "MOST_ENROLLED", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithCategoryAndSorting() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .category("programming")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, "programming", null, null, null, null, null, null, null, "MOST_VIEWED", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithMultiFilterAndSorting() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Programming")
                .status("PUBLISHED")
                .category("programming")
                .level("beginner")
                .language("english")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                "java", "programming", "beginner", "english", null, null, null, null, null,
                "TITLE_AZ", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithPriceFilterAndPriceSorting() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Course")
                .status("PUBLISHED")
                .price(new BigDecimal("49.99"))
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, null, null, null, new BigDecimal("25.00"), new BigDecimal("100.00"), null, null, null,
                "PRICE_LOW_HIGH", null, null);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
    }

    @Test
    void getCourseListWithFilterSortAndPagination() {
        // Given
        Course course = Course.builder()
                .id(1L)
                .title("Java Programming")
                .status("PUBLISHED")
                .category("programming")
                .build();

        Page<Course> mockPage = new PageImpl<>(List.of(course), PageRequest.of(1, 5), 15);
        when(courseRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(mockPage);

        // When
        CourseListResponse response = courseService.getCourseList(
                null, "programming", null, null, null, null, null, null, null,
                "MOST_VIEWED", 1, 5);

        // Then
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getCourses()).hasSize(1);
        assertThat(response.getData().getPagination().getPage()).isEqualTo(1);
        assertThat(response.getData().getPagination().getSize()).isEqualTo(5);
    }
}