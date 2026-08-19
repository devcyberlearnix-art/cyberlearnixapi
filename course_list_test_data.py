"""
Course List API Test Data Generator
Creates comprehensive test data for testing GET /api/v1/courses/list with all filters and sorts
"""

import psycopg2
import uuid
from datetime import datetime

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'port': 15432,
    'user': 'cyberlearnix',
    'password': 'cyberlearnix123',
    'database': 'lms_course_db'
}

def connect_to_database():
    """Connect to PostgreSQL database"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        conn.autocommit = True
        print("Connected to database: lms_course_db")
        return conn
    except Exception as e:
        print(f"Error connecting to database: {e}")
        return None

def get_instructor_ids(conn):
    """Get existing instructor IDs from users database"""
    try:
        # Connect to user database to get instructor IDs
        user_conn = psycopg2.connect(
            host='localhost',
            port=15432,
            user='cyberlearnix',
            password='cyberlearnix123',
            database='lms_user_db'
        )
        user_conn.autocommit = True
        
        cursor = user_conn.cursor()
        cursor.execute("""
            SELECT id FROM users 
            WHERE role = 'INSTRUCTOR' OR is_instructor_approved = true 
            LIMIT 5
        """)
        
        instructor_ids = [row[0] for row in cursor.fetchall()]
        cursor.close()
        user_conn.close()
        
        if not instructor_ids:
            print("No instructors found, using default UUID")
            return [uuid.uuid4()]
        
        print(f"Found {len(instructor_ids)} instructor IDs")
        return instructor_ids
    except Exception as e:
        print(f"Error getting instructor IDs: {e}")
        return [uuid.uuid4()]

def clear_existing_test_courses(conn):
    """Clear existing courses to start fresh with test data"""
    try:
        cursor = conn.cursor()
        cursor.execute("DELETE FROM enrollments WHERE course_id IN (SELECT id FROM courses WHERE title LIKE '%TEST%')")
        cursor.execute("DELETE FROM sections WHERE course_id IN (SELECT id FROM courses WHERE title LIKE '%TEST%')")
        cursor.execute("DELETE FROM courses WHERE title LIKE '%TEST%'")
        print("Cleared existing test courses")
        cursor.close()
    except Exception as e:
        print(f"Error clearing test courses: {e}")

def create_test_courses(conn, instructor_ids):
    """Create comprehensive test courses for Course List API testing"""
    
    courses = [
        # SEARCH TESTS - Java in title/subtitle/description
        {
            'title': 'Advanced Java Programming',
            'subtitle': 'Master Java fundamentals and advanced concepts',
            'description': 'Comprehensive Java course covering all aspects of Java programming language.',
            'category': 'Programming',
            'level': 'Advanced',
            'language': 'English',
            'price': 99.99,
            'thumbnail': 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 1000,
            'view_count': 100
        },
        {
            'title': 'Java for Beginners',
            'subtitle': 'Learn Java from scratch',
            'description': 'Introduction to Java programming for absolute beginners.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'English',
            'price': 0.00,
            'thumbnail': 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 500,
            'view_count': 200
        },
        {
            'title': 'Python Web Development',
            'subtitle': 'Build web applications with Python',
            'description': 'Learn Java integration in Python web applications.',
            'category': 'Programming',
            'level': 'Intermediate',
            'language': 'English',
            'price': 49.99,
            'thumbnail': 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 300,
            'view_count': 400
        },
        # SEARCH TESTS - Python in title/description
        {
            'title': 'Data Science with Python',
            'subtitle': 'Python for data analysis and machine learning',
            'description': 'Complete Python course for data science and machine learning applications.',
            'category': 'Data Science',
            'level': 'Intermediate',
            'language': 'English',
            'price': 79.99,
            'thumbnail': 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 800,
            'view_count': 600
        },
        {
            'title': 'Python Machine Learning',
            'subtitle': 'Machine learning algorithms with Python',
            'description': 'Advanced Python course for machine learning and AI.',
            'category': 'Machine Learning',
            'level': 'Advanced',
            'language': 'English',
            'price': 149.99,
            'thumbnail': 'https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 200,
            'view_count': 1000
        },
        # Non-Java/Python course for negative search testing
        {
            'title': 'React Development Masterclass',
            'subtitle': 'Build modern web applications with React',
            'description': 'Complete React course covering hooks, state management, and more.',
            'category': 'Programming',
            'level': 'Intermediate',
            'language': 'English',
            'price': 89.99,
            'thumbnail': 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 600,
            'view_count': 300
        },
        # CATEGORY TESTS - Multiple categories
        {
            'title': 'UI/UX Design Fundamentals',
            'subtitle': 'Design beautiful user interfaces',
            'description': 'Learn user interface and user experience design principles.',
            'category': 'Design',
            'level': 'Beginner',
            'language': 'English',
            'price': 39.99,
            'thumbnail': 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 400,
            'view_count': 350
        },
        {
            'title': 'Advanced UI/UX Design',
            'subtitle': 'Professional design techniques',
            'description': 'Advanced UI/UX design for professional applications.',
            'category': 'Design',
            'level': 'Advanced',
            'language': 'English',
            'price': 69.99,
            'thumbnail': 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 250,
            'view_count': 150
        },
        {
            'title': 'DevOps Engineering',
            'subtitle': 'CI/CD and cloud infrastructure',
            'description': 'Complete DevOps course covering Docker, Kubernetes, and cloud platforms.',
            'category': 'DevOps',
            'level': 'Intermediate',
            'language': 'English',
            'price': 129.99,
            'thumbnail': 'https://images.unsplash.com/photo-1504639725590-34d0984388bd?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 350,
            'view_count': 450
        },
        {
            'title': 'Cyber Security Essentials',
            'subtitle': 'Network and application security',
            'description': 'Fundamental cybersecurity concepts and practices.',
            'category': 'Cyber Security',
            'level': 'Beginner',
            'language': 'English',
            'price': 59.99,
            'thumbnail': 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 450,
            'view_count': 250
        },
        # LEVEL TESTS - Multiple levels in same category
        {
            'title': 'JavaScript for Beginners',
            'subtitle': 'JavaScript fundamentals',
            'description': 'Introduction to JavaScript programming for beginners.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'English',
            'price': 29.99,
            'thumbnail': 'https://images.unsplash.com/photo-1579468118864-1b9ea3c0db4a?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 700,
            'view_count': 500
        },
        {
            'title': 'JavaScript Advanced Patterns',
            'subtitle': 'Advanced JavaScript concepts',
            'description': 'Advanced JavaScript patterns and best practices.',
            'category': 'Programming',
            'level': 'Advanced',
            'language': 'English',
            'price': 79.99,
            'thumbnail': 'https://images.unsplash.com/photo-1627398242454-45a1465c2479?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 150,
            'view_count': 80
        },
        # LANGUAGE TESTS - Multiple languages
        {
            'title': 'Web Development in Hindi',
            'subtitle': 'Full stack web development',
            'description': 'Complete web development course taught in Hindi.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'Hindi',
            'price': 19.99,
            'thumbnail': 'https://images.unsplash.com/photo-1497215728101-856f4ea42174?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 300,
            'view_count': 200
        },
        {
            'title': 'Python en Español',
            'subtitle': 'Python programming in Spanish',
            'description': 'Complete Python course taught in Spanish.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'Spanish',
            'price': 24.99,
            'thumbnail': 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 200,
            'view_count': 150
        },
        # PRICE TESTS - Various price points
        {
            'title': 'Free Angular Course',
            'subtitle': 'Angular framework basics',
            'description': 'Free introduction to Angular framework.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'English',
            'price': 0.00,
            'thumbnail': 'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 900,
            'view_count': 700
        },
        {
            'title': 'Low Cost CSS Course',
            'subtitle': 'CSS styling techniques',
            'description': 'Affordable CSS course for beginners.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'English',
            'price': 25.00,
            'thumbnail': 'https://images.unsplash.com/photo-1507721999472-8ed4421c4af2?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 550,
            'view_count': 400
        },
        {
            'title': 'Mid-Range Node.js Course',
            'subtitle': 'Server-side JavaScript',
            'description': 'Node.js backend development course.',
            'category': 'Programming',
            'level': 'Intermediate',
            'language': 'English',
            'price': 50.00,
            'thumbnail': 'https://images.unsplash.com/photo-1627398242454-45a1465c2479?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 650,
            'view_count': 550
        },
        {
            'title': 'High-End AWS Course',
            'subtitle': 'Amazon Web Services',
            'description': 'Comprehensive AWS cloud computing course.',
            'category': 'DevOps',
            'level': 'Advanced',
            'language': 'English',
            'price': 100.00,
            'thumbnail': 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 750,
            'view_count': 600
        },
        {
            'title': 'Premium Docker Course',
            'subtitle': 'Container orchestration',
            'description': 'Advanced Docker and Kubernetes training.',
            'category': 'DevOps',
            'level': 'Advanced',
            'language': 'English',
            'price': 250.00,
            'thumbnail': 'https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 400,
            'view_count': 300
        },
        {
            'title': 'Enterprise Spring Boot',
            'subtitle': 'Enterprise Java development',
            'description': 'Enterprise-level Spring Boot application development.',
            'category': 'Programming',
            'level': 'Advanced',
            'language': 'English',
            'price': 500.00,
            'thumbnail': 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 300,
            'view_count': 200
        },
        {
            'title': 'Ultimate Cloud Course',
            'subtitle': 'Multi-cloud architecture',
            'description': 'Advanced multi-cloud architecture and deployment.',
            'category': 'DevOps',
            'level': 'Expert',
            'language': 'English',
            'price': 1000.00,
            'thumbnail': 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 200,
            'view_count': 100
        },
        # POPULAR FORMULA TEST COURSES
        # Formula: (searchCount * 0.6) + (viewCount * 0.4)
        # Course A: 1000*0.6 + 100*0.4 = 600 + 40 = 640
        # Course B: 100*0.6 + 1000*0.4 = 60 + 400 = 460
        # Course C: 500*0.6 + 500*0.4 = 300 + 200 = 500
        {
            'title': 'TEST - High Search Course',
            'subtitle': 'For POPULAR sort testing - high search count',
            'description': 'Test course with high search count for POPULAR sort verification.',
            'category': 'Programming',
            'level': 'Intermediate',
            'language': 'English',
            'price': 75.00,
            'thumbnail': 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 1000,
            'view_count': 100
        },
        {
            'title': 'TEST - High View Course',
            'subtitle': 'For POPULAR sort testing - high view count',
            'description': 'Test course with high view count for POPULAR sort verification.',
            'category': 'Programming',
            'level': 'Intermediate',
            'language': 'English',
            'price': 75.00,
            'thumbnail': 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 100,
            'view_count': 1000
        },
        {
            'title': 'TEST - Balanced Course',
            'subtitle': 'For POPULAR sort testing - balanced counts',
            'description': 'Test course with balanced search and view counts for POPULAR sort verification.',
            'category': 'Programming',
            'level': 'Intermediate',
            'language': 'English',
            'price': 75.00,
            'thumbnail': 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 500,
            'view_count': 500
        },
        # COMPLEX FILTER TEST COURSE - Matches all filters
        {
            'title': 'TEST - Java Multi-Filter Course',
            'subtitle': 'Matches complex filter combination',
            'description': 'Test course designed to match complex filter combinations for testing.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'English',
            'price': 45.00,
            'thumbnail': 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 600,
            'view_count': 400
        },
        {
            'title': 'TEST - Java Multi-Filter Course 2',
            'subtitle': 'Matches complex filter combination',
            'description': 'Another test course for complex filter combination testing.',
            'category': 'Design',
            'level': 'Intermediate',
            'language': 'English',
            'price': 55.00,
            'thumbnail': 'https://images.unsplash.com/photo-1587620962725-abab7fe55159?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 550,
            'view_count': 350
        },
        {
            'title': 'TEST - Java Multi-Filter Course 3',
            'subtitle': 'Matches complex filter combination',
            'description': 'Third test course for complex filter combination testing.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'English',
            'price': 35.00,
            'thumbnail': 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 450,
            'view_count': 300
        },
        {
            'title': 'TEST - Java Multi-Filter Course 4',
            'subtitle': 'Matches complex filter combination',
            'description': 'Fourth test course for complex filter combination testing.',
            'category': 'Programming',
            'level': 'Intermediate',
            'language': 'English',
            'price': 65.00,
            'thumbnail': 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 700,
            'view_count': 500
        },
        {
            'title': 'TEST - Java Multi-Filter Course 5',
            'subtitle': 'Matches complex filter combination',
            'description': 'Fifth test course for complex filter combination testing.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'English',
            'price': 85.00,
            'thumbnail': 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 800,
            'view_count': 600
        },
        # COURSE THAT FAILS SOME FILTERS - for AND logic testing
        {
            'title': 'TEST - Filter Failure Course',
            'subtitle': 'Intentionally fails some filters',
            'description': 'Test course designed to fail specific filters for AND logic verification.',
            'category': 'Data Science',  # Not Programming/Design
            'level': 'Advanced',  # Not Beginner/Intermediate
            'language': 'Hindi',  # Not English
            'price': 600.00,  # Not in 10-500 range
            'thumbnail': 'https://images.unsplash.com/photo-1504639725590-34d0984388bd?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': True,  # Not premium=false
            'search_count': 100,
            'view_count': 50
        },
        # COURSE THAT MATCHES COMPLEX FILTER COMBINATION
        # search=java + category=programming,design + level=beginner,intermediate + language=english + minPrice=10 + maxPrice=500 + premium=false + paid=true + sort=POPULAR
        {
            'title': 'TEST - Perfect Match Course',
            'subtitle': 'Matches all complex filter criteria',
            'description': 'Java course designed to match the complete complex filter combination for testing.',
            'category': 'Programming',  # Matches programming,design
            'level': 'Beginner',  # Matches beginner,intermediate
            'language': 'English',  # Matches english
            'price': 45.00,  # Matches 10-500 range
            'thumbnail': 'https://images.unsplash.com/photo-1497215728101-856f4ea42174?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,  # Matches premium=false
            'search_count': 750,
            'view_count': 550
        },
        # ALPHABETICAL SORT TEST COURSES
        {
            'title': 'Angular Fundamentals',
            'subtitle': 'Angular framework basics',
            'description': 'Introduction to Angular framework development.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'English',
            'price': 44.99,
            'thumbnail': 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': False,
            'search_count': 300,
            'view_count': 250
        },
        {
            'title': 'DevOps Masterclass',
            'subtitle': 'Complete DevOps training',
            'description': 'Comprehensive DevOps engineering course.',
            'category': 'DevOps',
            'level': 'Advanced',
            'language': 'English',
            'price': 119.99,
            'thumbnail': 'https://images.unsplash.com/photo-1579468118864-1b9ea3c0db4a?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 400,
            'view_count': 350
        },
        {
            'title': 'Spring Boot Microservices',
            'subtitle': 'Microservices architecture',
            'description': 'Building microservices with Spring Boot.',
            'category': 'Programming',
            'level': 'Advanced',
            'language': 'English',
            'price': 99.99,
            'thumbnail': 'https://images.unsplash.com/photo-1627398242454-45a1465c2479?w=400',
            'instructor_id': instructor_ids[0],
            'status': 'PUBLISHED',
            'premium': True,
            'search_count': 500,
            'view_count': 450
        },
        # NON-PUBLISHED COURSE - to verify exclusion
        {
            'title': 'TEST - Draft Course',
            'subtitle': 'Should not appear in list',
            'description': 'Draft course that should be excluded from Course List API.',
            'category': 'Programming',
            'level': 'Beginner',
            'language': 'English',
            'price': 29.99,
            'thumbnail': 'https://images.unsplash.com/photo-1497215728101-856f4ea42174?w=400',
            'instructor_id': instructor_ids[1],
            'status': 'DRAFT',  # Not PUBLISHED
            'premium': False,
            'search_count': 100,
            'view_count': 50
        }
    ]
    
    return courses

def insert_courses(conn, courses):
    """Insert test courses into database"""
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
        return []

def create_test_enrollments(conn, course_ids):
    """Create test enrollments for MOST_ENROLLED testing"""
    
    # We need student IDs from the user database
    try:
        user_conn = psycopg2.connect(
            host='localhost',
            port=15432,
            user='cyberlearnix',
            password='cyberlearnix123',
            database='lms_user_db'
        )
        user_conn.autocommit = True
        
        cursor = user_conn.cursor()
        cursor.execute("SELECT id FROM users WHERE role = 'STUDENT' LIMIT 20")
        student_ids = [row[0] for row in cursor.fetchall()]
        cursor.close()
        user_conn.close()
        
        if not student_ids:
            print("No students found for enrollment testing")
            return []
            
    except Exception as e:
        print(f"Error getting student IDs: {e}")
        return []
    
    # Create enrollments with specific counts for testing
    # Course A (index 0): 50 enrollments
    # Course B (index 1): 20 enrollments  
    # Course C (index 2): 5 enrollments
    # Course D (index 3): 0 enrollments
    
    enrollment_mapping = [
        (course_ids[0], 50),  # High enrollment
        (course_ids[1], 20),  # Medium enrollment
        (course_ids[2], 5),   # Low enrollment
        (course_ids[3], 0),   # No enrollment
    ]
    
    enrollments = []
    current_time = datetime.now()
    
    for course_id, count in enrollment_mapping:
        if count == 0:
            continue
            
        # Use available student IDs
        available_students = student_ids[:count] if len(student_ids) >= count else student_ids
        
        for i, student_id in enumerate(available_students):
            enrollment = {
                'student_id': student_id,
                'course_id': course_id,
                'student_name': f'Test Student {i+1}',
                'enrolled_at': current_time,
                'status': 'ACTIVE',
                'progress': 0.0
            }
            enrollments.append(enrollment)
    
    # Insert enrollments
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
        
        print(f"Inserted {len(enrollments)} enrollments for MOST_ENROLLED testing")
        cursor.close()
        return enrollment_mapping
    except Exception as e:
        print(f"Error inserting enrollments: {e}")
        return []

def verify_test_data(conn):
    """Verify the created test data"""
    try:
        cursor = conn.cursor()
        
        # Count total courses
        cursor.execute("SELECT COUNT(*) FROM courses WHERE title LIKE '%TEST%' OR title LIKE '%Java%' OR title LIKE '%Python%'")
        test_course_count = cursor.fetchone()[0]
        
        # Get sample courses
        cursor.execute("""
            SELECT id, title, category, level, language, price, premium, status, search_count, view_count 
            FROM courses 
            WHERE title LIKE '%TEST%' OR title LIKE '%Java%' OR title LIKE '%Python%'
            ORDER BY id
        """)
        
        courses = cursor.fetchall()
        print(f"\n=== VERIFICATION RESULTS ===")
        print(f"Total test courses created: {test_course_count}")
        print(f"\nSample courses:")
        for course in courses[:10]:  # Show first 10
            print(f"ID: {course[0]}, Title: {course[1]}, Category: {course[2]}, Level: {course[3]}, Price: {course[5]}, Premium: {course[6]}, Status: {course[7]}")
        
        # Count enrollments
        cursor.execute("SELECT course_id, COUNT(*) FROM enrollments GROUP BY course_id")
        enrollment_counts = cursor.fetchall()
        print(f"\nEnrollment counts:")
        for course_id, count in enrollment_counts:
            print(f"Course ID {course_id}: {count} enrollments")
        
        cursor.close()
        return True
    except Exception as e:
        print(f"Error verifying test data: {e}")
        return False

def main():
    """Main function to create comprehensive test data"""
    print("Starting Course List API test data generation...")
    
    conn = connect_to_database()
    if not conn:
        print("Failed to connect to database. Exiting.")
        return
    
    try:
        # Get instructor IDs
        instructor_ids = get_instructor_ids(conn)
        
        # Clear existing test courses
        clear_existing_test_courses(conn)
        
        # Create test courses
        print("\nCreating comprehensive test courses...")
        courses = create_test_courses(conn, instructor_ids)
        course_ids = insert_courses(conn, courses)
        
        if not course_ids:
            print("Failed to insert courses")
            return
        
        # Create test enrollments
        print("\nCreating test enrollments for MOST_ENROLLED testing...")
        enrollment_mapping = create_test_enrollments(conn, course_ids)
        
        # Verify test data
        print("\nVerifying test data...")
        verify_test_data(conn)
        
        print("\n" + "="*50)
        print("Course List API test data generation completed!")
        print("="*50)
        print(f"Summary:")
        print(f"- Test courses created: {len(courses)}")
        print(f"- Test enrollments created: {sum([count for _, count in enrollment_mapping])}")
        print(f"- Courses with different search/view counts for POPULAR testing: 3")
        print(f"- Courses with specific enrollment counts for MOST_ENROLLED testing: 4")
        print(f"- Courses matching complex filter combination: 5")
        print(f"- Courses designed to fail filters: 1")
        print(f"- Non-PUBLISHED course for exclusion testing: 1")
        
        print("\nTest data is ready for Course List API testing!")
        
    except Exception as e:
        print(f"Error during test data generation: {e}")
    finally:
        conn.close()
        print("\nDatabase connection closed.")

if __name__ == "__main__":
    main()