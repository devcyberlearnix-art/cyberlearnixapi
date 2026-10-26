BEGIN;

DO $$
DECLARE
    current_published_count BIGINT;
    existing_seed_count BIGINT;
BEGIN
    SELECT count(*) INTO current_published_count
    FROM courses
    WHERE upper(status) = 'PUBLISHED';

    SELECT count(*) INTO existing_seed_count
    FROM courses
    WHERE title LIKE '[CYBERLEARNIX-SEED-%';

    IF existing_seed_count > 295 THEN
        RAISE EXCEPTION 'Found % seed courses; expected no more than 295', existing_seed_count;
    END IF;

    IF current_published_count <> 5 + existing_seed_count THEN
        RAISE EXCEPTION 'Expected 5 existing published courses plus % seed courses, found % published courses',
            existing_seed_count, current_published_count;
    END IF;
END $$;

WITH seed_rows AS (
    SELECT
        n,
        (ARRAY[
            'Programming', 'Web Development', 'Backend Development', 'Frontend Development',
            'Mobile Development', 'Cyber Security', 'Cloud Computing', 'DevOps', 'Data Science',
            'Artificial Intelligence', 'Machine Learning', 'Database', 'Software Engineering',
            'Networking', 'Computer Science', 'Testing', 'System Design', 'Blockchain', 'UI/UX',
            'Business/Technology'
        ])[(n - 6) % 20 + 1] AS category,
        (ARRAY[
            'Java and Python Programming Foundations', 'Modern Web Applications with React',
            'Production APIs with Spring Boot', 'Accessible Interfaces with TypeScript',
            'Android Development with Kotlin', 'Practical Cybersecurity and Threat Defense',
            'Cloud Architecture on AWS', 'Reliable Delivery with Docker and Kubernetes',
            'Data Analysis with Python and SQL', 'Applied Artificial Intelligence Projects',
            'Machine Learning Model Engineering', 'PostgreSQL Design and Query Tuning',
            'Clean Architecture for Software Teams', 'Network Engineering and Troubleshooting',
            'Algorithms and Computational Thinking', 'Automated Testing with Playwright',
            'Scalable System Design Fundamentals', 'Smart Contracts and Blockchain Basics',
            'Product Design and UX Research', 'Technology Strategy for Digital Products'
        ])[(n - 6) % 20 + 1] AS topic,
        (ARRAY[
            'd2100000-0000-0000-0000-000000000001'::uuid,
            'd2100000-0000-0000-0000-000000000002'::uuid,
            'd2100000-0000-0000-0000-000000000003'::uuid,
            'd2100000-0000-0000-0000-000000000004'::uuid,
            'd2100000-0000-0000-0000-000000000005'::uuid
        ])[(n - 6) % 5 + 1] AS instructor_id,
        CASE (n - 6) % 3
            WHEN 0 THEN 'BEGINNER'
            WHEN 1 THEN 'INTERMEDIATE'
            ELSE 'ADVANCED'
        END AS level,
        CASE (n - 6) % 3
            WHEN 0 THEN 'English'
            WHEN 1 THEN 'Hindi'
            ELSE 'Telugu'
        END AS language,
        (ARRAY[0, 299, 499, 799, 999, 1499, 1999, 2999, 4999])[(n - 6) % 9 + 1] AS price,
        CASE
            WHEN n = 6 THEN 500000
            WHEN n = 7 THEN 50
            ELSE 100 + ((n * 7919) % 499900)
        END::bigint AS view_count,
        (n * 97) % 25000::bigint AS search_count
    FROM generate_series(6, 300) AS series(n)
)
INSERT INTO courses (
    title, subtitle, description, category, level, language, price, thumbnail,
    instructor_id, status, premium, search_count, view_count
)
SELECT
    '[CYBERLEARNIX-SEED-' || lpad(n::text, 3, '0') || '] ' || topic,
    'Project-based ' || level || ' learning in ' || category,
    'Build practical skills in ' || topic || '. Work through guided lessons, realistic exercises, and a final project using current tools and proven engineering practices.',
    category,
    level,
    language,
    price,
    'https://placehold.co/640x360/png?text=CyberLearnix+Course+' || n,
    instructor_id,
    'PUBLISHED',
    (n % 4 <> 0),
    search_count,
    view_count
