"""
Dummy Data Generator for LMS Platform
Generates users, instructors, courses, and enrollments with proper relationships
"""

import random
import uuid
from datetime import datetime, timedelta
import psycopg2
from psycopg2 import sql
import json

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'port': 15432,
    'user': 'cyberlearnix',
    'password': 'cyberlearnix123',
}

# Sample data
FIRST_NAMES = ['John', 'Jane', 'Michael', 'Sarah', 'David', 'Emily', 'Robert', 'Lisa', 'James', 'Jennifer', 'William', 'Amanda', 'Christopher', 'Jessica', 'Daniel', 'Ashley', 'Matthew', 'Stephanie', 'Andrew', 'Nicole']
LAST_NAMES = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Wilson', 'Anderson', 'Taylor', 'Thomas', 'Moore', 'Jackson', 'Martin', 'Lee', 'Thompson', 'White']
CITIES = ['New York', 'Los Angeles', 'Chicago', 'Houston', 'Phoenix', 'Philadelphia', 'San Antonio', 'San Diego', 'Dallas', 'San Jose', 'Austin', 'Jacksonville', 'Fort Worth', 'Columbus', 'Charlotte', 'San Francisco', 'Indianapolis', 'Seattle', 'Denver', 'Washington']
STATES = ['California', 'Texas', 'New York', 'Florida', 'Illinois', 'Pennsylvania', 'Ohio', 'Georgia', 'North Carolina', 'Michigan', 'New Jersey', 'Virginia', 'Washington', 'Arizona', 'Massachusetts', 'Tennessee', 'Indiana', 'Missouri', 'Maryland', 'Wisconsin']
COUNTRIES = ['United States', 'Canada', 'United Kingdom', 'Australia', 'Germany', 'France', 'India', 'Singapore', 'Japan', 'Brazil']
LANGUAGES = ['English', 'Spanish', 'French', 'German', 'Chinese', 'Japanese', 'Korean', 'Portuguese', 'Hindi', 'Arabic']
ORGANIZATIONS = ['Google', 'Microsoft', 'Amazon', 'Apple', 'Meta', 'Netflix', 'Twitter', 'LinkedIn', 'IBM', 'Oracle', 'SAP', 'Salesforce', 'Adobe', 'Intel', 'Cisco']
SKILLS = ['Python', 'Java', 'JavaScript', 'React', 'Angular', 'Node.js', 'Spring Boot', 'Docker', 'Kubernetes', 'AWS', 'Azure', 'Machine Learning', 'Data Science', 'DevOps', 'Cybersecurity', 'Mobile Development', 'UI/UX Design', 'Project Management']
FIELD_OF_STUDY = ['Computer Science', 'Information Technology', 'Software Engineering', 'Data Science', 'Computer Engineering', 'Electrical Engineering', 'Mathematics', 'Physics', 'Business Administration', 'Economics']
QUALIFICATIONS = ['High School', 'Bachelor\'s Degree', 'Master\'s Degree', 'PhD', 'Postdoctoral', 'Professional Certification']
COURSE_CATEGORIES = ['Programming', 'Web Development', 'Data Science', 'Machine Learning', 'Mobile Development', 'DevOps', 'Cybersecurity', 'Cloud Computing', 'AI & Robotics', 'Game Development']
COURSE_LEVELS = ['Beginner', 'Intermediate', 'Advanced', 'Expert']
COURSE_STATUSES = ['Published', 'Draft', 'Archived']
USER_STATUSES = ['ACTIVE', 'PENDING_VERIFICATION', 'LOCKED', 'SUSPENDED']
USER_ROLES = ['STUDENT', 'INSTRUCTOR', 'MAIN_ADMIN', 'SUB_ADMIN']
APPLICATION_STATUSES = ['PENDING', 'APPROVED', 'REJECTED']

# Sample thumbnail and video URLs (using placeholder services)
THUMBNAIL_URLS = [
    'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400',
    'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=400',
    'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400',
    'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=400',
    'https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=400',
    'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=400',
    'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=400',
    'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400',
    'https://images.unsplash.com/photo-1504639725590-34d0984388bd?w=400',
    'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400'
]

