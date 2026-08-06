-- Create all databases for CyberLearnix LMS microservices.
-- PostgreSQL does not support CREATE DATABASE IF NOT EXISTS,
-- so we use conditional CREATE statements via psql \gexec.

SELECT 'CREATE DATABASE lms_user_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_user_db')\gexec

SELECT 'CREATE DATABASE lms_admin_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_admin_db')\gexec

SELECT 'CREATE DATABASE lms_course_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_course_db')\gexec

SELECT 'CREATE DATABASE lms_cart_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_cart_db')\gexec

SELECT 'CREATE DATABASE lms_coupon_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_coupon_db')\gexec

SELECT 'CREATE DATABASE lms_order_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_order_db')\gexec

SELECT 'CREATE DATABASE lms_payment_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_payment_db')\gexec

SELECT 'CREATE DATABASE lms_review_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_review_db')\gexec

SELECT 'CREATE DATABASE lms_instructor_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_instructor_db')\gexec

SELECT 'CREATE DATABASE lms_wishlist_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_wishlist_db')\gexec

SELECT 'CREATE DATABASE lms_notification_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lms_notification_db')\gexec