FROM seed_rows
WHERE NOT EXISTS (
    SELECT 1 FROM courses existing
    WHERE existing.title = '[CYBERLEARNIX-SEED-' || lpad(seed_rows.n::text, 3, '0') || '] ' || seed_rows.topic
);

WITH seed_courses AS (
    SELECT
        c.id AS course_id,
        substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM courses c
    WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
), section_rows AS (
    SELECT
        seed_courses.course_id,
        seed_courses.seed_index,
        section_order
    FROM seed_courses
    CROSS JOIN LATERAL generate_series(1, 5 + mod(seed_courses.seed_index, 6)) AS sections(section_order)
)
INSERT INTO sections (title, order_index, course_id)
SELECT
    'Module ' || lpad(section_order::text, 2, '0') || ': ' ||
        (ARRAY['Core Concepts', 'Guided Practice', 'Building Blocks', 'Applied Workshop', 'Project Lab', 'Production Patterns'])[(section_order - 1) % 6 + 1],
    section_order,
    course_id
FROM section_rows
WHERE NOT EXISTS (
    SELECT 1 FROM sections existing
    WHERE existing.course_id = section_rows.course_id
      AND existing.order_index = section_rows.section_order
);

WITH seed_sections AS (
    SELECT
        s.id AS section_id,
        s.course_id,
        s.order_index AS section_order,
        substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM sections s
    JOIN courses c ON c.id = s.course_id
    WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
), lecture_rows AS (
    SELECT seed_sections.*, lecture_order
    FROM seed_sections
    CROSS JOIN LATERAL generate_series(1, 4 + mod(seed_sections.seed_index + seed_sections.section_order, 7)) AS lectures(lecture_order)
)
INSERT INTO lectures (
    title, description, video_url, duration, order_index, preview_enabled,
    resources, type, section_id
)
SELECT
    'Lesson ' || lpad(lecture_order::text, 2, '0') || ': ' ||
        (ARRAY['Concepts and Vocabulary', 'Implementation Walkthrough', 'Hands-on Exercise', 'Testing and Review', 'Production Notes', 'Knowledge Check', 'Case Study', 'Further Practice', 'Project Milestone', 'Wrap-up'])[(lecture_order - 1) % 10 + 1],
    'A focused lesson with an explanation, worked example, and practical checkpoint.',
    'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    300 + ((seed_index * 37 + section_order * 83 + lecture_order * 61) % 2100),
    lecture_order,
    (section_order = 1 AND lecture_order = 1),
    'https://example.com/cyberlearnix/seed/' || seed_index || '/module/' || section_order || '/lesson/' || lecture_order || '/resources',
    CASE lecture_order % 6
        WHEN 0 THEN 'QUIZ'
        WHEN 1 THEN 'VIDEO'
        WHEN 2 THEN 'ARTICLE'
        WHEN 3 THEN 'DOCUMENT'
        WHEN 4 THEN 'ASSIGNMENT'
        ELSE 'LIVE_CLASS'
    END,
    section_id
FROM lecture_rows
WHERE NOT EXISTS (
    SELECT 1 FROM lectures existing
    WHERE existing.section_id = lecture_rows.section_id
      AND existing.order_index = lecture_rows.lecture_order
);

WITH seed_sections AS (
    SELECT
        s.course_id,
        s.order_index AS section_order,
        substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM sections s
    JOIN courses c ON c.id = s.course_id
    WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
), material_rows AS (
    SELECT seed_sections.*, material_order
    FROM seed_sections
    CROSS JOIN LATERAL generate_series(1, 2) AS materials(material_order)
)
INSERT INTO course_materials (
    course_id, material_name, material_type, file_url, file_size, created_at
)
SELECT
    course_id,
    'Module ' || lpad(section_order::text, 2, '0') || CASE material_order
        WHEN 1 THEN ' - Workbook PDF'
        ELSE ' - Source Code'
    END,
    CASE material_order WHEN 1 THEN 'PDF' ELSE 'CODE' END,
    'https://example.com/cyberlearnix/seed/' || seed_index || '/module/' || section_order || '/resource/' || material_order,
    CASE material_order WHEN 1 THEN 480000 ELSE 125000 END,
    now() - ((seed_index * 3 + section_order) % 730) * interval '1 day'
