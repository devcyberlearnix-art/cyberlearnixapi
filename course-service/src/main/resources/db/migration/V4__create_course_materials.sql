-- Create course_materials table
-- This migration is idempotent - can be safely rerun

CREATE TABLE IF NOT EXISTS course_materials (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    material_name VARCHAR(255) NOT NULL,
    material_type VARCHAR(50) NOT NULL,
    file_url TEXT NOT NULL,
    file_size BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_materials_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- Add index for faster queries (idempotent)
CREATE INDEX IF NOT EXISTS idx_course_materials_course_id ON course_materials(course_id);