VIDEO_URLS = [
    'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
    'https://www.youtube.com/watch?v=jNQXAC9IVRw',
    'https://www.youtube.com/watch?v=9bZkp7q19f0',
    'https://www.youtube.com/watch?v=kJQP7kiw5Fk',
    'https://www.youtube.com/watch?v=fJ9rUzIMcZQ',
    'https://www.youtube.com/watch?v=rfscVS0vtbw',
    'https://www.youtube.com/watch?v=YPiQtdtTUiE',
    'https://www.youtube.com/watch?v=kqt5xAToVvM',
    'https://www.youtube.com/watch?v=tgbNymZ7vqY',
    'https://www.youtube.com/watch?v=8aGhZQkoFbQ',
    'https://www.youtube.com/watch?v=Ct6BUPvE2s',
    'https://www.youtube.com/watch?v=HGOBQPFInwk',
    'https://www.youtube.com/watch?v=xQ4W_M9aE7Y',
    'https://www.youtube.com/watch?v=7cKieB-bxrY',
    'https://www.youtube.com/watch?v=6z8SGaCkM8o'
]

PROFILE_PHOTO_URLS = [
    'https://randomuser.me/api/portraits/men/1.jpg',
    'https://randomuser.me/api/portraits/women/1.jpg',
    'https://randomuser.me/api/portraits/men/2.jpg',
    'https://randomuser.me/api/portraits/women/2.jpg',
    'https://randomuser.me/api/portraits/men/3.jpg',
    'https://randomuser.me/api/portraits/women/3.jpg',
    'https://randomuser.me/api/portraits/men/4.jpg',
    'https://randomuser.me/api/portraits/women/4.jpg',
    'https://randomuser.me/api/portraits/men/5.jpg',
    'https://randomuser.me/api/portraits/women/5.jpg'
]

COURSE_TITLES = [
    'Complete Python Masterclass',
    'Web Development Bootcamp',
    'Machine Learning A-Z',
    'React - The Complete Guide',
    'Angular Zero to Hero',
    'Node.js API Development',
    'Spring Boot Microservices',
    'Docker & Kubernetes Essentials',
    'AWS Cloud Practitioner',
    'Cybersecurity Fundamentals',
    'Data Science with Python',
    'Mobile App Development with Flutter',
    'DevOps Engineer Bootcamp',
    'Artificial Intelligence Basics',
    'Game Development with Unity'
]

COURSE_SUBTITLES = [
    'Learn from scratch with hands-on projects',
    'Build real-world applications from start to finish',
    'Comprehensive guide to modern development',
    'Master the latest technologies and frameworks',
    'Professional training for career advancement',
    'Step-by-step learning path for beginners',
    'Advanced concepts for experienced developers',
    'Industry-standard practices and patterns',
    'Job-ready skills and portfolio projects',
    'Expert-led instruction with practical examples'
]

BANK_NAMES = ['Chase', 'Bank of America', 'Wells Fargo', 'Citibank', 'TD Bank', 'PNC Bank', 'Capital One', 'US Bank', 'BB&T', 'SunTrust']

def generate_password():
    """Generate a simple password for dummy data"""
    return 'Password123!'

def generate_email(first_name, last_name):
    """Generate email from name"""
    domains = ['gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com', 'icloud.com']
    return f"{first_name.lower()}.{last_name.lower()}@{random.choice(domains)}"

def generate_phone():
    """Generate US phone number"""
    return f"+1{random.randint(200, 999)}{random.randint(200, 999)}{random.randint(1000, 9999)}"

def generate_date_within_years(years=5):
    """Generate datetime within past N years"""
    days = years * 365
    random_days = random.randint(0, days)
    return datetime.now() - timedelta(days=random_days)

def connect_to_database(db_name):
    """Connect to PostgreSQL database"""
    try:
        conn = psycopg2.connect(
            host=DB_CONFIG['host'],
            port=DB_CONFIG['port'],
            user=DB_CONFIG['user'],
            password=DB_CONFIG['password'],
            dbname=db_name
        )
        conn.autocommit = True
        print(f"Connected to database: {db_name}")
        return conn
    except Exception as e:
        print(f"Error connecting to database {db_name}: {e}")
        return None

