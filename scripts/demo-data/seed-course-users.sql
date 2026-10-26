BEGIN;
SELECT pg_advisory_xact_lock(hashtext('cyberlearnix.demo-course-users.v1'));

DO $seed$
BEGIN
    IF EXISTS (
        SELECT 1 FROM users u
        JOIN (VALUES
            ('d2100000-0000-0000-0000-000000000001'::uuid, 'ananya.rao@cyberlearnix.example', 'Ananya', 'Rao'),
            ('d2100000-0000-0000-0000-000000000002'::uuid, 'mateo.silva@cyberlearnix.example', 'Mateo', 'Silva'),
            ('d2100000-0000-0000-0000-000000000003'::uuid, 'samira.okafor@cyberlearnix.example', 'Samira', 'Okafor'),
            ('d2100000-0000-0000-0000-000000000004'::uuid, 'jordan.kim@cyberlearnix.example', 'Jordan', 'Kim'),
            ('d2100000-0000-0000-0000-000000000005'::uuid, 'priya.nair@cyberlearnix.example', 'Priya', 'Nair')
        ) AS seed(id, email, first_name, last_name)
          ON u.id = seed.id OR u.email = seed.email
        WHERE u.id IS DISTINCT FROM seed.id
           OR u.email IS DISTINCT FROM seed.email
           OR u.first_name IS DISTINCT FROM seed.first_name
           OR u.last_name IS DISTINCT FROM seed.last_name
    ) THEN
        RAISE EXCEPTION 'A demo instructor user ID or email conflicts with existing data';
    END IF;
END
$seed$;

INSERT INTO users (id, email, first_name, last_name, profile_photo, city, state, country,
                   preferred_language, organization, skills, field_of_study, highest_qualification,
                   status, applied_role, application_status, role, is_instructor_approved,
                   failed_login_attempts, provider, created_at, updated_at)
SELECT seed.id, seed.email, seed.first_name, seed.last_name, seed.profile_photo,
       seed.city, seed.state, 'India', 'English', 'CyberLearnix Learning', seed.skills,
       seed.field_of_study, seed.highest_qualification, 'PENDING_VERIFICATION',
       'INSTRUCTOR', 'APPROVED', 'INSTRUCTOR', TRUE, 0, 'SEED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('d2100000-0000-0000-0000-000000000001'::uuid, 'ananya.rao@cyberlearnix.example', 'Ananya', 'Rao', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=500&q=80', 'Bengaluru', 'Karnataka', 'Java, Spring Boot, PostgreSQL, distributed systems', 'Software Engineering', 'M.Tech'),
    ('d2100000-0000-0000-0000-000000000002'::uuid, 'mateo.silva@cyberlearnix.example', 'Mateo', 'Silva', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=500&q=80', 'Pune', 'Maharashtra', 'Python, Django, React, API design', 'Computer Science', 'M.Sc'),
    ('d2100000-0000-0000-0000-000000000003'::uuid, 'samira.okafor@cyberlearnix.example', 'Samira', 'Okafor', 'https://images.unsplash.com/photo-1531123897727-8f129e1688ce?auto=format&fit=crop&w=500&q=80', 'Hyderabad', 'Telangana', 'Cybersecurity, incident response, application security', 'Information Security', 'M.Sc'),
    ('d2100000-0000-0000-0000-000000000004'::uuid, 'jordan.kim@cyberlearnix.example', 'Jordan', 'Kim', 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=500&q=80', 'Chennai', 'Tamil Nadu', 'Java, algorithms, data structures, interview preparation', 'Computer Science', 'B.Tech'),
    ('d2100000-0000-0000-0000-000000000005'::uuid, 'priya.nair@cyberlearnix.example', 'Priya', 'Nair', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=500&q=80', 'Kochi', 'Kerala', 'AWS, Kubernetes, Terraform, site reliability', 'Cloud Computing', 'M.Tech')
) AS seed(id, email, first_name, last_name, profile_photo, city, state, skills, field_of_study, highest_qualification)
WHERE NOT EXISTS (SELECT 1 FROM users existing WHERE existing.id = seed.id OR existing.email = seed.email)
ON CONFLICT DO NOTHING;

COMMIT;