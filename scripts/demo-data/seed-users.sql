BEGIN;

UPDATE users SET
    profile_photo = CASE email
        WHEN 'demo.student1@cyberlearnix.com' THEN 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=500&q=80'
        WHEN 'demo.student2@cyberlearnix.com' THEN 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=500&q=80'
        WHEN 'demo.student3@cyberlearnix.com' THEN 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=500&q=80'
        WHEN 'demo.instructor1@cyberlearnix.com' THEN 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=500&q=80'
        WHEN 'demo.instructor2@cyberlearnix.com' THEN 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=500&q=80'
        ELSE profile_photo
    END,
    updated_at = CURRENT_TIMESTAMP
WHERE email IN (
    'demo.student1@cyberlearnix.com',
    'demo.student2@cyberlearnix.com',
    'demo.student3@cyberlearnix.com',
    'demo.instructor1@cyberlearnix.com',
    'demo.instructor2@cyberlearnix.com'
);

INSERT INTO users (
    id, email, password, first_name, last_name, dob, profile_photo, city, state,
    country, preferred_language, organization, skills, field_of_study,
    highest_qualification, status, role, applied_role, application_status,
    is_instructor_approved, failed_login_attempts, mobile, country_code,
    provider, created_at, updated_at
)
SELECT seed.id, seed.email, source.password, seed.first_name, seed.last_name,
       seed.dob, seed.profile_photo, seed.city, seed.state, seed.country,
       seed.preferred_language, seed.organization, seed.skills,
       seed.field_of_study, seed.highest_qualification, 'ACTIVE', seed.role,
       seed.role, 'APPROVED', seed.role = 'INSTRUCTOR', 0, seed.mobile, '+91',
       'LOCAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    VALUES
      ('d1000000-0000-0000-0000-000000000001'::uuid, 'maya.student@merqora.com', 'Maya', 'Patel', '2001-05-18', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=500&q=80', 'Ahmedabad', 'Gujarat', 'India', 'English', 'Merqora Academy', 'Java, SQL, Git', 'Computer Science', 'B.Tech', 'STUDENT', '+919810000101'),
      ('d1000000-0000-0000-0000-000000000002'::uuid, 'arjun.student@merqora.com', 'Arjun', 'Mehta', '2000-11-02', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=500&q=80', 'Pune', 'Maharashtra', 'India', 'English', 'Merqora Academy', 'React, JavaScript, CSS', 'Information Technology', 'B.Sc', 'STUDENT', '+919810000102'),
      ('d1000000-0000-0000-0000-000000000003'::uuid, 'sofia.student@merqora.com', 'Sofia', 'Khan', '2002-02-14', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=500&q=80', 'Hyderabad', 'Telangana', 'India', 'English', 'Merqora Academy', 'Python, Data Analysis, Excel', 'Data Science', 'BCA', 'STUDENT', '+919810000103'),
      ('d1000000-0000-0000-0000-000000000004'::uuid, 'liam.student@merqora.com', 'Liam', 'Chen', '1999-08-27', 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=500&q=80', 'Bengaluru', 'Karnataka', 'India', 'English', 'Merqora Academy', 'Docker, Linux, Cloud', 'Software Engineering', 'MCA', 'STUDENT', '+919810000104'),
      ('d2000000-0000-0000-0000-000000000001'::uuid, 'priya.instructor@merqora.com', 'Priya', 'Nair', '1990-04-12', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=500&q=80', 'Kochi', 'Kerala', 'India', 'English', 'Merqora Learning', 'Python, Machine Learning, Statistics', 'Artificial Intelligence', 'M.Tech', 'INSTRUCTOR', '+919820000201'),
      ('d2000000-0000-0000-0000-000000000002'::uuid, 'daniel.instructor@merqora.com', 'Daniel', 'Brooks', '1988-09-21', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=500&q=80', 'Bengaluru', 'Karnataka', 'India', 'English', 'Merqora Learning', 'DevOps, AWS, Kubernetes', 'Cloud Computing', 'M.Sc', 'INSTRUCTOR', '+919820000202')
) AS seed(id, email, first_name, last_name, dob, profile_photo, city, state,
          country, preferred_language, organization, skills, field_of_study,
          highest_qualification, role, mobile)
CROSS JOIN (SELECT password FROM users WHERE email = 'demo.student1@cyberlearnix.com' LIMIT 1) source
ON CONFLICT (id) DO UPDATE SET
    email = EXCLUDED.email,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    profile_photo = EXCLUDED.profile_photo,
    city = EXCLUDED.city,
    state = EXCLUDED.state,
    country = EXCLUDED.country,
    preferred_language = EXCLUDED.preferred_language,
    organization = EXCLUDED.organization,
    skills = EXCLUDED.skills,
    field_of_study = EXCLUDED.field_of_study,
    highest_qualification = EXCLUDED.highest_qualification,
    status = EXCLUDED.status,
    role = EXCLUDED.role,
    is_instructor_approved = EXCLUDED.is_instructor_approved,
    updated_at = CURRENT_TIMESTAMP;

COMMIT;