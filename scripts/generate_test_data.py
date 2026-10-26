import uuid
import random
import os

random.seed(42)

def get_uuid(seed_str):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "cyberlearnix_" + seed_str))

NUM_STUDENTS = 80
NUM_INSTRUCTORS = 20
NUM_COURSES = 360
SECTIONS_PER_COURSE = 4
LECTURES_PER_SECTION = 4

STUDENTS = [f"student{i:03d}@cyberlearnix.test" for i in range(1, NUM_STUDENTS + 1)]
INSTRUCTORS = [f"instructor{i:03d}@cyberlearnix.test" for i in range(1, NUM_INSTRUCTORS + 1)]
PASSWORD_HASH = "$2a$10$IXHGtp2am7E0V2t/WiH4KeHrqqYiEV4HwKfODgOO3XIpicLdbAdUG"

first_names = ["Arjun", "Priya", "Rahul", "Ananya", "Kiran", "Sneha", "Vikram", "Neha", "Akhil", "Meghana", "Rohan", "Sanya", "Aarav", "Isha", "Ravi", "Kavya", "Dev", "Tara", "Yash", "Riya"]
last_names = ["Reddy", "Sharma", "Kumar", "Rao", "Varma", "Patel", "Singh", "Mehta", "Das", "Gupta", "Jain", "Bose", "Nair", "Iyer", "Pillai", "Menon", "Sen", "Roy", "Kapoor", "Chopra"]
categories = ["Java", "Spring Boot", "Microservices", "Python", "Cyber Security", "Data Structures", "SQL", "PostgreSQL", "Docker", "Kubernetes", "AWS", "React", "Node.js", "AI/ML"]

category_images = {
    "Java": "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=1400&q=85",
    "Spring Boot": "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=1400&q=85",
    "Microservices": "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=1400&q=85",
    "Python": "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?auto=format&fit=crop&w=1400&q=85",
    "Cyber Security": "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=1400&q=85",
    "Data Structures": "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=1400&q=85",
    "SQL": "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&w=1400&q=85",
    "PostgreSQL": "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&w=1400&q=85",
    "Docker": "https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?auto=format&fit=crop&w=1400&q=85",
    "Kubernetes": "https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?auto=format&fit=crop&w=1400&q=85",
    "AWS": "https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?auto=format&fit=crop&w=1400&q=85",
    "React": "https://images.unsplash.com/photo-1633356122544-f134324a6cee?auto=format&fit=crop&w=1400&q=85",
    "Node.js": "https://images.unsplash.com/photo-1633356122544-f134324a6cee?auto=format&fit=crop&w=1400&q=85",
    "AI/ML": "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?auto=format&fit=crop&w=1400&q=85"
}