FROM material_rows
WHERE NOT EXISTS (
    SELECT 1 FROM course_materials existing
    WHERE existing.course_id = material_rows.course_id
      AND existing.material_name = 'Module ' || lpad(material_rows.section_order::text, 2, '0') ||
          CASE material_rows.material_order WHEN 1 THEN ' - Workbook PDF' ELSE ' - Source Code' END
);

WITH seed_courses AS (
    SELECT
        c.id AS course_id,
        c.title,
        substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM courses c
    WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
)
INSERT INTO course_previews (title, video_url, duration, course_id)
SELECT
    'Preview: ' || seed_courses.title,
    'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    180 + mod(seed_index * 13, 240),
    course_id
FROM seed_courses
WHERE NOT EXISTS (
    SELECT 1 FROM course_previews existing WHERE existing.course_id = seed_courses.course_id
);

WITH seed_courses AS (
    SELECT c.id AS course_id, substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM courses c WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
), requirement_rows AS (
    SELECT seed_courses.*, requirement_order
    FROM seed_courses
    CROSS JOIN LATERAL generate_series(1, 3) AS requirements(requirement_order)
)
INSERT INTO course_requirements (course_id, requirement_type, requirement_text, created_at)
SELECT
    course_id,
    CASE requirement_order
        WHEN 1 THEN 'PRIOR_KNOWLEDGE'
        WHEN 2 THEN 'SOFTWARE'
        ELSE 'TIME_COMMITMENT'
    END,
    CASE requirement_order
        WHEN 1 THEN CASE seed_index % 3
            WHEN 0 THEN 'No prior experience is required; curiosity and basic computer skills are enough.'
            WHEN 1 THEN 'Comfort with programming fundamentals and reading technical documentation is recommended.'
            ELSE 'Experience building and debugging software systems is expected.'
        END
        WHEN 2 THEN 'A laptop with a current browser and the free tools listed in the course setup guide.'
        ELSE 'Plan for two to four hours of focused practice each week.'
    END,
    now() - (seed_index % 365) * interval '1 day'
FROM requirement_rows
WHERE NOT EXISTS (
    SELECT 1 FROM course_requirements existing
    WHERE existing.course_id = requirement_rows.course_id
      AND existing.requirement_type = CASE requirement_order
          WHEN 1 THEN 'PRIOR_KNOWLEDGE'
          WHEN 2 THEN 'SOFTWARE'
          ELSE 'TIME_COMMITMENT'
      END
);

WITH seed_courses AS (
    SELECT c.id AS course_id, c.category, c.title,
           substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM courses c WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
), outcome_rows AS (
    SELECT seed_courses.*, outcome_order
    FROM seed_courses
    CROSS JOIN LATERAL generate_series(1, 4) AS outcomes(outcome_order)
)
INSERT INTO learning_outcomes (course_id, outcome_text, skill_category, created_at)
SELECT
    course_id,
    CASE outcome_order
        WHEN 1 THEN 'Explain core ' || lower(category) || ' concepts and choose suitable tools for a practical problem.'
        WHEN 2 THEN 'Design and implement a small, maintainable project using techniques from the course.'
        WHEN 3 THEN 'Test, debug, and improve a solution using repeatable engineering practices.'
        ELSE 'Present a portfolio-ready project and describe its trade-offs and next steps.'
    END,
    CASE outcome_order WHEN 4 THEN 'SOFT_SKILLS' ELSE 'TECHNICAL' END,
    now() - (seed_index % 365) * interval '1 day'
FROM outcome_rows
WHERE NOT EXISTS (
    SELECT 1 FROM learning_outcomes existing
    WHERE existing.course_id = outcome_rows.course_id
      AND existing.outcome_text = CASE outcome_order
          WHEN 1 THEN 'Explain core ' || lower(category) || ' concepts and choose suitable tools for a practical problem.'
          WHEN 2 THEN 'Design and implement a small, maintainable project using techniques from the course.'
          WHEN 3 THEN 'Test, debug, and improve a solution using repeatable engineering practices.'
          ELSE 'Present a portfolio-ready project and describe its trade-offs and next steps.'
      END
);

