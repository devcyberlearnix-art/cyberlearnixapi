BEGIN;

ALTER TABLE courses ADD COLUMN IF NOT EXISTS premium BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS search_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS view_count BIGINT NOT NULL DEFAULT 0;

INSERT INTO courses (id, title, subtitle, description, category, level, language, price, thumbnail, instructor_id, status, premium, search_count, view_count) VALUES
  (101, 'Python Data Science Bootcamp', 'From Python foundations to portfolio-ready analysis', 'Analyze real datasets with Python, pandas, visualization, and practical statistics.', 'Data Science', 'BEGINNER', 'English', 2199, 'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?auto=format&fit=crop&w=1200&q=80', 'd2000000-0000-0000-0000-000000000001', 'PUBLISHED', true, 180, 520),
  (102, 'Machine Learning with Real Projects', 'Six end-to-end projects with scikit-learn', 'Build, evaluate, and explain production-minded machine learning models.', 'Artificial Intelligence', 'INTERMEDIATE', 'English', 2999, 'https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?auto=format&fit=crop&w=1200&q=80', 'd2000000-0000-0000-0000-000000000001', 'PUBLISHED', true, 240, 680),
  (103, 'SQL Analytics Mastery', 'Turn business questions into trusted queries', 'Write reliable analytical SQL using joins, windows, CTEs, and performance tuning.', 'Data Engineering', 'INTERMEDIATE', 'English', 1299, 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80', 'd2000000-0000-0000-0000-000000000001', 'PUBLISHED', false, 155, 430),
  (104, 'Docker and Kubernetes in Production', 'Ship dependable container platforms', 'Containerize applications and operate resilient workloads on Kubernetes.', 'DevOps', 'INTERMEDIATE', 'English', 2799, 'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1200&q=80', 'd2000000-0000-0000-0000-000000000002', 'PUBLISHED', true, 210, 610),
  (105, 'AWS Cloud Architecture Essentials', 'Architecture patterns for modern AWS workloads', 'Design secure, scalable, and cost-aware systems using core AWS services.', 'Cloud Computing', 'BEGINNER', 'English', 2599, 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=1200&q=80', 'd2000000-0000-0000-0000-000000000002', 'PUBLISHED', true, 195, 570),
  (106, 'CI/CD with GitHub Actions', 'Build a delivery pipeline from commit to production', 'Automate tests, builds, security checks, and deployments with reusable workflows.', 'DevOps', 'BEGINNER', 'English', 999, 'https://images.unsplash.com/photo-1547658719-da2b51169166?auto=format&fit=crop&w=1200&q=80', 'd2000000-0000-0000-0000-000000000002', 'PUBLISHED', false, 120, 350)
ON CONFLICT (id) DO UPDATE SET
  title = EXCLUDED.title, subtitle = EXCLUDED.subtitle, description = EXCLUDED.description,
  category = EXCLUDED.category, level = EXCLUDED.level, language = EXCLUDED.language,
  price = EXCLUDED.price, thumbnail = EXCLUDED.thumbnail,
  instructor_id = EXCLUDED.instructor_id, status = EXCLUDED.status,
  premium = EXCLUDED.premium, search_count = EXCLUDED.search_count, view_count = EXCLUDED.view_count;

DELETE FROM lectures WHERE section_id BETWEEN 1001 AND 1012;
DELETE FROM sections WHERE id BETWEEN 1001 AND 1012;
INSERT INTO sections (id, title, order_index, course_id) VALUES
  (1001, 'Python and data foundations', 1, 101), (1002, 'Applied data analysis', 2, 101),
  (1003, 'Machine learning workflow', 1, 102), (1004, 'Production-minded modeling', 2, 102),
  (1005, 'Analytical SQL core', 1, 103), (1006, 'Advanced reporting patterns', 2, 103),
  (1007, 'Production containers', 1, 104), (1008, 'Kubernetes operations', 2, 104),
  (1009, 'AWS architecture foundations', 1, 105), (1010, 'Reliable cloud systems', 2, 105),
  (1011, 'GitHub Actions foundations', 1, 106), (1012, 'Delivery pipeline project', 2, 106);

INSERT INTO lectures (id, title, description, video_url, duration, order_index, preview_enabled, resources, section_id) VALUES
  (2001, 'Welcome and environment setup', 'Prepare Python and Jupyter.', 'https://www.youtube.com/watch?v=rfscVS0vtbw', 18, 1, true, 'Starter notebook', 1001),
  (2002, 'Python for analysis', 'Collections, functions, and clean data.', 'https://www.youtube.com/watch?v=LHBE6Q9XlzI', 42, 2, false, 'Python cheat sheet', 1001),
  (2003, 'Pandas case study', 'Explore a realistic sales dataset.', 'https://www.youtube.com/watch?v=vmEHCJofslg', 55, 1, false, 'Sales dataset', 1002),
  (2004, 'The ML project lifecycle', 'Frame and validate an ML problem.', 'https://www.youtube.com/watch?v=7eh4d6sabA0', 35, 1, true, 'Project brief', 1003),
  (2005, 'Feature engineering', 'Create robust predictive features.', 'https://www.youtube.com/watch?v=7eh4d6sabA0', 47, 2, false, 'Feature worksheet', 1003),
  (2006, 'Evaluate and explain models', 'Metrics, validation, and explainability.', 'https://www.youtube.com/watch?v=7eh4d6sabA0', 58, 1, false, 'Evaluation notebook', 1004),
  (2007, 'Joins that scale', 'Use joins safely in analytics.', 'https://www.youtube.com/watch?v=HXV3zeQKqGY', 39, 1, true, 'SQL schema', 1005),
  (2008, 'Window functions', 'Build rankings and running metrics.', 'https://www.youtube.com/watch?v=HXV3zeQKqGY', 46, 2, false, 'Query exercises', 1005),
  (2009, 'Analytics capstone', 'Create a business KPI report.', 'https://www.youtube.com/watch?v=HXV3zeQKqGY', 62, 1, false, 'Capstone brief', 1006),
  (2010, 'Build secure images', 'Create small reproducible containers.', 'https://www.youtube.com/watch?v=fqMOX6JJhGo', 44, 1, true, 'Dockerfile examples', 1007),
  (2011, 'Deploy to Kubernetes', 'Deploy and expose a web service.', 'https://www.youtube.com/watch?v=X48VuDVv0do', 57, 1, false, 'Kubernetes manifests', 1008),
  (2012, 'Observe and recover', 'Health checks, rollouts, and debugging.', 'https://www.youtube.com/watch?v=X48VuDVv0do', 51, 2, false, 'Operations runbook', 1008),
  (2013, 'AWS account foundations', 'Identity, networking, and budgets.', 'https://www.youtube.com/watch?v=ulprqHHWlng', 41, 1, true, 'Architecture checklist', 1009),
  (2014, 'Design resilient services', 'Scale across availability zones.', 'https://www.youtube.com/watch?v=ulprqHHWlng', 54, 1, false, 'Reference architecture', 1010),
  (2015, 'Cost and security review', 'Audit a cloud design.', 'https://www.youtube.com/watch?v=ulprqHHWlng', 38, 2, false, 'Review template', 1010),
  (2016, 'Your first workflow', 'Run checks on every pull request.', 'https://www.youtube.com/watch?v=R8_veQiYBjI', 32, 1, true, 'Workflow starter', 1011),
  (2017, 'Reusable delivery jobs', 'Share reliable workflow components.', 'https://www.youtube.com/watch?v=R8_veQiYBjI', 43, 2, false, 'Reusable workflow', 1011),
  (2018, 'Production release pipeline', 'Build and deploy a tagged release.', 'https://www.youtube.com/watch?v=R8_veQiYBjI', 64, 1, false, 'Capstone repository', 1012);

DELETE FROM course_previews WHERE course_id BETWEEN 101 AND 106;
INSERT INTO course_previews (id, title, video_url, duration, course_id) VALUES
  (3001, 'Python Data Science course preview', 'https://www.youtube.com/watch?v=LHBE6Q9XlzI', 180, 101),
  (3002, 'Machine Learning course preview', 'https://www.youtube.com/watch?v=7eh4d6sabA0', 190, 102),
  (3003, 'SQL Analytics course preview', 'https://www.youtube.com/watch?v=HXV3zeQKqGY', 170, 103),
  (3004, 'Docker and Kubernetes course preview', 'https://www.youtube.com/watch?v=X48VuDVv0do', 200, 104),
  (3005, 'AWS Architecture course preview', 'https://www.youtube.com/watch?v=ulprqHHWlng', 185, 105),
  (3006, 'GitHub Actions course preview', 'https://www.youtube.com/watch?v=R8_veQiYBjI', 175, 106);

DELETE FROM enrollment WHERE course_id BETWEEN 101 AND 106;
INSERT INTO enrollment (course_id, enrolled_at, progress, status, student_id, student_name) VALUES
  (101, CURRENT_TIMESTAMP - INTERVAL '40 days', 72, 'ACTIVE', 'd1000000-0000-0000-0000-000000000001', 'Maya Patel'),
  (101, CURRENT_TIMESTAMP - INTERVAL '28 days', 48, 'ACTIVE', 'd1000000-0000-0000-0000-000000000002', 'Arjun Mehta'),
  (102, CURRENT_TIMESTAMP - INTERVAL '20 days', 33, 'ACTIVE', 'd1000000-0000-0000-0000-000000000003', 'Sofia Khan'),
  (103, CURRENT_TIMESTAMP - INTERVAL '50 days', 91, 'COMPLETED', 'd1000000-0000-0000-0000-000000000004', 'Liam Chen'),
  (104, CURRENT_TIMESTAMP - INTERVAL '35 days', 56, 'ACTIVE', 'd1000000-0000-0000-0000-000000000001', 'Maya Patel'),
  (105, CURRENT_TIMESTAMP - INTERVAL '12 days', 22, 'ACTIVE', 'd1000000-0000-0000-0000-000000000002', 'Arjun Mehta'),
  (106, CURRENT_TIMESTAMP - INTERVAL '45 days', 84, 'ACTIVE', 'd1000000-0000-0000-0000-000000000003', 'Sofia Khan');

SELECT setval(pg_get_serial_sequence('courses', 'id'), GREATEST((SELECT MAX(id) FROM courses), 1));
SELECT setval(pg_get_serial_sequence('sections', 'id'), GREATEST((SELECT MAX(id) FROM sections), 1));
SELECT setval(pg_get_serial_sequence('lectures', 'id'), GREATEST((SELECT MAX(id) FROM lectures), 1));
SELECT setval(pg_get_serial_sequence('course_previews', 'id'), GREATEST((SELECT MAX(id) FROM course_previews), 1));
COMMIT;