courses = []
for i in range(1, NUM_COURSES + 1):
    status = 'PUBLISHED' if i <= 320 else 'DRAFT' if i <= 350 else 'ARCHIVED'
    courses.append({
        'id': i * 1000,
        'title': f"Premium Course {i}: {random.choice(categories)} Mastery",
        'category': random.choice(categories),
        'status': status,
        'instructor_email': random.choice(INSTRUCTORS),
        'price': random.choice([0, 499, 999, 1499, 1999, 2499, 2999]),
        'level': random.choice(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']),
    })

def write_users():
    lines = ["BEGIN;"]
    
    for student in STUDENTS:
        uid = get_uuid(student)
        fname = random.choice(first_names)
        lname = random.choice(last_names)
        lines.append(f"""
INSERT INTO users (id, email, password, first_name, last_name, status, role, applied_role, application_status, is_instructor_approved, created_at, updated_at)
SELECT '{uid}', '{student}', '{PASSWORD_HASH}', '{fname}', '{lname}', 'ACTIVE', 'STUDENT', 'STUDENT', 'APPROVED', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = '{student}');
""")

    for instructor in INSTRUCTORS:
        uid = get_uuid(instructor)
        fname = random.choice(first_names)
        lname = random.choice(last_names)
        lines.append(f"""
INSERT INTO users (id, email, password, first_name, last_name, status, role, applied_role, application_status, is_instructor_approved, created_at, updated_at)
SELECT '{uid}', '{instructor}', '{PASSWORD_HASH}', '{fname}', '{lname}', 'ACTIVE', 'INSTRUCTOR', 'INSTRUCTOR', 'APPROVED', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = '{instructor}');
""")

    lines.append("COMMIT;")
    with open("../seed_test_users.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines))

def write_instructors():
    lines = ["BEGIN;"]
    for email in INSTRUCTORS:
        uid = get_uuid("instructor_" + email)
        user_id = get_uuid(email)
        fname = random.choice(first_names)
        lname = random.choice(last_names)
        lines.append(f"""
INSERT INTO instructor (id, user_id, name, email, headline, rating, verified)
SELECT '{uid}', '{user_id}', '{fname} {lname}', '{email}', 'Expert Instructor', 4.5, true
WHERE NOT EXISTS (SELECT 1 FROM instructor WHERE email = '{email}');
""")
    for c in courses:
        inst_email = c['instructor_email']
        inst_id = get_uuid("instructor_" + inst_email)
        thumbnail = category_images.get(c['category'], 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=1400&q=85')
        lines.append(f"""
INSERT INTO course (id, title, description, price, category, status, instructor_id, course_service_id, sync_status, thumbnail_url, created_at, updated_at)
SELECT {c['id']}, '{c['title']}', 'Description for {c['title']}', {c['price']}, '{c['category']}', '{c['status']}', '{inst_id}', {c['id']}, 'SYNCED', '{thumbnail}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = {c['id']});
""")
    lines.append("COMMIT;")
    with open("../seed_test_instructors.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines))

def write_courses():
    lines = ["BEGIN;"]
    for c in courses:
        inst_id = get_uuid(c['instructor_email'])
        thumbnail = category_images.get(c['category'], 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=1400&q=85')
        lines.append(f"""
INSERT INTO courses (id, title, description, category, level, language, price, thumbnail, instructor_id, status, premium, search_count, view_count)
SELECT {c['id']}, '{c['title']}', 'Detailed description for {c['title']}', '{c['category']}', '{c['level']}', 'English', {c['price']}, '{thumbnail}', '{inst_id}', '{c['status']}', false, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE id = {c['id']});
""")
        if c['status'] == 'PUBLISHED':
            for s in range(1, SECTIONS_PER_COURSE + 1):
                section_title = f"Section {s} for {c['title']}"
                lines.append(f"""
INSERT INTO sections (course_id, title, order_index)
SELECT {c['id']}, '{section_title}', {s}
WHERE NOT EXISTS (SELECT 1 FROM sections WHERE course_id = {c['id']} AND title = '{section_title}');
""")
                for l in range(1, LECTURES_PER_SECTION + 1):
                    lecture_title = f"Lecture {l} in Section {s}"
                    lines.append(f"""
INSERT INTO lectures (section_id, title, duration, order_index, type)
SELECT s.id, '{lecture_title}', 15, {l}, 'VIDEO'
FROM sections s WHERE s.course_id = {c['id']} AND s.title = '{section_title}'
AND NOT EXISTS (SELECT 1 FROM lectures l2 WHERE l2.section_id = s.id AND l2.title = '{lecture_title}');
""")
    lines.append("COMMIT;")
    with open("../seed_test_courses.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines))

def write_enrollments():
    lines = ["BEGIN;"]
    
    for student in STUDENTS:
        uid = get_uuid(student)
        # 4 enrollments per student = 320 enrollments
        enrolled = random.sample([c for c in courses if c['status'] == 'PUBLISHED'], 4)
        for c in enrolled:
            progress = random.choice([0, 10, 25, 50, 75, 100])
            lines.append(f"""
INSERT INTO enrollment (course_id, student_id, progress, status, enrolled_at)
SELECT {c['id']}, '{uid}', {progress}, 'ACTIVE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM enrollment WHERE course_id = {c['id']} AND student_id = '{uid}');

INSERT INTO lecture_progress (student_id, course_id, lecture_id, status, progress_percentage, started_at, time_spent_seconds)
SELECT '{uid}', {c['id']}, l.id, 'COMPLETED', 100, CURRENT_TIMESTAMP, 600
FROM lectures l
JOIN sections s ON l.section_id = s.id
WHERE s.course_id = {c['id']}
AND NOT EXISTS (SELECT 1 FROM lecture_progress lp WHERE lp.student_id = '{uid}' AND lp.lecture_id = l.id)
LIMIT 1;
""")
    lines.append("COMMIT;")
    with open("../seed_test_enrollments.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines))

def write_wishlists():
    lines = ["BEGIN;"]
    for student in STUDENTS:
        uid = get_uuid(student)
        # 3 wishlist items per student = 240 items
        wishlisted = random.sample([c for c in courses if c['status'] == 'PUBLISHED'], 3)
        for c in wishlisted:
            w_id = get_uuid(f"wish_{uid}_{c['id']}")
            lines.append(f"""
INSERT INTO wishlist_items (id, course_id, user_id, added_at)
SELECT '{w_id}', {c['id']}, '{uid}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM wishlist_items WHERE user_id = '{uid}' AND course_id = {c['id']});
""")
    lines.append("COMMIT;")
    with open("../seed_test_wishlists.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines))

def write_carts():
    lines = ["BEGIN;"]
    for student in STUDENTS:
        uid = get_uuid(student)
        # 2 cart items per student = 160 items
        carted = random.sample([c for c in courses if c['status'] == 'PUBLISHED'], 2)
        for c in carted:
            lines.append(f"""
INSERT INTO cart_items (course_id, user_id, price, quantity, course_name)
SELECT {c['id']}, '{uid}', {c['price']}, 1, '{c['title']}'
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE user_id = '{uid}' AND course_id = {c['id']});
""")
    lines.append("COMMIT;")
    with open("../seed_test_carts.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines))

def write_reviews():
    lines = ["BEGIN;"]
    # 250 reviews
    review_students = STUDENTS * 5
    published_courses = [c for c in courses if c['status'] == 'PUBLISHED']
    for i in range(250):
        student = review_students[i % len(review_students)]
        uid = get_uuid(student)
        c = random.choice(published_courses)
        r_id = get_uuid(f"review_{uid}_{c['id']}_{i}")
        lines.append(f"""
INSERT INTO reviews (course_id, user_id, review_uuid, rating, comment, status, created_at, updated_at)
SELECT {c['id']}, '{uid}', '{r_id}', 5, 'Great course!', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE course_id = {c['id']} AND user_id = '{uid}');
""")
    lines.append("COMMIT;")
    with open("../seed_test_reviews.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines))

def write_coupons():
    lines = ["BEGIN;"]
    for i in range(1, 21):
        c_id = get_uuid(f"coupon_{i}")
        code = f"DISCOUNT{i*5}"
        lines.append(f"""
INSERT INTO coupons (id, code, discount_type, discount_value, status, is_active, start_time, end_time, expiry_date, usage_limit, used_count)
SELECT '{c_id}', '{code}', 'PERCENTAGE', {i*5}, 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', CURRENT_TIMESTAMP + INTERVAL '30 days', 100, 0
WHERE NOT EXISTS (SELECT 1 FROM coupons WHERE code = '{code}');
""")
    lines.append("COMMIT;")
    with open("../seed_test_coupons.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines))

def write_orders_and_payments():
    lines_orders = ["BEGIN;"]
    lines_payments = ["BEGIN;"]
    # 110 orders and payments
    order_students = STUDENTS * 2
    for i in range(110):
        student = order_students[i % len(order_students)]
        uid = get_uuid(student)
        o_id = get_uuid(f"order_{uid}_{i}")
        c = courses[i % len(courses)]
        amount = c['price'] if c['price'] > 0 else 999
        
        lines_orders.append(f"""
INSERT INTO orders (order_id, user_id, status, total_amount, created_at)
SELECT '{o_id}', '{uid}', 'COMPLETED', {amount}, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_id = '{o_id}');

INSERT INTO order_item (id, order_id, course_id)
SELECT '{get_uuid("oi_"+o_id)}', '{o_id}', {c['id']}
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE id = '{get_uuid("oi_"+o_id)}');
""")
        p_id = f"PAY-{o_id[:6]}-{i}"
        lines_payments.append(f"""
INSERT INTO payments (amount, course_id, payer_user_id, status, txn_id, created_at)
SELECT {amount}, '{c['id']}', '{uid}', 'SUCCESS', '{p_id}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE txn_id = '{p_id}');
""")
    
    lines_orders.append("COMMIT;")
    lines_payments.append("COMMIT;")
    with open("../seed_test_orders.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines_orders))
    with open("../seed_test_payments.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines_payments))

def write_notifications():
    lines = ["BEGIN;"]
    for student in STUDENTS:
        uid = get_uuid(student)
        for i in range(3):
            n_id = get_uuid(f"notif_{uid}_{i}")
            lines.append(f"""
INSERT INTO notifications (id, title, message, status, priority, is_deleted, created_at)
SELECT '{n_id}', 'Notification {i}', 'Message for notification {i}', 'DELIVERED', 'HIGH', false, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE id = '{n_id}');
""")
    lines.append("COMMIT;")
    with open("../seed_test_notifications.sql", "w", encoding="utf-8") as f: f.write("\n".join(lines))


if __name__ == "__main__":
    # Ensure working dir is right
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    write_users()
    write_instructors()
    write_courses()
    write_enrollments()
    write_wishlists()
    write_carts()
    write_reviews()
    write_coupons()
    write_orders_and_payments()
    write_notifications()
    print("Test data SQL files regenerated successfully.")