def generate_users(count=20):
    """Generate dummy users"""
    users = []
    for i in range(count):
        first_name = random.choice(FIRST_NAMES)
        last_name = random.choice(LAST_NAMES)
        
        user = {
            'id': str(uuid.uuid4()),
            'first_name': first_name,
            'last_name': last_name,
            'email': generate_email(first_name, last_name),
            'password': generate_password(),
            'dob': generate_date_within_years(30).strftime('%Y-%m-%d'),
            'profile_photo': random.choice(PROFILE_PHOTO_URLS),
            'city': random.choice(CITIES),
            'state': random.choice(STATES),
            'country': random.choice(COUNTRIES),
            'preferred_language': random.choice(LANGUAGES),
            'organization': random.choice(ORGANIZATIONS),
            'skills': ', '.join(random.sample(SKILLS, random.randint(2, 5))),
            'field_of_study': random.choice(FIELD_OF_STUDY),
            'highest_qualification': random.choice(QUALIFICATIONS),
            'status': random.choice(USER_STATUSES),
            'role': random.choice(USER_ROLES),
            'applied_role': random.choice(USER_ROLES) if random.random() > 0.5 else None,
            'failed_login_attempts': random.randint(0, 3),
            'application_status': random.choice(APPLICATION_STATUSES) if random.random() > 0.7 else None,
            'is_instructor_approved': random.choice([True, False]) if random.random() > 0.7 else False,
            'last_login': generate_date_within_years(1),
            'updated_at': generate_date_within_years(1),
            'last_login_at': generate_date_within_years(1),
            'ip_address': f"{random.randint(1, 255)}.{random.randint(1, 255)}.{random.randint(1, 255)}.{random.randint(1, 255)}",
            'device': random.choice(['Desktop', 'Mobile', 'Tablet']),
            'browser': random.choice(['Chrome', 'Firefox', 'Safari', 'Edge']),
            'os': random.choice(['Windows', 'MacOS', 'Linux', 'Android', 'iOS']),
            'mobile': generate_phone(),
            'mobile_hash': '',
            'user_agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            'provider': random.choice(['LOCAL', 'GOOGLE', 'GITHUB', 'LINKEDIN']) if random.random() > 0.8 else 'LOCAL',
            'provider_id': str(uuid.uuid4()) if random.random() > 0.8 else None,
            'created_at': generate_date_within_years(3),
            'locked_until': None,
            'reset_token': None,
            'reset_token_expiry': None,
            'country_code': '+1'
        }
        users.append(user)
    return users

def insert_users(conn, users):
    """Insert users into database"""
    if not conn:
        return False
    
    try:
        cursor = conn.cursor()
        query = """
            INSERT INTO users (
                id, first_name, last_name, email, password, dob, profile_photo, 
                city, state, country, preferred_language, organization, skills, 
                field_of_study, highest_qualification, status, role, applied_role, 
                failed_login_attempts, application_status, is_instructor_approved, 
                last_login, updated_at, last_login_at, ip_address, device, browser, 
                os, mobile, mobile_hash, user_agent, provider, provider_id, 
                created_at, locked_until, reset_token, reset_token_expiry, country_code
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
            )
        """
        
        for user in users:
            cursor.execute(query, (
                user['id'], user['first_name'], user['last_name'], user['email'], 
                user['password'], user['dob'], user['profile_photo'], user['city'], 
                user['state'], user['country'], user['preferred_language'], 
                user['organization'], user['skills'], user['field_of_study'], 
                user['highest_qualification'], user['status'], user['role'], 
                user['applied_role'], user['failed_login_attempts'], 
                user['application_status'], user['is_instructor_approved'], 
                user['last_login'], user['updated_at'], user['last_login_at'], 
                user['ip_address'], user['device'], user['browser'], user['os'], 
                user['mobile'], user['mobile_hash'], user['user_agent'], 
                user['provider'], user['provider_id'], user['created_at'], 
                user['locked_until'], user['reset_token'], user['reset_token_expiry'], 
                user['country_code']
            ))
        
        print(f"Inserted {len(users)} users into database")
        cursor.close()
        return True
    except Exception as e:
        print(f"Error inserting users: {e}")
        return False

