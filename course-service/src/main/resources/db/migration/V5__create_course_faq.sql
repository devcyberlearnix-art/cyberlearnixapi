-- Create course_faq table
-- This migration is idempotent - can be safely rerun

CREATE TABLE IF NOT EXISTS course_faq (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    display_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_faq_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Add index for faster queries (idempotent)
CREATE INDEX IF NOT EXISTS idx_course_faq_course_id ON course_faq(course_id);
CREATE INDEX IF NOT EXISTS idx_course_faq_display_order ON course_faq(display_order);