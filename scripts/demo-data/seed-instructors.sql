BEGIN;

INSERT INTO instructor (id, user_id, name, email, headline, rating, verified) VALUES
  ('e1000000-0000-0000-0000-000000000001', 'd2000000-0000-0000-0000-000000000001', 'Priya Nair', 'priya.instructor@merqora.com', 'Machine Learning and Python Instructor', 4.9, true),
  ('e1000000-0000-0000-0000-000000000002', 'd2000000-0000-0000-0000-000000000002', 'Daniel Brooks', 'daniel.instructor@merqora.com', 'Cloud and DevOps Architect', 4.8, true)
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name, email = EXCLUDED.email, headline = EXCLUDED.headline,
  rating = EXCLUDED.rating, verified = EXCLUDED.verified;

INSERT INTO course (
  id, slug, thumbnail_url, preview_video_url, title, description, price, category,
  subtitle, language, level, status, created_at, updated_at, published_at,
  instructor_id, course_service_id, sync_status
) VALUES
  (101, 'python-data-science-bootcamp', 'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?auto=format&fit=crop&w=1200&q=80', 'https://www.youtube.com/watch?v=LHBE6Q9XlzI', 'Python Data Science Bootcamp', 'Analyze real datasets with Python, pandas, visualization, and practical statistics.', 2199, 'Data Science', 'From Python foundations to portfolio-ready analysis', 'English', 'BEGINNER', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '120 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '110 days', 'e1000000-0000-0000-0000-000000000001', 101, 'SYNCED'),
  (102, 'machine-learning-real-projects', 'https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?auto=format&fit=crop&w=1200&q=80', 'https://www.youtube.com/watch?v=7eh4d6sabA0', 'Machine Learning with Real Projects', 'Build, evaluate, and explain production-minded machine learning models.', 2999, 'Artificial Intelligence', 'Six end-to-end projects with scikit-learn', 'English', 'INTERMEDIATE', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '82 days', 'e1000000-0000-0000-0000-000000000001', 102, 'SYNCED'),
  (103, 'sql-analytics-mastery', 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80', 'https://www.youtube.com/watch?v=HXV3zeQKqGY', 'SQL Analytics Mastery', 'Write reliable analytical SQL using joins, windows, CTEs, and performance tuning.', 1299, 'Data Engineering', 'Turn business questions into trusted queries', 'English', 'INTERMEDIATE', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '70 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '65 days', 'e1000000-0000-0000-0000-000000000001', 103, 'SYNCED'),
  (104, 'docker-kubernetes-production', 'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1200&q=80', 'https://www.youtube.com/watch?v=X48VuDVv0do', 'Docker and Kubernetes in Production', 'Containerize applications and operate resilient workloads on Kubernetes.', 2799, 'DevOps', 'Ship dependable container platforms', 'English', 'INTERMEDIATE', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '105 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '98 days', 'e1000000-0000-0000-0000-000000000002', 104, 'SYNCED'),
  (105, 'aws-cloud-architecture', 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=1200&q=80', 'https://www.youtube.com/watch?v=ulprqHHWlng', 'AWS Cloud Architecture Essentials', 'Design secure, scalable, and cost-aware systems using core AWS services.', 2599, 'Cloud Computing', 'Architecture patterns for modern AWS workloads', 'English', 'BEGINNER', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '80 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '74 days', 'e1000000-0000-0000-0000-000000000002', 105, 'SYNCED'),
  (106, 'cicd-github-actions', 'https://images.unsplash.com/photo-1547658719-da2b51169166?auto=format&fit=crop&w=1200&q=80', 'https://www.youtube.com/watch?v=R8_veQiYBjI', 'CI/CD with GitHub Actions', 'Automate tests, builds, security checks, and deployments with reusable workflows.', 999, 'DevOps', 'Build a delivery pipeline from commit to production', 'English', 'BEGINNER', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '55 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '50 days', 'e1000000-0000-0000-0000-000000000002', 106, 'SYNCED')
ON CONFLICT (id) DO UPDATE SET
  thumbnail_url = EXCLUDED.thumbnail_url, preview_video_url = EXCLUDED.preview_video_url,
  title = EXCLUDED.title, description = EXCLUDED.description, price = EXCLUDED.price,
  category = EXCLUDED.category, subtitle = EXCLUDED.subtitle, language = EXCLUDED.language,
  level = EXCLUDED.level, status = EXCLUDED.status, instructor_id = EXCLUDED.instructor_id,
  course_service_id = EXCLUDED.course_service_id, sync_status = EXCLUDED.sync_status,
  updated_at = CURRENT_TIMESTAMP;

DELETE FROM course_tags WHERE course_id BETWEEN 101 AND 106;
INSERT INTO course_tags (course_id, tags) VALUES
  (101, 'Python'), (101, 'Pandas'), (101, 'Data Visualization'),
  (102, 'Machine Learning'), (102, 'scikit-learn'), (102, 'AI'),
  (103, 'SQL'), (103, 'Analytics'), (103, 'PostgreSQL'),
  (104, 'Docker'), (104, 'Kubernetes'), (104, 'Containers'),
  (105, 'AWS'), (105, 'Cloud'), (105, 'Architecture'),
  (106, 'GitHub Actions'), (106, 'CI/CD'), (106, 'Automation');

DELETE FROM content WHERE course_id BETWEEN 101 AND 106;
INSERT INTO content (id, title, type, duration, status, created_at, updated_at, course_id, instructor_id) VALUES
  ('f1010000-0000-0000-0000-000000000001', 'Foundations and environment setup', 'VIDEO', 48, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 101, 'e1000000-0000-0000-0000-000000000001'),
  ('f1010000-0000-0000-0000-000000000002', 'Exploratory analysis project', 'PROJECT', 95, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 101, 'e1000000-0000-0000-0000-000000000001'),
  ('f1020000-0000-0000-0000-000000000001', 'Supervised learning workflow', 'VIDEO', 62, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 102, 'e1000000-0000-0000-0000-000000000001'),
  ('f1020000-0000-0000-0000-000000000002', 'Model evaluation lab', 'ASSIGNMENT', 75, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 102, 'e1000000-0000-0000-0000-000000000001'),
  ('f1030000-0000-0000-0000-000000000001', 'Analytical SQL patterns', 'VIDEO', 54, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 103, 'e1000000-0000-0000-0000-000000000001'),
  ('f1030000-0000-0000-0000-000000000002', 'Business metrics challenge', 'QUIZ', 35, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 103, 'e1000000-0000-0000-0000-000000000001'),
  ('f1040000-0000-0000-0000-000000000001', 'Production container images', 'VIDEO', 58, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 104, 'e1000000-0000-0000-0000-000000000002'),
  ('f1040000-0000-0000-0000-000000000002', 'Kubernetes deployment lab', 'PROJECT', 110, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 104, 'e1000000-0000-0000-0000-000000000002'),
  ('f1050000-0000-0000-0000-000000000001', 'AWS well-architected foundations', 'VIDEO', 61, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 105, 'e1000000-0000-0000-0000-000000000002'),
  ('f1050000-0000-0000-0000-000000000002', 'Design a resilient web tier', 'ASSIGNMENT', 85, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 105, 'e1000000-0000-0000-0000-000000000002'),
  ('f1060000-0000-0000-0000-000000000001', 'Workflow syntax and runners', 'VIDEO', 46, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 106, 'e1000000-0000-0000-0000-000000000002'),
  ('f1060000-0000-0000-0000-000000000002', 'Release pipeline project', 'PROJECT', 90, 'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 106, 'e1000000-0000-0000-0000-000000000002');

DELETE FROM enrollment WHERE course_id BETWEEN 101 AND 106;
INSERT INTO enrollment (id, active, completion_rate, enrolled_at, grade, last_activity_at, status, student_id, course_id) VALUES
  ('a1010000-0000-0000-0000-000000000001', true, 72, CURRENT_TIMESTAMP - INTERVAL '40 days', 88, CURRENT_TIMESTAMP - INTERVAL '1 day', 'ACTIVE', 'd1000000-0000-0000-0000-000000000001', 101),
  ('a1010000-0000-0000-0000-000000000002', true, 48, CURRENT_TIMESTAMP - INTERVAL '28 days', 82, CURRENT_TIMESTAMP - INTERVAL '2 days', 'ACTIVE', 'd1000000-0000-0000-0000-000000000002', 101),
  ('a1020000-0000-0000-0000-000000000001', true, 33, CURRENT_TIMESTAMP - INTERVAL '20 days', 79, CURRENT_TIMESTAMP - INTERVAL '1 day', 'ACTIVE', 'd1000000-0000-0000-0000-000000000003', 102),
  ('a1030000-0000-0000-0000-000000000001', true, 91, CURRENT_TIMESTAMP - INTERVAL '50 days', 94, CURRENT_TIMESTAMP - INTERVAL '4 days', 'ACTIVE', 'd1000000-0000-0000-0000-000000000004', 103),
  ('a1040000-0000-0000-0000-000000000001', true, 56, CURRENT_TIMESTAMP - INTERVAL '35 days', 86, CURRENT_TIMESTAMP - INTERVAL '1 day', 'ACTIVE', 'd1000000-0000-0000-0000-000000000001', 104),
  ('a1050000-0000-0000-0000-000000000001', true, 22, CURRENT_TIMESTAMP - INTERVAL '12 days', 75, CURRENT_TIMESTAMP - INTERVAL '3 days', 'ACTIVE', 'd1000000-0000-0000-0000-000000000002', 105),
  ('a1060000-0000-0000-0000-000000000001', true, 84, CURRENT_TIMESTAMP - INTERVAL '45 days', 92, CURRENT_TIMESTAMP - INTERVAL '1 day', 'ACTIVE', 'd1000000-0000-0000-0000-000000000003', 106);

DELETE FROM review WHERE course_id BETWEEN 101 AND 106;
INSERT INTO review (id, comment, created_at, rating, student_id, course_id) VALUES
  ('b1010000-0000-0000-0000-000000000001', 'Clear explanations and practical notebooks.', CURRENT_TIMESTAMP - INTERVAL '8 days', 5, 'd1000000-0000-0000-0000-000000000001', 101),
  ('b1020000-0000-0000-0000-000000000001', 'The model evaluation section was especially useful.', CURRENT_TIMESTAMP - INTERVAL '6 days', 4.5, 'd1000000-0000-0000-0000-000000000003', 102),
  ('b1040000-0000-0000-0000-000000000001', 'Excellent production-focused Kubernetes examples.', CURRENT_TIMESTAMP - INTERVAL '5 days', 5, 'd1000000-0000-0000-0000-000000000001', 104),
  ('b1050000-0000-0000-0000-000000000001', 'Good balance between architecture and cost guidance.', CURRENT_TIMESTAMP - INTERVAL '3 days', 4.5, 'd1000000-0000-0000-0000-000000000002', 105);

DELETE FROM payment WHERE course_id BETWEEN 101 AND 106;
INSERT INTO payment (id, amount, paid_at, student_id, course_id) VALUES
  ('c1010000-0000-0000-0000-000000000001', 2199, CURRENT_TIMESTAMP - INTERVAL '40 days', 'd1000000-0000-0000-0000-000000000001', 101),
  ('c1010000-0000-0000-0000-000000000002', 2199, CURRENT_TIMESTAMP - INTERVAL '28 days', 'd1000000-0000-0000-0000-000000000002', 101),
  ('c1020000-0000-0000-0000-000000000001', 2999, CURRENT_TIMESTAMP - INTERVAL '20 days', 'd1000000-0000-0000-0000-000000000003', 102),
  ('c1030000-0000-0000-0000-000000000001', 1299, CURRENT_TIMESTAMP - INTERVAL '50 days', 'd1000000-0000-0000-0000-000000000004', 103),
  ('c1040000-0000-0000-0000-000000000001', 2799, CURRENT_TIMESTAMP - INTERVAL '35 days', 'd1000000-0000-0000-0000-000000000001', 104),
  ('c1050000-0000-0000-0000-000000000001', 2599, CURRENT_TIMESTAMP - INTERVAL '12 days', 'd1000000-0000-0000-0000-000000000002', 105),
  ('c1060000-0000-0000-0000-000000000001', 999, CURRENT_TIMESTAMP - INTERVAL '45 days', 'd1000000-0000-0000-0000-000000000003', 106);

SELECT setval('course_id_seq', GREATEST((SELECT MAX(id) FROM course), 1));
COMMIT;