WITH seed_courses AS (
    SELECT c.id AS course_id, c.title,
           substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM courses c WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
), faq_rows AS (
    SELECT seed_courses.*, faq_order FROM seed_courses
    CROSS JOIN LATERAL generate_series(1, 2) AS faqs(faq_order)
)
INSERT INTO course_faq (course_id, question, answer, display_order, created_at)
SELECT
    course_id,
    CASE faq_order WHEN 1 THEN 'What will I build in ' || title || '?' ELSE 'How should I work through the lessons?' END,
    CASE faq_order
        WHEN 1 THEN 'You will complete guided exercises and a practical project that applies the techniques covered in the curriculum.'
        ELSE 'Follow the modules in order, complete the practice tasks, and revisit the downloadable materials when needed.'
    END,
    faq_order,
    now() - (seed_index % 365) * interval '1 day'
FROM faq_rows
WHERE NOT EXISTS (
    SELECT 1 FROM course_faq existing
    WHERE existing.course_id = faq_rows.course_id AND existing.display_order = faq_rows.faq_order
);

WITH seed_courses AS (
    SELECT c.id AS course_id,
           substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM courses c WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
), enrollment_sizes AS (
    SELECT seed_courses.*,
        CASE
            WHEN seed_index = 6 THEN 0
            WHEN seed_index = 7 THEN 300
            WHEN mod(seed_index, 43) = 0 THEN 180
            ELSE 5 + mod(seed_index * 17, 75)
        END AS enrollment_count
    FROM seed_courses
), enrollment_rows AS (
    SELECT enrollment_sizes.*, student_order
    FROM enrollment_sizes
    CROSS JOIN LATERAL generate_series(1, enrollment_sizes.enrollment_count) AS students(student_order)
), course_lectures AS (
    SELECT s.course_id, min(l.id) AS first_lecture_id
    FROM sections s
    JOIN lectures l ON l.section_id = s.id
    GROUP BY s.course_id
)
INSERT INTO enrollment (
    student_id, course_id, student_name, enrolled_at, status, progress, completed_at, last_lecture_id
)
SELECT
    md5('cyberlearnix-seed-student-' || student_order)::uuid,
    enrollment_rows.course_id,
    (ARRAY['Aarav', 'Ananya', 'Dev', 'Diya', 'Ishaan', 'Kavya', 'Mira', 'Neel', 'Rohan', 'Sana', 'Vihaan', 'Zoya'])[(student_order - 1) % 12 + 1]
        || ' ' || (ARRAY['Shah', 'Reddy', 'Patel', 'Iyer', 'Khan', 'Nair', 'Rao', 'Mehta', 'Das', 'Kapoor'])[(student_order - 1) % 10 + 1],
    now() - ((seed_index * 11 + student_order * 7) % 730) * interval '1 day',
    'ACTIVE',
    CASE WHEN student_order % 10 = 0 THEN 100.0 ELSE mod(seed_index * 13 + student_order * 7, 96)::double precision END,
    CASE WHEN student_order % 10 = 0 THEN now() - ((seed_index + student_order) % 180) * interval '1 day' ELSE NULL END,
    course_lectures.first_lecture_id
FROM enrollment_rows
JOIN course_lectures ON course_lectures.course_id = enrollment_rows.course_id
WHERE NOT EXISTS (
    SELECT 1 FROM enrollment existing
    WHERE existing.course_id = enrollment_rows.course_id
      AND existing.student_id = md5('cyberlearnix-seed-student-' || enrollment_rows.student_order)::uuid
);

WITH seed_courses AS (
    SELECT c.id AS course_id, c.title,
           substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM courses c WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
), banner_rows AS (
    SELECT seed_courses.*, row_number() OVER (ORDER BY seed_index) AS banner_order
    FROM seed_courses
    WHERE seed_index IN (6, 20, 34, 48, 62, 76, 90, 104)
)
INSERT INTO banners (
    title, description, image_url, mobile_image_url, alt_text, cta_text, cta_url,
    course_id, display_order, status, target_type, start_date, end_date,
    created_at, updated_at, created_by, impressions, clicks, conversions
)
SELECT
    '[CYBERLEARNIX-SEED] ' || title,
    'Explore a practical, project-based course in ' || title || '.',
    'https://placehold.co/1200x400/png?text=CyberLearnix+Learning',
    'https://placehold.co/640x480/png?text=CyberLearnix+Learning',
    'CyberLearnix course feature banner',
    'Explore course',
    '/courses/' || course_id,
    course_id,
    900 + banner_order,
    'ACTIVE',
    'COURSE',
    now() - ((seed_index % 14) + 1) * interval '1 day',
    now() + interval '180 days',
    now() - (seed_index % 90) * interval '1 day',
    now(),
    'development-seed@cyberlearnix.test',
    500 + seed_index * 137,
    10 + seed_index * 3,
    seed_index % 40
