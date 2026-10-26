BEGIN;
SELECT pg_advisory_xact_lock(hashtext('cyberlearnix.demo-course-instructors.v1'));

DO $seed$
BEGIN
    IF EXISTS (
        SELECT 1 FROM instructor i
        JOIN (VALUES
            ('e2100000-0000-0000-0000-000000000001'::uuid, 'd2100000-0000-0000-0000-000000000001'::uuid, 'Ananya Rao', 'ananya.rao@cyberlearnix.example'),
            ('e2100000-0000-0000-0000-000000000002'::uuid, 'd2100000-0000-0000-0000-000000000002'::uuid, 'Mateo Silva', 'mateo.silva@cyberlearnix.example'),
            ('e2100000-0000-0000-0000-000000000003'::uuid, 'd2100000-0000-0000-0000-000000000003'::uuid, 'Samira Okafor', 'samira.okafor@cyberlearnix.example'),
            ('e2100000-0000-0000-0000-000000000004'::uuid, 'd2100000-0000-0000-0000-000000000004'::uuid, 'Jordan Kim', 'jordan.kim@cyberlearnix.example'),
            ('e2100000-0000-0000-0000-000000000005'::uuid, 'd2100000-0000-0000-0000-000000000005'::uuid, 'Priya Nair', 'priya.nair@cyberlearnix.example')
        ) AS seed(id, user_id, name, email)
          ON i.id = seed.id OR i.user_id = seed.user_id OR i.email = seed.email
        WHERE i.id IS DISTINCT FROM seed.id
           OR i.user_id IS DISTINCT FROM seed.user_id
           OR i.name IS DISTINCT FROM seed.name
           OR i.email IS DISTINCT FROM seed.email
    ) THEN
        RAISE EXCEPTION 'A demo instructor ID, user ID, or email conflicts with existing data';
    END IF;
END
$seed$;

INSERT INTO instructor (id, user_id, name, email, headline, rating, verified)
SELECT seed.id, seed.user_id, seed.name, seed.email, seed.headline, seed.rating, TRUE
FROM (VALUES
    ('e2100000-0000-0000-0000-000000000001'::uuid, 'd2100000-0000-0000-0000-000000000001'::uuid, 'Ananya Rao', 'ananya.rao@cyberlearnix.example', 'Java backend engineer and Spring Boot educator', 4.9),
    ('e2100000-0000-0000-0000-000000000002'::uuid, 'd2100000-0000-0000-0000-000000000002'::uuid, 'Mateo Silva', 'mateo.silva@cyberlearnix.example', 'Full-stack Python and Django practitioner', 4.8),
    ('e2100000-0000-0000-0000-000000000003'::uuid, 'd2100000-0000-0000-0000-000000000003'::uuid, 'Samira Okafor', 'samira.okafor@cyberlearnix.example', 'Application security and incident response specialist', 4.9),
    ('e2100000-0000-0000-0000-000000000004'::uuid, 'd2100000-0000-0000-0000-000000000004'::uuid, 'Jordan Kim', 'jordan.kim@cyberlearnix.example', 'Algorithms instructor focused on clear problem solving', 4.8),
    ('e2100000-0000-0000-0000-000000000005'::uuid, 'd2100000-0000-0000-0000-000000000005'::uuid, 'Priya Nair', 'priya.nair@cyberlearnix.example', 'AWS and Kubernetes platform architect', 4.9)
) AS seed(id, user_id, name, email, headline, rating)
WHERE NOT EXISTS (SELECT 1 FROM instructor existing WHERE existing.id = seed.id OR existing.user_id = seed.user_id OR existing.email = seed.email)
ON CONFLICT DO NOTHING;

COMMIT;