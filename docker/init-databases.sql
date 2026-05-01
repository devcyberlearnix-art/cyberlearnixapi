-- CyberLearnix LMS – Database-per-Service initialization
-- This script runs once when the PostgreSQL container starts for the first time.
-- Each microservice owns its own isolated database – no cross-service table access.

-- userservice  → owns all user, session, instructor data
CREATE DATABASE lms_user_db;

-- course-service → owns all course, section, lecture data
CREATE DATABASE lms_course_db;

-- cart-service   → owns cart and cart-item data
CREATE DATABASE lms_cart_db;

-- coupon-service → owns coupon and redemption data
CREATE DATABASE lms_coupon_db;

-- wishlist-service → owns wishlist data
CREATE DATABASE lms_wishlist;

-- order-service → owns order, order-item, refund data
CREATE DATABASE lms_order_db;

-- Grant all privileges to the shared app user
GRANT ALL PRIVILEGES ON DATABASE lms_user_db   TO cyberlearnix;
GRANT ALL PRIVILEGES ON DATABASE lms_course_db TO cyberlearnix;
GRANT ALL PRIVILEGES ON DATABASE lms_cart_db   TO cyberlearnix;
GRANT ALL PRIVILEGES ON DATABASE lms_coupon_db TO cyberlearnix;
GRANT ALL PRIVILEGES ON DATABASE lms_wishlist  TO cyberlearnix;
GRANT ALL PRIVILEGES ON DATABASE lms_order_db  TO cyberlearnix;