FROM banner_rows
WHERE NOT EXISTS (
    SELECT 1 FROM banners existing WHERE existing.title = '[CYBERLEARNIX-SEED] ' || banner_rows.title
);

WITH seed_courses AS (
    SELECT c.id AS course_id, c.instructor_id, s.id AS section_id, c.title,
           substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM courses c
    JOIN sections s ON s.course_id = c.id AND s.order_index = 1
    WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
)
INSERT INTO quizzes (
    course_id, section_id, title, description, instructions, status,
    time_limit_minutes, max_attempts, passing_score, total_marks,
    created_by, created_at, updated_at
)
SELECT
    course_id, section_id,
    '[CYBERLEARNIX-SEED-QUIZ-' || lpad(seed_index::text, 3, '0') || '] Course checkpoint',
    'A short knowledge check covering the core skills taught in this course.',
    'Choose the best answer for each question. Review the lesson materials before attempting the quiz.',
    'PUBLISHED', 20, 3, 70, 30, instructor_id,
    now() - (seed_index % 180) * interval '1 day', now()
FROM seed_courses
WHERE NOT EXISTS (
    SELECT 1 FROM quizzes existing
    WHERE existing.course_id = seed_courses.course_id
      AND existing.title = '[CYBERLEARNIX-SEED-QUIZ-' || lpad(seed_courses.seed_index::text, 3, '0') || '] Course checkpoint'
);

WITH seed_quizzes AS (
    SELECT q.id AS quiz_id,
           substring(q.title from 'SEED-QUIZ-([0-9]{3})')::integer AS seed_index,
           question_order
    FROM quizzes q
    CROSS JOIN LATERAL generate_series(1, 3) AS questions(question_order)
    WHERE q.title LIKE '[CYBERLEARNIX-SEED-QUIZ-%'
)
INSERT INTO quiz_questions (
    quiz_id, question_text, question_type, marks, order_index,
    explanation, created_at, updated_at
)
SELECT
    quiz_id,
    'Which approach best supports reliable work in this course? Question ' || question_order || ' for course ' || seed_index || '.',
    'SINGLE_CHOICE', 10, question_order,
    'Prefer a small, testable implementation and verify it with a repeatable check.',
    now() - (seed_index % 180) * interval '1 day', now()
FROM seed_quizzes
WHERE NOT EXISTS (
    SELECT 1 FROM quiz_questions existing
    WHERE existing.quiz_id = seed_quizzes.quiz_id
      AND existing.order_index = seed_quizzes.question_order
);

WITH seed_questions AS (
    SELECT q.id AS question_id, q.order_index,
           substring(quiz.title from 'SEED-QUIZ-([0-9]{3})')::integer AS seed_index,
           option_order
    FROM quiz_questions q
    JOIN quizzes quiz ON quiz.id = q.quiz_id
    CROSS JOIN LATERAL generate_series(1, 4) AS options(option_order)
    WHERE quiz.title LIKE '[CYBERLEARNIX-SEED-QUIZ-%'
)
INSERT INTO quiz_options (
    question_id, option_text, is_correct, order_index, created_at, updated_at
)
SELECT
    question_id,
    CASE option_order
        WHEN 1 THEN 'Build a small solution and verify it with focused tests.'
        WHEN 2 THEN 'Skip validation and rely on the initial implementation.'
        WHEN 3 THEN 'Duplicate the same logic in every module.'
        ELSE 'Defer all error handling until after release.'
    END,
    option_order = ((order_index - 1) % 4 + 1),
    option_order,
    now() - (seed_index % 180) * interval '1 day', now()