def generate_instructor_applications(user_ids, count=10):
    """Generate instructor applications"""
    applications = []
    selected_users = random.sample(user_ids, min(count, len(user_ids)))
    
    for user_id in selected_users:
        app = {
            'id': str(uuid.uuid4()),
            'user_id': user_id,
            'status': random.choice(APPLICATION_STATUSES),
            'resume_path': f'/uploads/resumes/{uuid.uuid4()}.pdf',
            'educational_certificates_path': f'/uploads/certificates/{uuid.uuid4()}.pdf',
            'government_id_proof_path': f'/uploads/ids/{uuid.uuid4()}.pdf',
            'experience_letter_path': f'/uploads/experience/{uuid.uuid4()}.pdf',
            'internship_certificate_path': f'/uploads/internships/{uuid.uuid4()}.pdf',
            'skill_certificates_path': f'/uploads/skills/{uuid.uuid4()}.pdf',
            'portfolio_path': f'/uploads/portfolios/{uuid.uuid4()}.pdf',
            'demo_lecture_ppt_path': f'/uploads/demos/{uuid.uuid4()}.pptx',
            'demo_lecture_recording_path': f'/uploads/recordings/{uuid.uuid4()}.mp4',
            'projects_path': f'/uploads/projects/{uuid.uuid4()}.zip',
            'passport_photo_path': f'/uploads/photos/{uuid.uuid4()}.jpg',
            'bank_details_path': f'/uploads/bank/{uuid.uuid4()}.pdf',
            'pan_document_path': f'/uploads/pan/{uuid.uuid4()}.pdf',
            'application_form_path': f'/uploads/forms/{uuid.uuid4()}.pdf',
            'bank_account_number': f"{random.randint(1000000000, 9999999999)}",
            'bank_ifsc': f"{random.choice(['HDFC', 'ICICI', 'SBI', 'AXIS'])}{random.randint(1000, 9999)}",
            'bank_name': random.choice(BANK_NAMES),
            'pan_number': f"{random.choice(['ABCDE', 'FGHIJ', 'KLMNO'])}{random.randint(1000, 9999)}{random.choice(['A', 'B', 'C'])}",
            'additional_notes': random.choice(['Experienced in teaching', 'Industry professional', 'PhD holder', 'Corporate trainer', 'Freelance instructor']),
            'submitted_at': generate_date_within_years(2),
            'reviewed_at': generate_date_within_years(1) if random.random() > 0.5 else None,
            'reviewed_by': str(random.choice(user_ids)) if random.random() > 0.5 else None,
            'rejection_reason': random.choice(['Incomplete documentation', 'Insufficient experience', 'Other']) if random.random() > 0.8 else None
        }
        applications.append(app)
    return applications

def insert_instructor_applications(conn, applications):
    """Insert instructor applications into database"""
    if not conn:
        return False
    
    try:
        cursor = conn.cursor()
        query = """
            INSERT INTO instructor_applications (
                id, user_id, status, resume_path, educational_certificates_path, 
                government_id_proof_path, experience_letter_path, internship_certificate_path, 
                skill_certificates_path, portfolio_path, demo_lecture_ppt_path, 
                demo_lecture_recording_path, projects_path, passport_photo_path, 
                bank_details_path, pan_document_path, application_form_path, 
                bank_account_number, bank_ifsc, bank_name, pan_number, additional_notes, 
                submitted_at, reviewed_at, reviewed_by, rejection_reason
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 
                %s, %s, %s, %s, %s, %s, %s, %s, %s
            )
        """
        
        for app in applications:
            cursor.execute(query, (
                app['id'], app['user_id'], app['status'], app['resume_path'], 
                app['educational_certificates_path'], app['government_id_proof_path'], 
                app['experience_letter_path'], app['internship_certificate_path'], 
                app['skill_certificates_path'], app['portfolio_path'], 
                app['demo_lecture_ppt_path'], app['demo_lecture_recording_path'], 
                app['projects_path'], app['passport_photo_path'], app['bank_details_path'], 
                app['pan_document_path'], app['application_form_path'], 
                app['bank_account_number'], app['bank_ifsc'], app['bank_name'], 
                app['pan_number'], app['additional_notes'], app['submitted_at'], 
                app['reviewed_at'], app['reviewed_by'], app['rejection_reason']
            ))
        
        print(f"Inserted {len(applications)} instructor applications into database")
        cursor.close()
        return True
    except Exception as e:
        print(f"Error inserting instructor applications: {e}")
        return False

