package com.lms.courseservice.service;

import com.lms.courseservice.dto.CourseListData;
import com.lms.courseservice.dto.CourseRatingSummary;
import com.lms.courseservice.dto.CourseListResponse;
import com.lms.courseservice.dto.FeaturedCourseResponse;
import com.lms.courseservice.dto.SortOption;
import com.lms.courseservice.dto.TrendingCourseResponse;
import com.lms.courseservice.dto.TrendingCoursesResponse;
import com.lms.courseservice.dto.TrendingResponseData;
import com.lms.courseservice.dto.TrendingPagination;
import com.lms.courseservice.entity.Course;
import com.lms.courseservice.entity.Enrollment;
import com.lms.courseservice.entity.Lecture;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.repository.CourseSpecifications;
import com.lms.courseservice.repository.EnrollmentRepository;
import com.lms.courseservice.exception.EnrollmentException;
import com.lms.courseservice.repository.LectureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lms.courseservice.entity.CoursePreview;
import com.lms.courseservice.entity.Section;
import com.lms.courseservice.repository.CoursePreviewRepository;
import com.lms.courseservice.repository.SectionRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final LectureRepository lectureRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    private final CoursePreviewRepository coursePreviewRepository;
    private final SectionRepository sectionRepository;

    private final ReviewRatingClient reviewRatingClient;

    public Course createCourse(Course course) {
        if (course.getStatus() == null || course.getStatus().isBlank()) {
            course.setStatus("DRAFT");
        }
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Map<String, Object> getCourseStats() {
        List<Course> courses = courseRepository.findAll();
        long published = courses.stream().filter(course -> "PUBLISHED".equalsIgnoreCase(course.getStatus())).count();
        long draft = courses.stream().filter(course -> "DRAFT".equalsIgnoreCase(course.getStatus())).count();
        long archived = courses.stream().filter(course -> "ARCHIVED".equalsIgnoreCase(course.getStatus())).count();
        return Map.of(
                "totalCourses", courses.size(),
                "publishedCourses", published,
                "draftCourses", draft,
                "archivedCourses", archived);
    }

    public List<FeaturedCourseResponse> getFeaturedCourses(int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 12));
        List<FeaturedCandidate> candidates = courseRepository.findByStatusIgnoreCase("PUBLISHED").stream()
            .map(course -> new FeaturedCandidate(
                course,
                reviewRatingClient.getCourseRating(course.getId()),
                enrollmentRepository.countByCourseId(course.getId())))
            .toList();

        long maxEnrollments = candidates.stream().mapToLong(FeaturedCandidate::enrollments).max().orElse(0L);
        long maxSearches = candidates.stream().mapToLong(candidate -> valueOrZero(candidate.course().getSearchCount())).max().orElse(0L);
        long maxViews = candidates.stream().mapToLong(candidate -> valueOrZero(candidate.course().getViewCount())).max().orElse(0L);

        return candidates.stream()
            .map(candidate -> toFeaturedCourse(candidate, maxEnrollments, maxSearches, maxViews))
            .sorted(Comparator.comparingDouble(FeaturedCourseResponse::getFeaturedScore).reversed()
                .thenComparing(Comparator.comparingDouble(FeaturedCourseResponse::getRating).reversed())
                .thenComparing(Comparator.comparingLong(FeaturedCourseResponse::getStudents).reversed())
                .thenComparing(FeaturedCourseResponse::getId))
            .limit(limit)
            .toList();
    }

    @Transactional
    public void trackImpression(Long courseId, String source) {
        int updated = "SEARCH".equalsIgnoreCase(source)
            ? courseRepository.incrementSearchAndViewCount(courseId)
            : courseRepository.incrementViewCount(courseId);
        if (updated == 0) {
            throw new RuntimeException("Course not found with id: " + courseId);
        }
    }

    private FeaturedCourseResponse toFeaturedCourse(
        FeaturedCandidate candidate,
        long maxEnrollments,
        long maxSearches,
        long maxViews) {
        Course course = candidate.course();
        CourseRatingSummary ratingSummary = candidate.ratingSummary();
        double averageRating = ratingSummary.getAverageRating() == null ? 0.0 : ratingSummary.getAverageRating();
        long totalReviews = valueOrZero(ratingSummary.getTotalReviews());
        long searchCount = valueOrZero(course.getSearchCount());
        long viewCount = valueOrZero(course.getViewCount());
        double reviewConfidence = Math.min(1.0, Math.log1p(totalReviews) / Math.log1p(20));
        double score = (Boolean.TRUE.equals(course.getPremium()) ? 20.0 : 0.0)
            + (averageRating / 5.0 * 35.0 * reviewConfidence)
            + normalizedLogScore(candidate.enrollments(), maxEnrollments, 25.0)
            + normalizedLogScore(searchCount, maxSearches, 15.0)
            + normalizedLogScore(viewCount, maxViews, 5.0);

        return FeaturedCourseResponse.builder()
            .id(course.getId())
            .title(course.getTitle())
            .subtitle(course.getSubtitle())
            .description(course.getDescription())
            .category(course.getCategory())
            .level(course.getLevel())
            .language(course.getLanguage())
            .price(course.getPrice())
            .thumbnail(course.getThumbnail())
            .instructorId(course.getInstructorId())
            .status(course.getStatus())
            .premium(Boolean.TRUE.equals(course.getPremium()))
            .students(candidate.enrollments())
            .rating(Math.round(averageRating * 10.0) / 10.0)
            .totalReviews(totalReviews)
            .searchCount(searchCount)
            .viewCount(viewCount)
            .featuredScore(Math.round(score * 100.0) / 100.0)
            .tag(Boolean.TRUE.equals(course.getPremium()) ? "Premium" : averageRating >= 4.5 ? "Top Rated" : "Popular")
            .build();
    }

    private double normalizedLogScore(long value, long maximum, double weight) {
        return maximum == 0 ? 0.0 : Math.log1p(value) / Math.log1p(maximum) * weight;
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private record FeaturedCandidate(Course course, CourseRatingSummary ratingSummary, long enrollments) {
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    public Course updateCourse(Long id, Course updatedCourse) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));

        if (updatedCourse.getTitle() != null)
            course.setTitle(updatedCourse.getTitle());

        if (updatedCourse.getSubtitle() != null)
            course.setSubtitle(updatedCourse.getSubtitle());

        if (updatedCourse.getDescription() != null)
            course.setDescription(updatedCourse.getDescription());

        if (updatedCourse.getCategory() != null)
            course.setCategory(updatedCourse.getCategory());

        if (updatedCourse.getLevel() != null)
            course.setLevel(updatedCourse.getLevel());

        if (updatedCourse.getLanguage() != null)
            course.setLanguage(updatedCourse.getLanguage());

        if (updatedCourse.getPrice() != null)
            course.setPrice(updatedCourse.getPrice());

        if (updatedCourse.getThumbnail() != null)
            course.setThumbnail(updatedCourse.getThumbnail());

        if (updatedCourse.getStatus() != null)
            course.setStatus(updatedCourse.getStatus());

        if (updatedCourse.getInstructorId() != null)
            course.setInstructorId(updatedCourse.getInstructorId());

        if (updatedCourse.getPremium() != null)
            course.setPremium(updatedCourse.getPremium());

        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        // 1. Check if course exists
        getCourseById(id);

        // 2. Delete Course Previews
        List<CoursePreview> previews = coursePreviewRepository.findByCourseId(id);
        if (!previews.isEmpty()) {
            coursePreviewRepository.deleteAll(previews);
        }

        // 3. Delete Enrollments
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(id);
        if (!enrollments.isEmpty()) {
            enrollmentRepository.deleteAll(enrollments);
        }

        // 4. Delete Sections and their Lectures
        List<Section> sections = sectionRepository.findByCourseId(id);
        for (Section section : sections) {
            List<Lecture> lectures = lectureRepository.findBySectionId(section.getId());
            if (!lectures.isEmpty()) {
                lectureRepository.deleteAll(lectures);
            }
        }
        if (!sections.isEmpty()) {
            sectionRepository.deleteAll(sections);
        }

        // 5. Finally delete the course
        courseRepository.deleteById(id);
    }

    public Lecture enableLecturePreview(Long courseId, Long lectureId) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));

        if (!lecture.getSection().getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Lecture does not belong to this course");
        }

        lecture.setPreviewEnabled(true);

        return lectureRepository.save(lecture);
    }

    // 🔥 FIXED → UUID
    // 🔥 FIXED → UUID
    public List<UUID> getStudents(Long courseId) {

        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(Enrollment::getStudentId)
                .toList();
    }

    public List<com.lms.courseservice.dto.EnrolledStudentInfo> getEnrolledStudentDetails(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(e -> new com.lms.courseservice.dto.EnrolledStudentInfo(
                        e.getStudentId(),
                        e.getStudentName() != null ? e.getStudentName() : (e.getStudentId() != null ? "Student " + e.getStudentId().toString().substring(0, 8) : "Student"),
                        e.getEnrolledAt() != null ? e.getEnrolledAt() : java.time.LocalDateTime.now(),
                        e.getStatus() != null ? e.getStatus() : "Active",
                        e.getProgress() != null ? e.getProgress() : 0.0
                ))
                .toList();
    }

    // Enroll student in course (free or paid)
    public void enrollFreeCourse(Long courseId, UUID userId) {
        // Verify course exists
        getCourseById(courseId);
        createEnrollment(courseId, userId);
    }

    public void enrollAfterPayment(Long courseId, UUID userId) {
        createEnrollment(courseId, userId);
    }

    private void createEnrollment(Long courseId, UUID userId) {

        if (enrollmentRepository.existsByStudentIdAndCourseId(userId, courseId)) {
            throw new EnrollmentException("Student is already enrolled.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(userId);

        enrollmentRepository.save(enrollment);
    }

    public TrendingCoursesResponse getTrendingCourses(int page, int size, String category, String level) {
        // Validate pagination parameters
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size < 1 || size > 50) {
            throw new IllegalArgumentException("Page size must be between 1 and 50");
        }

        // Fetch published courses with optional filters
        List<Course> publishedCourses = courseRepository.findByStatusIgnoreCase("PUBLISHED");
        
        // Apply filters if provided
        if (category != null && !category.isBlank()) {
            publishedCourses = publishedCourses.stream()
                .filter(course -> category.equalsIgnoreCase(course.getCategory()))
                .collect(Collectors.toList());
        }
        
        if (level != null && !level.isBlank()) {
            publishedCourses = publishedCourses.stream()
                .filter(course -> level.equalsIgnoreCase(course.getLevel()))
                .collect(Collectors.toList());
        }

        // Filter courses with required fields
        publishedCourses = publishedCourses.stream()
            .filter(course -> course.getId() != null)
            .filter(course -> course.getTitle() != null && !course.getTitle().isBlank())
            .filter(course -> course.getThumbnail() != null && !course.getThumbnail().isBlank())
            .collect(Collectors.toList());

        if (publishedCourses.isEmpty()) {
            return buildEmptyResponse(page, size);
        }

        // First pass: collect all metrics to find maximum values for normalization
        List<TrendingCandidate> candidates = publishedCourses.stream()
            .map(course -> {
                CourseRatingSummary ratingSummary = reviewRatingClient.getCourseRating(course.getId());
                long enrollmentCount = enrollmentRepository.countByCourseId(course.getId());
                return new TrendingCandidate(course, ratingSummary, enrollmentCount);
            })
            .collect(Collectors.toList());

        // Find maximum values across all courses for proper normalization
        long maxSearchCount = candidates.stream()
            .mapToLong(c -> c.course().getSearchCount() != null ? c.course().getSearchCount() : 0L)
            .max().orElse(1000L);
        long maxViewCount = candidates.stream()
            .mapToLong(c -> c.course().getViewCount() != null ? c.course().getViewCount() : 0L)
            .max().orElse(1000L);
        long maxEnrollmentCount = candidates.stream()
            .mapToLong(TrendingCandidate::enrollmentCount)
            .max().orElse(100L);

        // Ensure minimum baselines to avoid division by zero
        final long finalMaxSearchCount = Math.max(maxSearchCount, 1000L);
        final long finalMaxViewCount = Math.max(maxViewCount, 1000L);
        final long finalMaxEnrollmentCount = Math.max(maxEnrollmentCount, 100L);

        // Second pass: calculate trending scores using proper normalization
        List<TrendingCourseWithScore> trendingCourses = candidates.stream()
            .map(candidate -> {
                double trendingScore = calculateTrendingScore(
                    candidate.course(), 
                    candidate.ratingSummary(), 
                    candidate.enrollmentCount(),
                    finalMaxSearchCount,
                    finalMaxViewCount,
                    finalMaxEnrollmentCount
                );
                return new TrendingCourseWithScore(
                    candidate.course(), 
                    candidate.ratingSummary(), 
                    candidate.enrollmentCount(), 
                    trendingScore
                );
            })
            .collect(Collectors.toList());

        // Sort by trending score descending
        trendingCourses.sort(Comparator.comparingDouble(TrendingCourseWithScore::trendingScore).reversed());

        // Create pagination
        int totalElements = trendingCourses.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        List<TrendingCourseWithScore> paginatedCourses = startIndex < totalElements 
            ? trendingCourses.subList(startIndex, endIndex) 
            : List.of();

        // Build response
        List<TrendingCourseResponse> courseResponses = paginatedCourses.stream()
            .map(this::toTrendingCourseResponse)
            .collect(Collectors.toList());

        TrendingPagination pagination = TrendingPagination.builder()
            .page(page)
            .size(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .hasNext(page < totalPages - 1)
            .hasPrevious(page > 0)
            .build();

        TrendingResponseData data = TrendingResponseData.builder()
            .courses(courseResponses)
            .pagination(pagination)
            .build();

        return TrendingCoursesResponse.builder()
            .success(true)
            .message("Trending courses fetched successfully")
            .data(data)
            .timestamp(java.time.Instant.now().toString())
            .build();
    }

    private double calculateTrendingScore(Course course, CourseRatingSummary ratingSummary, long enrollmentCount,
                                        long maxSearchCount, long maxViewCount, long maxEnrollmentCount) {
        // Get metrics with defaults for null values
        long searchCount = course.getSearchCount() != null ? course.getSearchCount() : 0L;
        long viewCount = course.getViewCount() != null ? course.getViewCount() : 0L;
        double rating = ratingSummary.getAverageRating() != null ? ratingSummary.getAverageRating() : 0.0;
        boolean isPremium = Boolean.TRUE.equals(course.getPremium());

        // Normalize each metric to 0-100 range using dataset maximums
        double searchScore = normalizeScore(searchCount, maxSearchCount);
        double viewScore = normalizeScore(viewCount, maxViewCount);
        double enrollmentScore = normalizeScore(enrollmentCount, maxEnrollmentCount);
        double ratingScore = (rating / 5.0) * 100.0;
        double premiumScore = isPremium ? 100.0 : 50.0;

        // Calculate weighted trending score as per PRD
        // Search Score (30%) + Enrollment Score (25%) + Rating Score (20%) + View Score (15%) + Premium Score (10%)
        double trendingScore = 
            (searchScore * 0.30) +
            (enrollmentScore * 0.25) +
            (ratingScore * 0.20) +
            (viewScore * 0.15) +
            (premiumScore * 0.10);

        return Math.round(trendingScore * 100.0) / 100.0; // Round to 2 decimal places
    }

    private double normalizeScore(long value, long maximum) {
        if (maximum == 0) return 0.0;
        return ((double) value / maximum) * 100.0;
    }

    private TrendingCourseResponse toTrendingCourseResponse(TrendingCourseWithScore trendingCourse) {
        Course course = trendingCourse.course();
        CourseRatingSummary ratingSummary = trendingCourse.ratingSummary();
        
        return TrendingCourseResponse.builder()
            .id(course.getId())
            .title(course.getTitle())
            .subtitle(course.getSubtitle())
            .thumbnail(course.getThumbnail())
            .category(course.getCategory())
            .level(course.getLevel())
            .language(course.getLanguage())
            .premium(course.getPremium())
            .price(course.getPrice())
            .rating(ratingSummary.getAverageRating() != null ? ratingSummary.getAverageRating() : 0.0)
            .enrollmentCount(trendingCourse.enrollmentCount())
            .searchCount(course.getSearchCount() != null ? course.getSearchCount() : 0L)
            .viewCount(course.getViewCount() != null ? course.getViewCount() : 0L)
            .trendingScore(trendingCourse.trendingScore())
            .build();
    }

    private TrendingCoursesResponse buildEmptyResponse(int page, int size) {
        TrendingPagination pagination = TrendingPagination.builder()
            .page(page)
            .size(size)
            .totalElements(0)
            .totalPages(0)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        TrendingResponseData data = TrendingResponseData.builder()
            .courses(List.of())
            .pagination(pagination)
            .build();

        return TrendingCoursesResponse.builder()
            .success(true)
            .message("No trending courses found")
            .data(data)
            .timestamp(java.time.Instant.now().toString())
            .build();
    }

    private record TrendingCandidate(
        Course course,
        CourseRatingSummary ratingSummary,
        long enrollmentCount
    ) {}

    private record TrendingCourseWithScore(
        Course course,
        CourseRatingSummary ratingSummary,
        long enrollmentCount,
        double trendingScore
    ) {}

    public CourseListResponse getCourseList(
            String search,
            String category,
            String level,
            String language,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean premium,
            Boolean free,
            Boolean paid,
            String sort,
            Integer page,
            Integer size) {

        // Validate pagination parameters
        int validatedPage = (page != null) ? page : 0;
        int validatedSize = (size != null) ? size : 10;

        if (validatedPage < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (validatedSize < 1 || validatedSize > 50) {
            throw new IllegalArgumentException("Size must be between 1 and 50");
        }

        // Validate sort option
        SortOption sortOption = null;
        if (sort != null && !sort.isBlank()) {
            try {
                sortOption = SortOption.valueOf(sort.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid sort option: " + sort);
            }
        }

        // Validate free and paid conflict
        if (Boolean.TRUE.equals(free) && Boolean.TRUE.equals(paid)) {
            throw new IllegalArgumentException("Free and paid filters cannot both be true");
        }

        // Validate minPrice
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("minPrice must be >= 0");
        }

        // Validate maxPrice
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("maxPrice must be >= 0");
        }

        // Validate minPrice <= maxPrice
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
        }

        // Parse comma-separated lists and convert to lowercase for case-insensitive matching
        List<String> categories = parseList(category);
        List<String> levels = parseList(level);
        List<String> languages = parseList(language);
        
        // Convert filter lists to lowercase for case-insensitive matching
        if (categories != null) {
            categories = categories.stream().map(String::toLowerCase).toList();
        }
        if (levels != null) {
            levels = levels.stream().map(String::toLowerCase).toList();
        }
        if (languages != null) {
            languages = languages.stream().map(String::toLowerCase).toList();
        }

        // Build specifications for filtering
        Specification<Course> spec = (root, query, cb) ->
                cb.equal(cb.lower(root.get("status")), "PUBLISHED".toLowerCase());

        if (search != null && !search.isBlank()) {
            spec = spec.and(CourseSpecifications.withSearch(search.toLowerCase()));
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and(CourseSpecifications.withCategories(categories));
        }

        if (levels != null && !levels.isEmpty()) {
            spec = spec.and(CourseSpecifications.withLevels(levels));
        }

        if (languages != null && !languages.isEmpty()) {
            spec = spec.and(CourseSpecifications.withLanguages(languages));
        }

        if (minPrice != null) {
            spec = spec.and(CourseSpecifications.withMinPrice(minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and(CourseSpecifications.withMaxPrice(maxPrice));
        }

        if (premium != null) {
            spec = spec.and(CourseSpecifications.withPremium(premium));
        }

        if (free != null && free) {
            spec = spec.and(CourseSpecifications.withFree(true));
        }

        if (paid != null && paid) {
            spec = spec.and(CourseSpecifications.withPaid(true));
        }

        // Execute query with appropriate method based on sort option
        Page<Course> coursePage;
        org.springframework.data.domain.Pageable pageable;

        if (sortOption == SortOption.POPULAR) {
            pageable = org.springframework.data.domain.PageRequest.of(validatedPage, validatedSize);
            String searchPattern = null;
            if (search != null && !search.isBlank()) {
                searchPattern = "%" + search.toLowerCase() + "%";
            }
            coursePage = courseRepository.findPopularCoursesWithFilters(
                "PUBLISHED",
                searchPattern,
                categories,
                levels,
                languages,
                minPrice,
                maxPrice,
                premium,
                free,
                paid,
                pageable
            );
        } else if (sortOption == SortOption.MOST_ENROLLED) {
            pageable = org.springframework.data.domain.PageRequest.of(validatedPage, validatedSize);
            String searchPattern = null;
            if (search != null && !search.isBlank()) {
                searchPattern = "%" + search.toLowerCase() + "%";
            }
            coursePage = courseRepository.findMostEnrolledCoursesWithFilters(
                "PUBLISHED",
                searchPattern,
                categories,
                levels,
                languages,
                minPrice,
                maxPrice,
                premium,
                free,
                paid,
                pageable
            );
        } else {
            // Apply dynamic sorting for other options
            org.springframework.data.domain.Sort sortObj = buildSort(sortOption);
            pageable = org.springframework.data.domain.PageRequest.of(validatedPage, validatedSize, sortObj);
            coursePage = courseRepository.findAll(spec, pageable);
        }

        // Build pagination metadata
        TrendingPagination pagination =
                TrendingPagination.builder()
                        .page(coursePage.getNumber())
                        .size(coursePage.getSize())
                        .totalElements(coursePage.getTotalElements())
                        .totalPages(coursePage.getTotalPages())
                        .hasNext(coursePage.hasNext())
                        .hasPrevious(coursePage.hasPrevious())
                        .build();

        // Build response data wrapper
        CourseListData data = CourseListData.builder()
                .courses(coursePage.getContent())
                .pagination(pagination)
                .build();

        // Build response
        return CourseListResponse.builder()
                .success(true)
                .message("Courses fetched successfully")
                .data(data)
                .timestamp(java.time.Instant.now().toString())
                .build();
    }

    private org.springframework.data.domain.Sort buildSort(SortOption sortOption) {
        if (sortOption == null) {
            return org.springframework.data.domain.Sort.unsorted();
        }

        return switch (sortOption) {
            case MOST_VIEWED -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "viewCount");
            case MOST_SEARCHED -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "searchCount");
            case PRICE_LOW_HIGH -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.ASC, "price");
            case PRICE_HIGH_LOW -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "price");
            case TITLE_AZ -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.ASC, "title");
            case TITLE_ZA -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "title");
            default -> org.springframework.data.domain.Sort.unsorted();
        };
    }
    
    private List<String> parseList(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return java.util.Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList();
    }
}