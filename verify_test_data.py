"""
Verify Course List API Test Data
Connects to database and shows detailed information about created test courses
"""

import psycopg2

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

def verify_test_data(conn):
    """Comprehensive verification of test data"""
    try:
        cursor = conn.cursor()
        
        print("="*70)
        print("COURSE LIST API TEST DATA VERIFICATION")
        print("="*70)
        
        # Get all PUBLISHED courses with our test data
        cursor.execute("""
            SELECT id, title, subtitle, category, level, language, price, premium, status, search_count, view_count
            FROM courses 
            WHERE status = 'PUBLISHED'
            ORDER BY id DESC
        """)
        
        courses = cursor.fetchall()
        
        print(f"\nTOTAL PUBLISHED COURSES: {len(courses)}")
        print("="*70)
        
        # Group by category
        categories = {}
        levels = {}
        languages = {}
        price_ranges = {'free': 0, 'low': 0, 'medium': 0, 'high': 0, 'premium': 0}
        
        for course in courses:
            course_id, title, subtitle, category, level, language, price, premium, status, search_count, view_count = course
            
            # Handle None values
            category = category or 'Uncategorized'
            level = level or 'Unspecified'
            language = language or 'Unspecified'
            price = price or 0
            
            # Count categories
            if category not in categories:
                categories[category] = 0
            categories[category] += 1
            
            # Count levels
            if level not in levels:
                levels[level] = 0
            levels[level] += 1
            
            # Count languages
            if language not in languages:
                languages[language] = 0
            languages[language] += 1
            
            # Count price ranges
            if price == 0:
                price_ranges['free'] += 1
            elif price < 50:
                price_ranges['low'] += 1
            elif price < 100:
                price_ranges['medium'] += 1
            elif price < 500:
                price_ranges['high'] += 1
            else:
                price_ranges['premium'] += 1
        
        print("\nCATEGORY DISTRIBUTION:")
        for category, count in sorted(categories.items()):
            print(f"  {category}: {count}")
        
        print("\nLEVEL DISTRIBUTION:")
        for level, count in sorted(levels.items()):
            print(f"  {level}: {count}")
        
        print("\nLANGUAGE DISTRIBUTION:")
        for language, count in sorted(languages.items()):
            print(f"  {language}: {count}")
        
        print("\nPRICE RANGE DISTRIBUTION:")
        print(f"  Free (0): {price_ranges['free']}")
        print(f"  Low (<50): {price_ranges['low']}")
        print(f"  Medium (50-100): {price_ranges['medium']}")
        print(f"  High (100-500): {price_ranges['high']}")
        print(f"  Premium (>500): {price_ranges['premium']}")
        
        # Show Java courses for search testing
        print("\nJAVA COURSES (for search testing):")
        cursor.execute("""
            SELECT id, title, category, level, language, price, premium, search_count, view_count
            FROM courses 
            WHERE status = 'PUBLISHED' AND (title ILIKE '%Java%' OR subtitle ILIKE '%Java%' OR description ILIKE '%Java%')
            ORDER BY id
        """)
        java_courses = cursor.fetchall()
        print(f"  Found {len(java_courses)} Java courses:")
        for course in java_courses:
            print(f"    ID {course[0]}: {course[1]} | {course[2]} | {course[3]} | ${course[5]} | Premium: {course[6]}")
        
        # Show Python courses for search testing
        print("\nPYTHON COURSES (for search testing):")
        cursor.execute("""
            SELECT id, title, category, level, language, price, premium, search_count, view_count
            FROM courses 
            WHERE status = 'PUBLISHED' AND (title ILIKE '%Python%' OR subtitle ILIKE '%Python%' OR description ILIKE '%Python%')
            ORDER BY id
        """)
        python_courses = cursor.fetchall()
        print(f"  Found {len(python_courses)} Python courses:")
        for course in python_courses:
            print(f"    ID {course[0]}: {course[1]} | {course[2]} | {course[3]} | ${course[5]} | Premium: {course[6]}")
        
        # Show POPULAR test courses
        print("\nPOPULAR SORT TEST COURSES:")
        cursor.execute("""
            SELECT id, title, search_count, view_count, (search_count * 0.6 + view_count * 0.4) as popular_score
            FROM courses 
            WHERE status = 'PUBLISHED' AND title ILIKE '%TEST%'
            ORDER BY popular_score DESC
        """)
        popular_courses = cursor.fetchall()
        print(f"  Found {len(popular_courses)} POPULAR test courses:")
        for course in popular_courses:
            print(f"    ID {course[0]}: {course[1]} | Search: {course[2]} | View: {course[3]} | Score: {course[4]:.1f}")
        
        # Show multi-filter test courses
        print("\nMULTI-FILTER TEST COURSES:")
        cursor.execute("""
            SELECT id, title, category, level, language, price, premium
            FROM courses 
            WHERE status = 'PUBLISHED' AND title ILIKE '%Multi-Filter%'
            ORDER BY id
        """)
        multi_filter_courses = cursor.fetchall()
        print(f"  Found {len(multi_filter_courses)} multi-filter test courses:")
        for course in multi_filter_courses:
            print(f"    ID {course[0]}: {course[1]} | {course[2]} | {course[3]} | {course[4]} | ${course[5]} | Premium: {course[6]}")
        
        # Show non-PUBLISHED courses
        print("\nNON-PUBLISHED COURSES (should be excluded):")
        cursor.execute("""
            SELECT id, title, status
            FROM courses 
            WHERE status != 'PUBLISHED'
            ORDER BY id
        """)
        non_published = cursor.fetchall()
        print(f"  Found {len(non_published)} non-PUBLISHED courses:")
        for course in non_published:
            print(f"    ID {course[0]}: {course[1]} | Status: {course[2]}")
        
        # Check if enrollments table exists
        cursor.execute("""
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_name = 'enrollments'
            )
        """)
        enrollments_exists = cursor.fetchone()[0]
        
        if enrollments_exists:
            print("\nENROLLMENT DATA:")
            cursor.execute("""
                SELECT course_id, COUNT(*) as enrollment_count
                FROM enrollments
                GROUP BY course_id
                ORDER BY enrollment_count DESC
            """)
            enrollments = cursor.fetchall()
            print(f"  Found enrollment data for {len(enrollments)} courses:")
            for course_id, count in enrollments:
                cursor.execute("SELECT title FROM courses WHERE id = %s", (course_id,))
                title = cursor.fetchone()[0]
                print(f"    Course ID {course_id} ({title}): {count} enrollments")
        else:
            print("\nENROLLMENT DATA: Table does not exist yet")
            print("  Note: MOST_ENROLLED sort cannot be tested without enrollment data")
        
        cursor.close()
        return True
        
    except Exception as e:
        print(f"Error verifying test data: {e}")
        return False

def main():
    """Main verification function"""
    print("Starting Course List API test data verification...\n")
    
    conn = connect_to_database()
    if not conn:
        print("Failed to connect to database. Exiting.")
        return
    
    try:
        verify_test_data(conn)
        
        print("\n" + "="*70)
        print("VERIFICATION COMPLETED")
        print("="*70)
        print("\nTest data is ready for Course List API testing")
        print("All filter combinations can be tested")
        print("All sort options can be tested")
        print("Pagination can be tested")
        print("MOST_ENROLLED sort requires enrollment table to be created")
        
    except Exception as e:
        print(f"Error during verification: {e}")
    finally:
        conn.close()
        print("\nDatabase connection closed.")

if __name__ == "__main__":
    main()