def generate_courses(instructor_ids, count=15):
    """Generate dummy courses"""
    courses = []
    for i in range(count):
        course = {
            'title': random.choice(COURSE_TITLES),
            'subtitle': random.choice(COURSE_SUBTITLES),
            'description': f"This comprehensive {random.choice(COURSE_CATEGORIES).lower()} course covers everything from fundamentals to advanced concepts. Includes hands-on projects, real-world examples, and industry best practices.",
            'category': random.choice(COURSE_CATEGORIES),
            'level': random.choice(COURSE_LEVELS),
            'language': random.choice(LANGUAGES),
            'price': round(random.uniform(9.99, 199.99), 2),
            'thumbnail': random.choice(THUMBNAIL_URLS),
            'instructor_id': random.choice(instructor_ids),
            'status': random.choice(COURSE_STATUSES),
            'premium': random.choice([True, False]),
            'search_count': random.randint(0, 10000),
            'view_count': random.randint(0, 50000)
        }
        courses.append(course)
    return courses

def insert_courses(conn, courses):
    """Insert courses into database"""
    if not conn:
        return False
    
    try:
        cursor = conn.cursor()
        query = """
            INSERT INTO courses (
                title, subtitle, description, category, level, language, 
                price, thumbnail, instructor_id, status, premium, search_count, view_count
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
            ) RETURNING id
        """
        
        course_ids = []
        for course in courses:
            cursor.execute(query, (
                course['title'], course['subtitle'], course['description'], 
                course['category'], course['level'], course['language'], 
                course['price'], course['thumbnail'], course['instructor_id'], 
                course['status'], course['premium'], course['search_count'], 
                course['view_count']
            ))
            course_id = cursor.fetchone()[0]
            course_ids.append(course_id)
        
        print(f"Inserted {len(courses)} courses into database")
        cursor.close()
        return course_ids
    except Exception as e:
        print(f"Error inserting courses: {e}")
        return False

def generate_enrollments(student_ids, course_ids, count=30):
    """Generate dummy enrollments"""
    enrollments = []
    for i in range(count):
        enrollment = {
            'student_id': random.choice(student_ids),
            'course_id': random.choice(course_ids),
            'student_name': f"{random.choice(FIRST_NAMES)} {random.choice(LAST_NAMES)}",
            'enrolled_at': generate_date_within_years(2),
            'status': random.choice(['ACTIVE', 'COMPLETED', 'DROPPED', 'IN_PROGRESS']),
            'progress': round(random.uniform(0, 100), 2)
        }
        enrollments.append(enrollment)
    return enrollments

def check_table_exists(conn, table_name):
    """Check if a table exists in the database"""
    try:
        cursor = conn.cursor()
        cursor.execute("""
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_name = %s
            )
        """, (table_name,))
        exists = cursor.fetchone()[0]
        cursor.close()
        return exists
    except Exception as e:
        print(f"Error checking table existence: {e}")
        return False

def generate_sections(course_ids, sections_per_course=5):
    """Generate dummy sections for courses"""
    sections = []
    section_titles = [
        'Introduction', 'Getting Started', 'Core Concepts', 'Advanced Topics', 
        'Practical Examples', 'Best Practices', 'Troubleshooting', 'Case Studies',
        'Final Project', 'Assessment', 'Bonus Content', 'Resources'
    ]
    
    for course_id in course_ids:
        num_sections = random.randint(3, sections_per_course)
        for i in range(num_sections):
            section = {
                'title': f"{random.choice(section_titles)} - Part {i+1}",
                'order_index': i + 1,
                'course_id': course_id
            }
            sections.append(section)
    return sections