FROM seed_questions
WHERE NOT EXISTS (
    SELECT 1 FROM quiz_options existing
    WHERE existing.question_id = seed_questions.question_id
      AND existing.order_index = seed_questions.option_order
);

WITH seed_courses AS (
    SELECT c.id AS course_id, c.instructor_id, c.title, s.id AS section_id,
           substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM courses c
    JOIN sections s ON s.course_id = c.id AND s.order_index = 2
    WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
)
INSERT INTO assignments (
    course_id, section_id, title, description, instructions, max_score,
    due_date, status, allowed_file_types, max_file_size_bytes,
    created_by, created_at, updated_at
)
SELECT
    course_id, section_id,
    '[CYBERLEARNIX-SEED-ASSIGNMENT-' || lpad(seed_index::text, 3, '0') || '] Applied course project',
    'Apply the course techniques in a small, documented project.',
    'Submit your source files and a short explanation of design choices, tests, and known limitations.',
    100,
    now() + ((seed_index % 30) - 10) * interval '1 day',
    'PUBLISHED', 'pdf,zip,txt', 10485760,
    instructor_id, now() - (seed_index % 180) * interval '1 day', now()
FROM seed_courses
WHERE NOT EXISTS (
    SELECT 1 FROM assignments existing
    WHERE existing.course_id = seed_courses.course_id
      AND existing.title = '[CYBERLEARNIX-SEED-ASSIGNMENT-' || lpad(seed_courses.seed_index::text, 3, '0') || '] Applied course project'
);

WITH seed_assignments AS (
    SELECT a.id AS assignment_id, c.instructor_id,
           substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index,
           student_order
    FROM assignments a
    JOIN courses c ON c.id = a.course_id
    CROSS JOIN LATERAL generate_series(1, 3) AS students(student_order)
    WHERE a.title LIKE '[CYBERLEARNIX-SEED-ASSIGNMENT-%'
)
INSERT INTO assignment_submissions (
    assignment_id, student_id, submission_text, file_url, status,
    created_at, submitted_at, updated_at, score, feedback,
    graded_by, graded_at, attempt_number
)
SELECT
    assignment_id,
    md5('cyberlearnix-seed-student-' || student_order)::uuid,
    'Development submission ' || student_order || ' for course ' || seed_index || ': includes implementation notes and test results.',
    'https://example.com/cyberlearnix/seed/' || seed_index || '/assignment/submission/' || student_order,
    CASE student_order WHEN 1 THEN 'GRADED' WHEN 2 THEN 'SUBMITTED' ELSE 'DRAFT' END,
    now() - ((seed_index + student_order) % 90) * interval '1 day',
    CASE WHEN student_order = 3 THEN NULL ELSE now() - ((seed_index + student_order) % 60) * interval '1 day' END,
    now(),
    CASE WHEN student_order = 1 THEN 65 + seed_index % 36 ELSE NULL END,
    CASE WHEN student_order = 1 THEN 'Clear implementation; review the documented edge cases.' ELSE NULL END,
    CASE WHEN student_order = 1 THEN instructor_id ELSE NULL END,
    CASE WHEN student_order = 1 THEN now() - (seed_index % 30) * interval '1 day' ELSE NULL END,
    1
FROM seed_assignments
WHERE NOT EXISTS (
    SELECT 1 FROM assignment_submissions existing
    WHERE existing.assignment_id = seed_assignments.assignment_id
      AND existing.student_id = md5('cyberlearnix-seed-student-' || seed_assignments.student_order)::uuid
);

