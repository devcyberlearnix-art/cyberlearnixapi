-- Create course_requirements table
-- This migration is idempotent - can be safely rerun

CREATE TABLE IF NOT EXISTS course_requirements (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    requirement_type VARCHAR(50) NOT NULL,
    requirement_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_requirements_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Add index for faster queries (idempotent)
CREATE INDEX IF NOT EXISTS idx_course_requirements_course_id ON course_requirements(course_id);