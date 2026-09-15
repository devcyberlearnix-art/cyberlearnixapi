-- Baseline migration for existing tables
-- This script captures the current state of the database schema
-- This migration is idempotent - can be safely rerun

-- Existing courses table
-- Note: Existing structure is preserved, only adding missing constraints if needed

-- Existing sections table
-- Note: Existing structure is preserved

-- Existing lectures table  
-- Note: Existing structure is preserved

-- Existing enrollments table
-- Note: Existing structure is preserved

-- Add missing CASCADE constraints to existing tables
-- These are idempotent - use IF NOT EXISTS and DROP IF EXISTS

DO $$
BEGIN
    -- Check if constraint exists before adding
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_sections_course'
        AND table_name = 'sections'
    ) THEN
        ALTER TABLE sections ADD CONSTRAINT fk_sections_course 
            FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_lectures_section'
        AND table_name = 'lectures'
    ) THEN
        ALTER TABLE lectures ADD CONSTRAINT fk_lectures_section 
            FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_enrollments_course'
        AND table_name = 'enrollments'
    ) THEN
        ALTER TABLE enrollments ADD CONSTRAINT fk_enrollments_course 
            FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;
    END IF;
END $$;