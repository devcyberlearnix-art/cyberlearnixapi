-- Create learning_outcomes table
-- This migration is idempotent - can be safely rerun

CREATE TABLE IF NOT EXISTS learning_outcomes (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    outcome_text TEXT NOT NULL,
    skill_category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_learning_outcomes_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Add index for faster queries (idempotent)
CREATE INDEX IF NOT EXISTS idx_learning_outcomes_course_id ON learning_outcomes(course_id);