WITH seed_courses AS (
    SELECT c.id AS course_id, c.instructor_id, c.title, s.id AS section_id,
           substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index
    FROM courses c
    JOIN sections s ON s.course_id = c.id AND s.order_index = 1
    WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
      AND mod(substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer, 5) = 0
)
INSERT INTO live_classes (
    course_id, section_id, instructor_id, title, description, meeting_url,
    meeting_provider, scheduled_start_time, scheduled_end_time, status,
    recording_url, max_participants, cancelled_at, created_at, updated_at
)
SELECT
    course_id, section_id, instructor_id,
    '[CYBERLEARNIX-SEED-LIVE-' || lpad(seed_index::text, 3, '0') || '] Instructor workshop',
    'A focused live workshop with guided questions and practical examples.',
    'https://example.com/cyberlearnix/seed/live-class/' || seed_index,
    'GOOGLE_MEET',
    CASE WHEN mod(seed_index, 3) = 0 THEN now() + interval '7 days' ELSE now() - interval '7 days' END,
    CASE WHEN mod(seed_index, 3) = 0 THEN now() + interval '7 days 1 hour' ELSE now() - interval '7 days' + interval '1 hour' END,
    CASE mod(seed_index, 3) WHEN 0 THEN 'SCHEDULED' WHEN 1 THEN 'COMPLETED' ELSE 'CANCELLED' END,
    CASE WHEN mod(seed_index, 3) = 1 THEN 'https://example.com/cyberlearnix/seed/live-class/' || seed_index || '/recording' ELSE NULL END,
    100,
    CASE WHEN mod(seed_index, 3) = 2 THEN now() - interval '8 days' ELSE NULL END,
    now() - interval '14 days', now()
FROM seed_courses
WHERE NOT EXISTS (
    SELECT 1 FROM live_classes existing
    WHERE existing.title = '[CYBERLEARNIX-SEED-LIVE-' || lpad(seed_courses.seed_index::text, 3, '0') || '] Instructor workshop'
);

WITH completed_classes AS (
    SELECT id, substring(title from 'SEED-LIVE-([0-9]{3})')::integer AS seed_index
    FROM live_classes
    WHERE title LIKE '[CYBERLEARNIX-SEED-LIVE-%' AND status = 'COMPLETED'
), attendance_rows AS (
    SELECT completed_classes.*, student_order
    FROM completed_classes
    CROSS JOIN LATERAL generate_series(1, 2) AS students(student_order)
)
INSERT INTO live_class_attendances (
    live_class_id, student_id, joined_at, left_at, duration_seconds,
    status, created_at, updated_at
)
SELECT
    id,
    md5('cyberlearnix-seed-student-' || student_order)::uuid,
    now() - interval '7 days', now() - interval '7 days' + interval '55 minutes',
    3300, 'ATTENDED', now() - interval '7 days', now()
FROM attendance_rows
WHERE NOT EXISTS (
    SELECT 1 FROM live_class_attendances existing
    WHERE existing.live_class_id = attendance_rows.id
      AND existing.student_id = md5('cyberlearnix-seed-student-' || attendance_rows.student_order)::uuid
);

WITH ordered_lectures AS (
    SELECT c.id AS course_id, l.id AS lecture_id,
           substring(c.title from 'CYBERLEARNIX-SEED-([0-9]{3})')::integer AS seed_index,
           row_number() OVER (PARTITION BY c.id ORDER BY s.order_index, l.order_index) AS lecture_order
    FROM courses c
    JOIN sections s ON s.course_id = c.id
    JOIN lectures l ON l.section_id = s.id
    WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
), progress_rows AS (
    SELECT ordered_lectures.*, student_order
    FROM ordered_lectures
    CROSS JOIN LATERAL generate_series(1, 2) AS students(student_order)
    WHERE lecture_order <= 3
)
INSERT INTO lecture_progress (
    student_id, course_id, lecture_id, status, progress_percentage,
    started_at, completed_at, last_accessed_at, time_spent_seconds
)
SELECT
    md5('cyberlearnix-seed-student-' || student_order)::uuid,
    course_id, lecture_id,
    CASE student_order WHEN 1 THEN 'COMPLETED' ELSE 'IN_PROGRESS' END,
    CASE student_order WHEN 1 THEN 100 ELSE 25 + (seed_index % 70) END,
    now() - ((seed_index + student_order) % 60) * interval '1 day',
    CASE WHEN student_order = 1 THEN now() - ((seed_index + lecture_order) % 30) * interval '1 day' ELSE NULL END,
    now() - ((seed_index + student_order) % 14) * interval '1 day',
    300 + ((seed_index * lecture_order + student_order) % 5400)
FROM progress_rows
WHERE NOT EXISTS (
    SELECT 1 FROM lecture_progress existing
    WHERE existing.student_id = md5('cyberlearnix-seed-student-' || progress_rows.student_order)::uuid
      AND existing.lecture_id = progress_rows.lecture_id
);

COMMIT;