def insert_sections(conn, sections):
    """Insert sections into database"""
    if not conn:
        return False
    
    # Check if sections table exists
    if not check_table_exists(conn, 'sections'):
        print("Sections table does not exist. Skipping section data.")
        return False
    
    try:
        cursor = conn.cursor()
        query = """
            INSERT INTO sections (title, order_index, course_id)
            VALUES (%s, %s, %s)
            RETURNING id
        """
        
        section_ids = []
        for section in sections:
            cursor.execute(query, (
                section['title'], 
                section['order_index'], 
                section['course_id']
            ))
            section_id = cursor.fetchone()[0]
            section_ids.append(section_id)
        
        print(f"Inserted {len(sections)} sections into database")
        cursor.close()
        return section_ids
    except Exception as e:
        print(f"Error inserting sections: {e}")
        return False

def generate_lectures(section_ids, lectures_per_section=4):
    """Generate dummy lectures with video URLs"""
    lectures = []
    lecture_titles = [
        'Overview and Setup', 'Core Fundamentals', 'Deep Dive Analysis',
        'Hands-on Tutorial', 'Real-world Implementation', 'Advanced Techniques',
        'Performance Optimization', 'Security Considerations', 'Testing Strategies',
        'Deployment Guide', 'Maintenance Tips', 'Scaling Solutions'
    ]
    
    lecture_descriptions = [
        'Learn the fundamental concepts and setup your environment',
        'Comprehensive guide to core principles and best practices',
        'Advanced analysis and in-depth exploration of the topic',
        'Step-by-step tutorial with practical examples',
        'Real-world implementation scenarios and case studies',
        'Advanced techniques for experienced developers',
        'Optimization strategies for better performance',
        'Security best practices and considerations',
        'Testing methodologies and quality assurance',
        'Complete deployment guide for production',
        'Maintenance and troubleshooting tips',
        'Scaling strategies for growing applications'
    ]
    
    for section_id in section_ids:
        num_lectures = random.randint(2, lectures_per_section)
        for i in range(num_lectures):
            lecture = {
                'title': random.choice(lecture_titles),
                'description': random.choice(lecture_descriptions),
                'video_url': random.choice(VIDEO_URLS),
                'duration': random.randint(10, 120),  # 10-120 minutes
                'order_index': i + 1,
                'preview_enabled': random.choice([True, False]),
                'resources': json.dumps({
                    'slides': f'/resources/slides/{uuid.uuid4()}.pdf',
                    'code': f'/resources/code/{uuid.uuid4()}.zip',
                    'notes': f'/resources/notes/{uuid.uuid4()}.pdf'
                }),
                'section_id': section_id
            }
            lectures.append(lecture)
    return lectures

def insert_lectures(conn, lectures):
    """Insert lectures into database"""
    if not conn:
        return False
    
    # Check if lectures table exists
    if not check_table_exists(conn, 'lectures'):
        print("Lectures table does not exist. Skipping lecture data.")
        return False
    
    try:
        cursor = conn.cursor()
        query = """
            INSERT INTO lectures (title, description, video_url, duration, order_index, preview_enabled, resources, section_id)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """
        
        for lecture in lectures:
            cursor.execute(query, (
                lecture['title'], 
                lecture['description'], 
                lecture['video_url'], 
                lecture['duration'], 
                lecture['order_index'], 
                lecture['preview_enabled'], 
                lecture['resources'], 
                lecture['section_id']
            ))
        
        print(f"Inserted {len(lectures)} lectures into database")
        cursor.close()
        return True
    except Exception as e:
        print(f"Error inserting lectures: {e}")
        return False

