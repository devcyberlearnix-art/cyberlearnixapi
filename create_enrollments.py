"""
Create Enrollment Table and Test Data for MOST_ENROLLED Testing
This script creates the enrollments table and inserts test data for enrollment-based sorting
"""

import psycopg2
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

def get_student_ids():
    """Get student IDs from user database"""
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
        cursor.execute("SELECT id FROM users WHERE role = 'STUDENT' LIMIT 50")
        student_ids = [row[0] for row in cursor.fetchall()]
        cursor.close()
        user_conn.close()
        
        if not student_ids:
            print("No students found, will create dummy student IDs")
            # Create some dummy UUIDs for testing
            import uuid
            return [str(uuid.uuid4()) for _ in range(20)]
        
        print(f"Found {len(student_ids)} student IDs")
        return student_ids
    except Exception as e:
        print(f"Error getting student IDs: {e}")
        import uuid
        return [str(uuid.uuid4()) for _ in range(20)]

def create_enrollments_table(conn):
    """Create the enrollments table if it doesn't exist"""
    try:
        cursor = conn.cursor()
        
        # Check if table exists
        cursor.execute("""
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_name = 'enrollments'
            )
        """)
        table_exists = cursor.fetchone()[0]
        
        if table_exists:
            print("Enrollments table already exists")
            cursor.close()
            return True
        
        # Create the table
        create_table_query = """
            CREATE TABLE enrollments (
                id BIGSERIAL PRIMARY KEY,
                student_id UUID NOT NULL,
                course_id BIGINT NOT NULL,
                student_name VARCHAR(255),
                enrolled_at TIMESTAMP,
                status VARCHAR(50),
                progress DOUBLE PRECISION,
                FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
            )
        """
        
        cursor.execute(create_table_query)
        print("Created enrollments table successfully")
        cursor.close()
        return True
        
    except Exception as e:
        print(f"Error creating enrollments table: {e}")
        return False

def create_test_enrollments(conn, student_ids):
    """Create test enrollments for MOST_ENROLLED testing"""
    try:
        cursor = conn.cursor()
        
        # Get our test course IDs
        cursor.execute("""
            SELECT id, title FROM courses 
            WHERE title LIKE '%TEST%' AND status = 'PUBLISHED'
            ORDER BY id
            LIMIT 10
        """)
        test_courses = cursor.fetchall()
        
        if not test_courses:
            print("No test courses found for enrollment testing")
            return []
        
        print(f"Found {len(test_courses)} test courses for enrollment testing")
        
        # Create enrollment mapping with specific counts
        # We want different enrollment counts to test MOST_ENROLLED sorting
        enrollment_plans = [
            (test_courses[0][0], 50),  # Course 1: 50 enrollments
            (test_courses[1][0], 20),  # Course 2: 20 enrollments
            (test_courses[2][0], 5),   # Course 3: 5 enrollments
            (test_courses[3][0], 0),   # Course 4: 0 enrollments (for testing)
        ]
        
        enrollments = []
        current_time = datetime.now()
        
        for course_id, target_count in enrollment_plans:
            if target_count == 0:
                print(f"Course ID {course_id}: 0 enrollments (skipped)")
                continue
            
            # Use available student IDs
            available_students = student_ids[:target_count] if len(student_ids) >= target_count else student_ids
            
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
            
            print(f"Course ID {course_id}: {target_count} enrollments planned")
        
        # Insert enrollments
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
        
        print(f"Inserted {len(enrollments)} enrollments successfully")
        cursor.close()
        return enrollment_plans
        
    except Exception as e:
        print(f"Error creating test enrollments: {e}")
        return []

def verify_enrollments(conn):
    """Verify the created enrollments"""
    try:
        cursor = conn.cursor()
        
        # Count total enrollments
        cursor.execute("SELECT COUNT(*) FROM enrollments")
        total_enrollments = cursor.fetchone()[0]
        
        # Get enrollment counts by course
        cursor.execute("""
            SELECT c.id, c.title, COUNT(e.id) as enrollment_count
            FROM courses c
            LEFT JOIN enrollments e ON c.id = e.course_id
            WHERE c.title LIKE '%TEST%' AND c.status = 'PUBLISHED'
            GROUP BY c.id, c.title
            ORDER BY enrollment_count DESC
        """)
        
        enrollment_data = cursor.fetchall()
        
        print("\n" + "="*70)
        print("ENROLLMENT VERIFICATION")
        print("="*70)
        print(f"Total enrollments created: {total_enrollments}")
        print("\nEnrollment counts by test course:")
        for course_id, title, count in enrollment_data:
            print(f"  Course ID {course_id} ({title}): {count} enrollments")
        
        cursor.close()
        return True
        
    except Exception as e:
        print(f"Error verifying enrollments: {e}")
        return False

def main():
    """Main function to create enrollment table and test data"""
    print("Starting enrollment table and test data creation...")
    
    conn = connect_to_database()
    if not conn:
        print("Failed to connect to database. Exiting.")
        return
    
    try:
        # Create enrollments table
        print("\nCreating enrollments table...")
        if not create_enrollments_table(conn):
            print("Failed to create enrollments table")
            return
        
        # Get student IDs
        print("\nGetting student IDs...")
        student_ids = get_student_ids()
        
        # Create test enrollments
        print("\nCreating test enrollments for MOST_ENROLLED testing...")
        enrollment_plans = create_test_enrollments(conn, student_ids)
        
        # Verify enrollments
        print("\nVerifying enrollments...")
        verify_enrollments(conn)
        
        print("\n" + "="*70)
        print("ENROLLMENT DATA CREATION COMPLETED")
        print("="*70)
        print("MOST_ENROLLED sort can now be tested with the Course List API")
        
    except Exception as e:
        print(f"Error during enrollment data creation: {e}")
    finally:
        conn.close()
        print("\nDatabase connection closed.")

if __name__ == "__main__":
    main()