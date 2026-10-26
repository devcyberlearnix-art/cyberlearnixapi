package com.lms.seeder.service;

import com.lms.seeder.generator.CloudinaryThumbnailGenerator;
import com.lms.seeder.generator.CourseGenerator;
import com.lms.seeder.generator.UserGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetSeeder {

    private final Map<String, JdbcTemplate> jdbcTemplates;
    private final CloudinaryThumbnailGenerator thumbnailGenerator;

    @Value("${seeder.students.count:75}")
    private int studentCount;

    @Value("${seeder.instructors.count:15}")
    private int instructorCount;

    @Value("${seeder.courses.count:400}")
    private int courseCount;

    @Value("${seeder.sections.per-course:4}")
    private int sectionsPerCourse;

    @Value("${seeder.lectures.per-section:4}")
    private int lecturesPerSection;

    @Value("${seeder.enrollments.count:500}")
    private int enrollmentCount;

    @Value("${seeder.wishlist.count:200}")
    private int wishlistCount;

    @Value("${seeder.cart.count:100}")
    private int cartCount;

    @Value("${seeder.reviews.count:250}")
    private int reviewCount;

    @Value("${seeder.coupons.count:15}")
    private int couponCount;

    @Value("${seeder.orders.count:100}")
    private int orderCount;

    @Value("${seeder.payments.count:100}")
    private int paymentCount;

    @Value("${seeder.notifications.count:200}")
    private int notificationCount;

    // Store generated IDs for cross-referencing
    private List<UUID> studentIds = new ArrayList<>();
    private List<UUID> instructorIds = new ArrayList<>();
    private List<Long> courseIds = new ArrayList<>();
    private Map<UUID, String> studentNames = new HashMap<>();

    public void seedAll() {
        log.info("========================================");
        log.info("Starting dataset seeding");
        log.info("========================================");
        
        try {
            // Phase 1: Seed Users (Students and Instructors)
            seedUsers();
            
            // Phase 2: Seed Instructors
            seedInstructors();
            
            // Phase 3: Seed Courses with Thumbnails
            seedCourses();
            
            // Phase 4: Seed Sections
            seedSections();
            
            // Phase 5: Seed Lectures
            seedLectures();
            
            // Phase 6: Seed Enrollments
            seedEnrollments();
            
            // Phase 7: Seed Wishlist Items
            seedWishlistItems();
            
            // Phase 8: Seed Cart Items
            seedCartItems();
            
            // Phase 9: Seed Reviews
            seedReviews();
            
            // Phase 10: Seed Coupons
            seedCoupons();
            
            // Phase 11: Seed Orders
            seedOrders();
            
            // Phase 12: Seed Payments
            seedPayments();
            
            // Phase 13: Seed Notifications
            seedNotifications();
            
            log.info("========================================");
            log.info("Dataset seeding completed successfully!");
            log.info("========================================");
            printSummary();
            
        } catch (Exception e) {
            log.error("========================================");
            log.error("Dataset seeding failed in a phase. Data seeded up to the failure point is preserved.", e);
            log.error("========================================");
            throw e;
        }
    }

    private void seedUsers() {
        log.info("Phase 1: Seeding {} students...", studentCount);
        JdbcTemplate userJdbc = jdbcTemplates.get("user");
        
        int seeded = 0;
        for (int i = 0; i < studentCount; i++) {
            String firstName = UserGenerator.generateFirstName();
            String lastName = UserGenerator.generateLastName();
            String email = UserGenerator.generateEmail(firstName, lastName);
            
            // Idempotency: Check if user already exists by email
            Integer existing = userJdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
            
            if (existing != null && existing > 0) {
                // User exists, get their ID and add to list
                UUID existingId = userJdbc.queryForObject(
                    "SELECT id FROM users WHERE email = ?", UUID.class, email);
                if (existingId != null && !studentIds.contains(existingId)) {
                    studentIds.add(existingId);
                    String existingName = userJdbc.queryForObject(
                        "SELECT first_name || ' ' || last_name FROM users WHERE id = ?", String.class, existingId);
                    studentNames.put(existingId, existingName != null ? existingName : "Student");
                }
                continue;
            }
            
            UUID userId = UUID.randomUUID();
            String city = UserGenerator.generateCity();
            String state = UserGenerator.generateState();
            String organization = UserGenerator.generateOrganization();
            String skills = UserGenerator.generateSkills();
            String qualification = UserGenerator.generateQualification();
            String fieldOfStudy = UserGenerator.generateFieldOfStudy();
            
            userJdbc.update(
                "INSERT INTO users (id, first_name, last_name, email, password, role, status, " +
                "dob, city, state, country, organization, skills, highest_qualification, " +
                "field_of_study, preferred_language, failed_login_attempts, is_instructor_approved, " +
                "mobile_hash, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                userId, firstName, lastName, email, UserGenerator.PASSWORD_HASH,
                "STUDENT", "ACTIVE", "1990-01-01", city, state, "India",
                organization, skills, qualification, fieldOfStudy, "en",
                0, false, "", LocalDateTime.now(), LocalDateTime.now()
            );
            
            studentIds.add(userId);
            studentNames.put(userId, firstName + " " + lastName);
            seeded++;
        }
        
        log.info("Phase 1 complete: Seeded {} new students (total: {})", seeded, studentIds.size());
    }

    private void seedInstructors() {
        log.info("Phase 2: Seeding {} instructors...", instructorCount);
        JdbcTemplate userJdbc = jdbcTemplates.get("user");
        JdbcTemplate instructorJdbc = jdbcTemplates.get("instructor");
        
        // Verify instructor database connection
        String instructorDbName = instructorJdbc.queryForObject("SELECT current_database()", String.class);
        log.info("Instructor JDBC template connected to database: {}", instructorDbName);
        
        // Check if instructor table exists
        try {
            Integer tableCount = instructorJdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'instructor'", Integer.class);
            log.info("Instructor table exists check: {}", tableCount > 0 ? "YES" : "NO");
        } catch (Exception e) {
            log.error("Failed to check for instructor table: {}", e.getMessage());
        }
        
        int seeded = 0;
        for (int i = 0; i < instructorCount; i++) {
            String firstName = UserGenerator.generateFirstName();
            String lastName = UserGenerator.generateLastName();
            String email = UserGenerator.generateEmail(firstName, lastName);
            
            // Idempotency: Check if user already exists
            Integer existing = userJdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
            
            UUID userId;
            if (existing != null && existing > 0) {
                // Get existing user ID
                userId = userJdbc.queryForObject(
                    "SELECT id FROM users WHERE email = ?", UUID.class, email);
            } else {
                // Create new user
                userId = UUID.randomUUID();
                String city = UserGenerator.generateCity();
                String state = UserGenerator.generateState();
                String organization = UserGenerator.generateOrganization();
                String skills = UserGenerator.generateSkills();
                String qualification = UserGenerator.generateQualification();
                String fieldOfStudy = UserGenerator.generateFieldOfStudy();
                
                userJdbc.update(
                    "INSERT INTO users (id, first_name, last_name, email, password, role, status, " +
                    "dob, city, state, country, organization, skills, highest_qualification, " +
                    "field_of_study, preferred_language, failed_login_attempts, is_instructor_approved, " +
                    "mobile_hash, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    userId, firstName, lastName, email, UserGenerator.PASSWORD_HASH,
                    "INSTRUCTOR", "ACTIVE", "1985-01-01", city, state, "India",
                    organization, skills, qualification, fieldOfStudy, "en",
                    0, true, "", LocalDateTime.now(), LocalDateTime.now()
                );
                seeded++;
            }
            
            // Idempotency: Check if instructor record exists
            Integer existingInstructor = instructorJdbc.queryForObject(
                "SELECT COUNT(*) FROM instructor WHERE user_id = ?", Integer.class, userId);
            
            if (existingInstructor != null && existingInstructor > 0) {
                if (!instructorIds.contains(userId)) {
                    instructorIds.add(userId);
                }
                continue;
            }
            
            // Create instructor record
            UUID instructorId = UUID.randomUUID();
            instructorJdbc.update(
                "INSERT INTO instructor (id, user_id, name, email, headline, rating, verified) VALUES (?, ?, ?, ?, ?, ?, ?)",
                instructorId, userId, firstName + " " + lastName, email, "Software Development",
                4.5, true
            );
            
            // Store userId for course references (courses use user_id as instructor_id)
            if (!instructorIds.contains(userId)) {
                instructorIds.add(userId);
            }
        }
        
        log.info("Phase 2 complete: Seeded {} new instructors (total: {})", seeded, instructorIds.size());
    }

    private void seedCourses() {
        log.info("Phase 3: Seeding {} courses with thumbnails...", courseCount);
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");

        // Idempotency: Check if we already have enough courses
        Integer existingCount = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM courses", Integer.class);
        if (existingCount != null && existingCount >= courseCount) {
            log.info("Phase 3 complete: Already have {} courses (target: {}), skipping", existingCount, courseCount);
            return;
        }

        int seeded = 0;
        for (int i = 0; i < courseCount; i++) {
            CourseGenerator.CourseData courseData = CourseGenerator.generateCourseData();
            UUID instructorId = instructorIds.get(i % instructorIds.size());
            
            // Idempotency: Check if course already exists (by title + instructor)
            Integer existing = courseJdbc.queryForObject(
                "SELECT COUNT(*) FROM courses WHERE title = ? AND instructor_id = ?",
                Integer.class, courseData.title, instructorId);
            
            if (existing != null && existing > 0) {
                // Course exists, get its ID
                Long existingCourseId = courseJdbc.queryForObject(
                    "SELECT id FROM courses WHERE title = ? AND instructor_id = ? LIMIT 1",
                    Long.class, courseData.title, instructorId);
                if (existingCourseId != null && !courseIds.contains(existingCourseId)) {
                    courseIds.add(existingCourseId);
                }
                continue;
            }
            
            // Generate thumbnail - NO FALLBACKS, will fail if upload fails
            String thumbnail = thumbnailGenerator.generateThumbnail(courseData.category, (long) (i + 1));
            
            // Insert course
            courseJdbc.update(
                "INSERT INTO courses (title, subtitle, description, category, level, language, " +
                "price, thumbnail, instructor_id, status, premium, search_count, view_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                courseData.title, courseData.subtitle, courseData.description,
                courseData.category, courseData.level, courseData.language,
                courseData.price, thumbnail, instructorId, courseData.status,
                courseData.premium, courseData.searchCount, courseData.viewCount
            );
            
            // Get the generated course ID
            Long courseId = courseJdbc.queryForObject(
                "SELECT id FROM courses WHERE title = ? AND instructor_id = ? ORDER BY id DESC LIMIT 1",
                Long.class, courseData.title, instructorId);
            
            if (courseId != null) {
                courseIds.add(courseId);
                seeded++;
            }
        }
        
        log.info("Phase 3 complete: Seeded {} new courses (total: {})", seeded, courseIds.size());
    }

    private void seedSections() {
        log.info("Phase 4: Seeding sections for {} courses...", courseIds.size());
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");
        
        int seeded = 0;
        for (Long courseId : courseIds) {
            // Get course category
            String category = courseJdbc.queryForObject(
                "SELECT category FROM courses WHERE id = ?", String.class, courseId);
            
            // Idempotency: Check existing sections for this course
            Integer existingSections = courseJdbc.queryForObject(
                "SELECT COUNT(*) FROM sections WHERE course_id = ?", Integer.class, courseId);
            
            if (existingSections != null && existingSections >= sectionsPerCourse) {
                continue; // Skip if already has enough sections
            }
            
            for (int i = existingSections != null ? existingSections : 0; i < sectionsPerCourse; i++) {
                String sectionTitle = CourseGenerator.generateSectionTitle(category, i);
                
                courseJdbc.update(
                    "INSERT INTO sections (title, order_index, course_id) VALUES (?, ?, ?)",
                    sectionTitle, i + 1, courseId
                );
                seeded++;
            }
        }
        
        log.info("Phase 4 complete: Seeded {} sections", seeded);
    }

    private void seedLectures() {
        log.info("Phase 5: Seeding lectures...");
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");
        
        int seeded = 0;
        List<Long> sectionIds = courseJdbc.queryForList(
            "SELECT id FROM sections ORDER BY course_id, order_index", Long.class);
        
        for (Long sectionId : sectionIds) {
            String sectionTitle = courseJdbc.queryForObject(
                "SELECT title FROM sections WHERE id = ?", String.class, sectionId);
            
            // Idempotency: Check existing lectures for this section
            Integer existingLectures = courseJdbc.queryForObject(
                "SELECT COUNT(*) FROM lectures WHERE section_id = ?", Integer.class, sectionId);
            
            if (existingLectures != null && existingLectures >= lecturesPerSection) {
                continue;
            }
            
            for (int i = existingLectures != null ? existingLectures : 0; i < lecturesPerSection; i++) {
                String lectureTitle = CourseGenerator.generateLectureTitle(sectionTitle, i);
                String description = CourseGenerator.generateLectureDescription(lectureTitle);
                int duration = CourseGenerator.generateDuration();
                boolean previewEnabled = (i == 0); // First lecture is preview
                
                courseJdbc.update(
                    "INSERT INTO lectures (title, description, video_url, duration, order_index, " +
                    "preview_enabled, type, section_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    lectureTitle, description, "", duration, i + 1, previewEnabled, "VIDEO", sectionId
                );
                seeded++;
            }
        }
        
        log.info("Phase 5 complete: Seeded {} lectures", seeded);
    }

    private void seedEnrollments() {
        log.info("Phase 6: Seeding {} enrollments...", enrollmentCount);
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");

        // Idempotency: Check if we already have enough enrollments
        Integer existingCount = courseJdbc.queryForObject(
            "SELECT COUNT(*) FROM enrollment", Integer.class);
        if (existingCount != null && existingCount >= enrollmentCount) {
            log.info("Phase 6 complete: Already have {} enrollments (target: {}), skipping", existingCount, enrollmentCount);
            return;
        }

        int seeded = 0;
        for (int i = 0; i < enrollmentCount; i++) {
            UUID studentId = studentIds.get(i % studentIds.size());
            Long courseId = courseIds.get(i % courseIds.size());
            
            // Idempotency: Check if enrollment already exists
            Integer existing = courseJdbc.queryForObject(
                "SELECT COUNT(*) FROM enrollment WHERE student_id = ? AND course_id = ?",
                Integer.class, studentId, courseId);
            
            if (existing != null && existing > 0) {
                continue;
            }
            
            String studentName = studentNames.getOrDefault(studentId, "Student");
            double progress = Math.random() * 100;
            
            courseJdbc.update(
                "INSERT INTO enrollment (student_id, course_id, student_name, enrolled_at, " +
                "status, progress) VALUES (?, ?, ?, ?, ?, ?)",
                studentId, courseId, studentName, LocalDateTime.now(),
                "ACTIVE", progress
            );
            seeded++;
        }
        
        log.info("Phase 6 complete: Seeded {} enrollments", seeded);
    }

    private void seedWishlistItems() {
        log.info("Phase 7: Seeding {} wishlist items...", wishlistCount);
        JdbcTemplate wishlistJdbc = jdbcTemplates.get("wishlist");

        // Idempotency: Check if we already have enough wishlist items
        Integer existingCount = wishlistJdbc.queryForObject(
            "SELECT COUNT(*) FROM wishlist_items", Integer.class);
        if (existingCount != null && existingCount >= wishlistCount) {
            log.info("Phase 7 complete: Already have {} wishlist items (target: {}), skipping", existingCount, wishlistCount);
            return;
        }

        int seeded = 0;
        for (int i = 0; i < wishlistCount; i++) {
            UUID studentId = studentIds.get(i % studentIds.size());
            Long courseId = courseIds.get((i + 5) % courseIds.size()); // Offset to avoid overlap with enrollments
            
            // Idempotency: Check if item already exists
            Integer existing = wishlistJdbc.queryForObject(
                "SELECT COUNT(*) FROM wishlist_items WHERE user_id = ? AND course_id = ?",
                Integer.class, studentId.toString(), courseId);
            
            if (existing != null && existing > 0) {
                continue;
            }
            
            UUID wishlistId = UUID.randomUUID();
            wishlistJdbc.update(
                "INSERT INTO wishlist_items (id, user_id, course_id, added_at) VALUES (?, ?, ?, ?)",
                wishlistId, studentId.toString(), courseId, LocalDateTime.now()
            );
            seeded++;
        }
        
        log.info("Phase 7 complete: Seeded {} wishlist items", seeded);
    }

    private void seedCartItems() {
        log.info("Phase 8: Seeding {} cart items...", cartCount);
        JdbcTemplate cartJdbc = jdbcTemplates.get("cart");
        JdbcTemplate courseJdbc = jdbcTemplates.get("course");

        // Idempotency: Check if we already have enough cart items
        Integer existingCount = cartJdbc.queryForObject(
            "SELECT COUNT(*) FROM cart_items", Integer.class);
        if (existingCount != null && existingCount >= cartCount) {
            log.info("Phase 8 complete: Already have {} cart items (target: {}), skipping", existingCount, cartCount);
            return;
        }

        int seeded = 0;
        for (int i = 0; i < cartCount; i++) {
            UUID studentId = studentIds.get(i % studentIds.size());
            Long courseId = courseIds.get((i + 10) % courseIds.size());
            UUID instructorId = instructorIds.get(i % instructorIds.size());
            
            // Idempotency: Check if item already exists
            Integer existing = cartJdbc.queryForObject(
                "SELECT COUNT(*) FROM cart_items WHERE user_id = ? AND course_id = ?",
                Integer.class, studentId.toString(), courseId);
            
            if (existing != null && existing > 0) {
                continue;
            }
            
            // Get course details
            String courseName = courseJdbc.queryForObject(
                "SELECT title FROM courses WHERE id = ?", String.class, courseId);
            Double price = courseJdbc.queryForObject(
                "SELECT price FROM courses WHERE id = ?", Double.class, courseId);
            
            cartJdbc.update(
                "INSERT INTO cart_items (user_id, instructor_id, course_id, course_name, " +
                "price, quantity) VALUES (?, ?, ?, ?, ?, ?)",
                studentId.toString(), instructorId.toString(), courseId, courseName, price, 1
            );
            seeded++;
        }
        
        log.info("Phase 8 complete: Seeded {} cart items", seeded);
    }

    private void seedReviews() {
        log.info("Phase 9: Seeding {} reviews...", reviewCount);
        JdbcTemplate reviewJdbc = jdbcTemplates.get("review");

        // Idempotency: Check if we already have enough reviews
        Integer existingCount = reviewJdbc.queryForObject(
            "SELECT COUNT(*) FROM reviews", Integer.class);
        if (existingCount != null && existingCount >= reviewCount) {
            log.info("Phase 9 complete: Already have {} reviews (target: {}), skipping", existingCount, reviewCount);
            return;
        }

        int seeded = 0;
        for (int i = 0; i < reviewCount; i++) {
            UUID studentId = studentIds.get(i % studentIds.size());
            Long courseId = courseIds.get(i % courseIds.size());
            
            // Idempotency: Check if review already exists
            Integer existing = reviewJdbc.queryForObject(
                "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND course_id = ?",
                Integer.class, studentId, courseId);
            
            if (existing != null && existing > 0) {
                continue;
            }
            
            int rating = 3 + (int) (Math.random() * 3); // 3-5 stars
            String[] comments = {
                "Excellent course! Very well structured and easy to follow.",
                "Great content and practical examples. Highly recommended!",
                "Good course overall. Some sections could be more detailed.",
                "Fantastic instructor. Learned a lot from this course.",
                "Comprehensive coverage of the topic. Worth every penny.",
                "Clear explanations and hands-on exercises. Loved it!",
                "Perfect for beginners. Builds solid foundation.",
                "Advanced concepts explained very well. Great job!",
                "Real-world examples were very helpful. Thank you!",
                "One of the best courses I've taken on this platform."
            };
            String comment = comments[(int) (Math.random() * comments.length)];
            
            reviewJdbc.update(
                "INSERT INTO reviews (review_uuid, user_id, course_id, rating, comment, status, " +
                "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), studentId, courseId, rating, comment, "ACTIVE",
                LocalDateTime.now(), LocalDateTime.now()
            );
            seeded++;
        }
        
        log.info("Phase 9 complete: Seeded {} reviews", seeded);
    }

    private void seedCoupons() {
        log.info("Phase 10: Seeding {} coupons...", couponCount);
        JdbcTemplate couponJdbc = jdbcTemplates.get("coupon");

        // Idempotency: Check if we already have enough coupons
        Integer existingCount = couponJdbc.queryForObject(
            "SELECT COUNT(*) FROM coupons", Integer.class);
        if (existingCount != null && existingCount >= couponCount) {
            log.info("Phase 10 complete: Already have {} coupons (target: {}), skipping", existingCount, couponCount);
            return;
        }

        String[] couponCodes = {
            "WELCOME50", "SPRING20", "DEVMONTH", "CYBER15", "PYTHON30",
            "JAVA25", "AWS10", "DOCKER35", "KUBE20", "MLSTART",
            "SECURE10", "CLOUD20", "CODE15", "LEARN25", "TECH40"
        };

        String[] discountTypes = {"PERCENTAGE", "FLAT"};

        int seeded = 0;
        for (int i = 0; i < couponCount; i++) {
            String code = couponCodes[i % couponCodes.length];
            
            // Idempotency: Check if coupon already exists
            Integer existing = couponJdbc.queryForObject(
                "SELECT COUNT(*) FROM coupons WHERE code = ?", Integer.class, code);
            
            if (existing != null && existing > 0) {
                continue;
            }
            
            String discountType = discountTypes[i % discountTypes.length];
            double discountValue = discountType.equals("PERCENTAGE") ? 10 + (i * 5) : 500 + (i * 100);
            double minPurchase = 999.0;
            int usageLimit = 100 + (i * 10);
            
            couponJdbc.update(
                "INSERT INTO coupons (id, code, title, description, discount_type, discount_value, " +
                "minimum_order_amount, usage_limit, used_count, is_active, status, created_at, expiry_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), code, code + " Discount",
                "Get " + discountValue + (discountType.equals("PERCENTAGE") ? "% off" : " INR off") + " on selected courses",
                discountType, discountValue, minPurchase, usageLimit, 0, true, "ACTIVE",
                LocalDateTime.now(), LocalDateTime.now().plusMonths(6)
            );
            seeded++;
        }
        
        log.info("Phase 10 complete: Seeded {} coupons", seeded);
    }

    private void seedOrders() {
        log.info("Phase 11: Seeding {} orders...", orderCount);
        JdbcTemplate orderJdbc = jdbcTemplates.get("order");

        // Idempotency: Check if we already have enough orders
        Integer existingCount = orderJdbc.queryForObject(
            "SELECT COUNT(*) FROM orders", Integer.class);
        if (existingCount != null && existingCount >= orderCount) {
            log.info("Phase 11 complete: Already have {} orders (target: {}), skipping", existingCount, orderCount);
            return;
        }

        int seeded = 0;
        for (int i = 0; i < orderCount; i++) {
            UUID studentId = studentIds.get(i % studentIds.size());
            String orderId = UUID.randomUUID().toString();
            double totalAmount = 999.0 + (Math.random() * 5000);
            
            orderJdbc.update(
                "INSERT INTO orders (order_id, user_id, total_amount, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?)",
                orderId, studentId.toString(), totalAmount, "COMPLETED", LocalDateTime.now()
            );
            seeded++;
        }
        
        log.info("Phase 11 complete: Seeded {} orders", seeded);
    }

    private void seedPayments() {
        log.info("Phase 12: Seeding {} payments...", paymentCount);
        JdbcTemplate paymentJdbc = jdbcTemplates.get("payment");
        JdbcTemplate orderJdbc = jdbcTemplates.get("order");

        // Idempotency: Check if we already have enough payments
        Integer existingCount = paymentJdbc.queryForObject(
            "SELECT COUNT(*) FROM payments", Integer.class);
        if (existingCount != null && existingCount >= paymentCount) {
            log.info("Phase 12 complete: Already have {} payments (target: {}), skipping", existingCount, paymentCount);
            return;
        }

        int seeded = 0;
        List<String> orderIds = orderJdbc.queryForList(
            "SELECT order_id FROM orders LIMIT ?", String.class, paymentCount);
        
        for (String orderId : orderIds) {
            String userId = orderJdbc.queryForObject(
                "SELECT user_id FROM orders WHERE order_id = ?", String.class, orderId);
            double amount = orderJdbc.queryForObject(
                "SELECT total_amount FROM orders WHERE order_id = ?", Double.class, orderId);
            
            String txnId = "TXN" + System.currentTimeMillis() + (int) (Math.random() * 10000);
            
            paymentJdbc.update(
                "INSERT INTO payments (txn_id, amount, status, email, payer_user_id, " +
                "course_id, instructor_id, currency, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                txnId, amount, "SUCCESS", "user@cyberlearnix.test", userId,
                courseIds.get(seeded % courseIds.size()).toString(),
                instructorIds.get(seeded % instructorIds.size()).toString(),
                "INR", LocalDateTime.now(), LocalDateTime.now()
            );
            seeded++;
        }
        
        log.info("Phase 12 complete: Seeded {} payments", seeded);
    }

    private void seedNotifications() {
        log.info("Phase 13: Seeding {} notifications...", notificationCount);
        JdbcTemplate notificationJdbc = jdbcTemplates.get("notification");

        // Idempotency: Check if we already have enough notifications
        Integer existingCount = notificationJdbc.queryForObject(
            "SELECT COUNT(*) FROM notifications", Integer.class);
        if (existingCount != null && existingCount >= notificationCount) {
            log.info("Phase 13 complete: Already have {} notifications (target: {}), skipping", existingCount, notificationCount);
            return;
        }

        String[] titles = {
            "Welcome to CyberLearnix!",
            "New course available in {category}",
            "Your enrollment is confirmed",
            "Course updated: {course}",
            "Special offer: {coupon}",
            "Assignment due reminder",
            "Certificate ready",
            "New lecture added",
            "Instructor announcement",
            "Payment successful"
        };

        String[] messages = {
            "Thank you for joining CyberLearnix. Start learning today!",
            "A new course has been added to your favorite category.",
            "You have been successfully enrolled in the course.",
            "The course content has been updated with new material.",
            "Use this coupon to get discounts on selected courses.",
            "Don't forget to complete your assignment by the deadline.",
            "Your certificate is ready for download.",
            "A new lecture has been added to your enrolled course.",
            "Your instructor has made an important announcement.",
            "Your payment was processed successfully."
        };

        int seeded = 0;
        for (int i = 0; i < notificationCount; i++) {
            UUID studentId = studentIds.get(i % studentIds.size());
            String title = titles[i % titles.length];
            String message = messages[i % messages.length];
            
            UUID notificationId = UUID.randomUUID();
            
            notificationJdbc.update(
                "INSERT INTO notifications (id, title, message, status, priority, is_deleted, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                notificationId, title, message, "SENT", "MEDIUM", false, LocalDateTime.now()
            );
            
            // Add user to notification_users
            notificationJdbc.update(
                "INSERT INTO notification_users (notification_id, user_id) VALUES (?, ?)",
                notificationId, studentId
            );
            
            // Add channel to notification_channels
            notificationJdbc.update(
                "INSERT INTO notification_channels (notification_id, channel) VALUES (?, ?)",
                notificationId, "EMAIL"
            );
            seeded++;
        }
        
        log.info("Phase 13 complete: Seeded {} notifications", seeded);
    }

    private void printSummary() {
        log.info("========================================");
        log.info("DATASET SEEDING SUMMARY");
        log.info("========================================");
        log.info("Students: {}", studentIds.size());
        log.info("Instructors: {}", instructorIds.size());
        log.info("Courses: {}", courseIds.size());
        log.info("Sections: {}", jdbcTemplates.get("course").queryForObject(
            "SELECT COUNT(*) FROM sections", Integer.class));
        log.info("Lectures: {}", jdbcTemplates.get("course").queryForObject(
            "SELECT COUNT(*) FROM lectures", Integer.class));
        log.info("Enrollments: {}", jdbcTemplates.get("course").queryForObject(
            "SELECT COUNT(*) FROM enrollment", Integer.class));
        log.info("Wishlist Items: {}", jdbcTemplates.get("wishlist").queryForObject(
            "SELECT COUNT(*) FROM wishlist_items", Integer.class));
        log.info("Cart Items: {}", jdbcTemplates.get("cart").queryForObject(
            "SELECT COUNT(*) FROM cart_items", Integer.class));
        log.info("Reviews: {}", jdbcTemplates.get("review").queryForObject(
            "SELECT COUNT(*) FROM reviews", Integer.class));
        log.info("Coupons: {}", jdbcTemplates.get("coupon").queryForObject(
            "SELECT COUNT(*) FROM coupons", Integer.class));
        log.info("Orders: {}", jdbcTemplates.get("order").queryForObject(
            "SELECT COUNT(*) FROM orders", Integer.class));
        log.info("Payments: {}", jdbcTemplates.get("payment").queryForObject(
            "SELECT COUNT(*) FROM payments", Integer.class));
        log.info("Notifications: {}", jdbcTemplates.get("notification").queryForObject(
            "SELECT COUNT(*) FROM notifications", Integer.class));
        log.info("========================================");
    }
}