def insert_enrollments(conn, enrollments):
    """Insert enrollments into database"""
    if not conn:
        return False
    
    # Check if enrollments table exists
    if not check_table_exists(conn, 'enrollments'):
        print("Enrollments table does not exist. Skipping enrollment data.")
        print("Note: The table will be created when you run the course-service application.")
        return False
    
    try:
        cursor = conn.cursor()
        query = """
            INSERT INTO enrollments (
                student_id, course_id, student_name, enrolled_at, status, progress
            ) VALUES (
                %s, %s, %s, %s, %s, %s
            )
        """
        
        for enrollment in enrollments:
            cursor.execute(query, (
                enrollment['student_id'], enrollment['course_id'], 
                enrollment['student_name'], enrollment['enrolled_at'], 
                enrollment['status'], enrollment['progress']
            ))
        
        print(f"Inserted {len(enrollments)} enrollments into database")
        cursor.close()
        return True
    except Exception as e:
        print(f"Error inserting enrollments: {e}")
        return False

def main():
    """Main function to generate all dummy data"""
    print("Starting dummy data generation...")
    
    # Connect to user database
    user_conn = connect_to_database('lms_user_db')
    if not user_conn:
        print("Failed to connect to user database. Exiting.")
        return
    
    # Connect to course database
    course_conn = connect_to_database('lms_course_db')
    if not course_conn:
        print("Failed to connect to course database. Exiting.")
        user_conn.close()
        return
    
    try:
        # Generate and insert users
        print("\nGenerating users...")
        users = generate_users(count=25)
        if insert_users(user_conn, users):
            print("[OK] Users inserted successfully")
        
        user_ids = [user['id'] for user in users]
        
        # Get instructor IDs (users with INSTRUCTOR role or approved instructors)
        instructor_ids = [user['id'] for user in users if user['role'] == 'INSTRUCTOR' or user['is_instructor_approved']]
        if not instructor_ids:
            # Fallback: use some user IDs as instructors
            instructor_ids = user_ids[:10]
        
        # Generate and insert instructor applications
        print("\nGenerating instructor applications...")
        instructor_apps = generate_instructor_applications(user_ids, count=8)
        if insert_instructor_applications(user_conn, instructor_apps):
            print("[OK] Instructor applications inserted successfully")
        
        # Generate and insert courses
        print("\nGenerating courses...")
        courses = generate_courses(instructor_ids, count=20)
        course_ids = insert_courses(course_conn, courses)
        if course_ids:
            print("[OK] Courses inserted successfully")
        
        # Generate and insert sections
        print("\nGenerating sections...")
        sections = generate_sections(course_ids, sections_per_course=5)
        section_ids = insert_sections(course_conn, sections)
        if section_ids:
            print("[OK] Sections inserted successfully")
        else:
            section_ids = []
            sections = []
        
        # Generate and insert lectures with video content
        print("\nGenerating lectures with video content...")
        lectures = generate_lectures(section_ids, lectures_per_section=4)
        lecture_result = insert_lectures(course_conn, lectures)
        lecture_count = len(lectures) if lecture_result else 0
        if lecture_result:
            print("[OK] Lectures inserted successfully")
        else:
            print("[INFO] Lectures skipped (table doesn't exist yet)")
        
        # Generate and insert enrollments
        print("\nGenerating enrollments...")
        enrollments = generate_enrollments(user_ids, course_ids, count=40)
        enrollment_result = insert_enrollments(course_conn, enrollments)
        enrollment_count = len(enrollments) if enrollment_result else 0
        if enrollment_result:
            print("[OK] Enrollments inserted successfully")
        else:
            print("[INFO] Enrollments skipped (table doesn't exist yet)")
        
        print("\n" + "="*50)
        print("Dummy data generation completed successfully!")
        print("="*50)
        print(f"Summary:")
        print(f"- Users: {len(users)}")
        print(f"- Instructor Applications: {len(instructor_apps)}")
        print(f"- Courses: {len(courses)}")
        print(f"- Sections: {len(sections)}")
        print(f"- Lectures: {lecture_count}")
        print(f"- Enrollments: {enrollment_count}")
        
    except Exception as e:
        print(f"Error during data generation: {e}")
    finally:
        user_conn.close()
        course_conn.close()
        print("\nDatabase connections closed.")

if __name__ == "__main__":
    main()