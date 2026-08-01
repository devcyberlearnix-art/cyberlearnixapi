-- ============================================================
-- RBAC Migration Script: Role Updates
-- ============================================================
-- Purpose: Migrate legacy role names to new RBAC structure
-- Date: 2026-08-01
-- 
-- Old Roles -> New Roles:
--   ADMIN -> MAIN_ADMIN
--   SUPER_ADMIN -> MAIN_ADMIN (consolidated)
--   SUB_ADMIN -> SUB_ADMIN (already exists)
--   STUDENT -> STUDENT (no change)
--   INSTRUCTOR -> INSTRUCTOR (no change)
-- ============================================================

-- ============================================================
-- USERS TABLE MIGRATION
-- ============================================================

-- Update ADMIN role to MAIN_ADMIN
UPDATE users 
SET role = 'MAIN_ADMIN' 
WHERE role = 'ADMIN';

-- Update SUPER_ADMIN role to MAIN_ADMIN (consolidate into MAIN_ADMIN)
UPDATE users 
SET role = 'MAIN_ADMIN' 
WHERE role = 'SUPER_ADMIN';

-- Verify the migration
SELECT role, COUNT(*) as count 
FROM users 
GROUP BY role 
ORDER BY role;

-- ============================================================
-- ADMINS TABLE MIGRATION (if exists)
-- ============================================================

-- Update ADMIN role to MAIN_ADMIN in admins table
UPDATE admins 
SET admin_type = 'MAIN_ADMIN' 
WHERE admin_type = 'ADMIN';

-- Update SUPER_ADMIN role to MAIN_ADMIN in admins table
UPDATE admins 
SET admin_type = 'MAIN_ADMIN' 
WHERE admin_type = 'SUPER_ADMIN';

-- Verify the migration
SELECT admin_type, COUNT(*) as count 
FROM admins 
GROUP BY admin_type 
ORDER BY admin_type;

-- ============================================================
-- AUDIT LOG ENTRY
-- ============================================================

-- Create audit log entry for this migration
INSERT INTO audit_log (action, entity_type, description, performed_at, performed_by)
VALUES ('ROLE_MIGRATION', 'USER', 'Migrated legacy roles (ADMIN, SUPER_ADMIN) to MAIN_ADMIN', NOW(), 'SYSTEM');

-- ============================================================
-- ROLLBACK SCRIPT (in case of issues)
-- ============================================================

-- To rollback, run:
-- UPDATE users SET role = 'ADMIN' WHERE role = 'MAIN_ADMIN';
-- UPDATE admins SET admin_type = 'ADMIN' WHERE admin_type = 'MAIN_ADMIN';

-- ============================================================
-- VERIFICATION QUERIES
-- ============================================================

-- Check for any remaining legacy roles
SELECT 'Legacy roles in users table' as check_type, 
       COUNT(*) as count 
FROM users 
WHERE role IN ('ADMIN', 'SUPER_ADMIN');

SELECT 'Legacy roles in admins table' as check_type, 
       COUNT(*) as count 
FROM admins 
WHERE admin_type IN ('ADMIN', 'SUPER_ADMIN');

-- Show current role distribution
SELECT 'Current user role distribution' as check_type,
       role, 
       COUNT(*) as count 
FROM users 
GROUP BY role 
ORDER BY count DESC;

SELECT 'Current admin type distribution' as check_type,
       admin_type, 
       COUNT(*) as count 
FROM admins 
GROUP BY admin_type 
ORDER BY count DESC;
