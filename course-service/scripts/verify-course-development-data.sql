SELECT status, count(*) AS courses
FROM courses
GROUP BY status
ORDER BY status;

SELECT 'seed_courses' AS metric, count(*) AS rows
FROM courses
WHERE title LIKE '[CYBERLEARNIX-SEED-%'
UNION ALL SELECT 'sections', count(*) FROM sections s JOIN courses c ON c.id = s.course_id WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
UNION ALL SELECT 'lectures', count(*) FROM lectures l JOIN sections s ON s.id = l.section_id JOIN courses c ON c.id = s.course_id WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
UNION ALL SELECT 'materials', count(*) FROM course_materials m JOIN courses c ON c.id = m.course_id WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
UNION ALL SELECT 'previews', count(*) FROM course_previews p JOIN courses c ON c.id = p.course_id WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
UNION ALL SELECT 'requirements', count(*) FROM course_requirements r JOIN courses c ON c.id = r.course_id WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
UNION ALL SELECT 'outcomes', count(*) FROM learning_outcomes o JOIN courses c ON c.id = o.course_id WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
UNION ALL SELECT 'faqs', count(*) FROM course_faq f JOIN courses c ON c.id = f.course_id WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
UNION ALL SELECT 'enrollments', count(*) FROM enrollment e JOIN courses c ON c.id = e.course_id WHERE c.title LIKE '[CYBERLEARNIX-SEED-%'
UNION ALL SELECT 'banners', count(*) FROM banners WHERE title LIKE '[CYBERLEARNIX-SEED] %'
UNION ALL SELECT 'quizzes', count(*) FROM quizzes WHERE title LIKE '[CYBERLEARNIX-SEED-QUIZ-%'
UNION ALL SELECT 'quiz_questions', count(*) FROM quiz_questions q JOIN quizzes z ON z.id = q.quiz_id WHERE z.title LIKE '[CYBERLEARNIX-SEED-QUIZ-%'
UNION ALL SELECT 'quiz_options', count(*) FROM quiz_options o JOIN quiz_questions q ON q.id = o.question_id JOIN quizzes z ON z.id = q.quiz_id WHERE z.title LIKE '[CYBERLEARNIX-SEED-QUIZ-%'
UNION ALL SELECT 'assignments', count(*) FROM assignments WHERE title LIKE '[CYBERLEARNIX-SEED-ASSIGNMENT-%'
UNION ALL SELECT 'assignment_submissions', count(*) FROM assignment_submissions s JOIN assignments a ON a.id = s.assignment_id WHERE a.title LIKE '[CYBERLEARNIX-SEED-ASSIGNMENT-%'
UNION ALL SELECT 'live_classes', count(*) FROM live_classes WHERE title LIKE '[CYBERLEARNIX-SEED-LIVE-%'
UNION ALL SELECT 'live_class_attendance', count(*) FROM live_class_attendances a JOIN live_classes l ON l.id = a.live_class_id WHERE l.title LIKE '[CYBERLEARNIX-SEED-LIVE-%'
UNION ALL SELECT 'lecture_progress', count(*) FROM lecture_progress p JOIN courses c ON c.id = p.course_id WHERE c.title LIKE '[CYBERLEARNIX-SEED-%';

SELECT 'orphan_sections' AS check_name, count(*) AS failures FROM sections s LEFT JOIN courses c ON c.id = s.course_id WHERE s.course_id IS NOT NULL AND c.id IS NULL
UNION ALL SELECT 'orphan_lectures', count(*) FROM lectures l LEFT JOIN sections s ON s.id = l.section_id WHERE l.section_id IS NOT NULL AND s.id IS NULL
UNION ALL SELECT 'orphan_previews', count(*) FROM course_previews p LEFT JOIN courses c ON c.id = p.course_id WHERE p.course_id IS NOT NULL AND c.id IS NULL
UNION ALL SELECT 'orphan_requirements', count(*) FROM course_requirements r LEFT JOIN courses c ON c.id = r.course_id WHERE r.course_id IS NULL OR c.id IS NULL
UNION ALL SELECT 'orphan_outcomes', count(*) FROM learning_outcomes o LEFT JOIN courses c ON c.id = o.course_id WHERE o.course_id IS NULL OR c.id IS NULL
UNION ALL SELECT 'orphan_faqs', count(*) FROM course_faq f LEFT JOIN courses c ON c.id = f.course_id WHERE f.course_id IS NULL OR c.id IS NULL
UNION ALL SELECT 'orphan_materials', count(*) FROM course_materials m LEFT JOIN courses c ON c.id = m.course_id WHERE m.course_id IS NULL OR c.id IS NULL
UNION ALL SELECT 'orphan_enrollments', count(*) FROM enrollment e LEFT JOIN courses c ON c.id = e.course_id WHERE e.course_id IS NULL OR c.id IS NULL
UNION ALL SELECT 'invalid_enrollment_lecture', count(*) FROM enrollment e LEFT JOIN lectures l ON l.id = e.last_lecture_id WHERE e.last_lecture_id IS NOT NULL AND l.id IS NULL
UNION ALL SELECT 'orphan_quizzes', count(*) FROM quizzes q LEFT JOIN courses c ON c.id = q.course_id LEFT JOIN sections s ON s.id = q.section_id WHERE q.title LIKE '[CYBERLEARNIX-SEED-QUIZ-%' AND (c.id IS NULL OR s.id IS NULL)
UNION ALL SELECT 'orphan_questions', count(*) FROM quiz_questions q LEFT JOIN quizzes z ON z.id = q.quiz_id WHERE z.id IS NULL
UNION ALL SELECT 'orphan_options', count(*) FROM quiz_options o LEFT JOIN quiz_questions q ON q.id = o.question_id WHERE q.id IS NULL
UNION ALL SELECT 'orphan_assignments', count(*) FROM assignments a LEFT JOIN courses c ON c.id = a.course_id LEFT JOIN sections s ON s.id = a.section_id WHERE a.title LIKE '[CYBERLEARNIX-SEED-ASSIGNMENT-%' AND (c.id IS NULL OR s.id IS NULL)
UNION ALL SELECT 'orphan_submissions', count(*) FROM assignment_submissions x LEFT JOIN assignments a ON a.id = x.assignment_id WHERE a.id IS NULL
UNION ALL SELECT 'orphan_live_classes', count(*) FROM live_classes l LEFT JOIN courses c ON c.id = l.course_id LEFT JOIN sections s ON s.id = l.section_id WHERE l.title LIKE '[CYBERLEARNIX-SEED-LIVE-%' AND (c.id IS NULL OR s.id IS NULL)
UNION ALL SELECT 'orphan_attendance', count(*) FROM live_class_attendances a LEFT JOIN live_classes l ON l.id = a.live_class_id WHERE l.id IS NULL
UNION ALL SELECT 'orphan_progress', count(*) FROM lecture_progress p LEFT JOIN courses c ON c.id = p.course_id LEFT JOIN lectures l ON l.id = p.lecture_id WHERE c.id IS NULL OR l.id IS NULL
UNION ALL SELECT 'orphan_seed_banners', count(*) FROM banners b LEFT JOIN courses c ON c.id = b.course_id WHERE b.title LIKE '[CYBERLEARNIX-SEED] %' AND c.id IS NULL;

SELECT count(DISTINCT category) AS categories,
       count(DISTINCT instructor_id) AS instructors,
       count(DISTINCT level) AS levels,
       count(DISTINCT language) AS languages,
       min(price) AS min_price,
       max(price) AS max_price,
       min(view_count) AS min_views,
       max(view_count) AS max_views
FROM courses
WHERE upper(status) = 'PUBLISHED';