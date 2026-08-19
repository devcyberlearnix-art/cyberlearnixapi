"""
Test Course List API with Created Test Data
Tests various filter and sort combinations to verify the test data is sufficient
"""

import requests
import json

BASE_URL = "http://localhost:8083/api/v1/courses/list"

def test_api(endpoint, description, expect_success=True):
    """Test a single API endpoint and print results"""
    try:
        response = requests.get(endpoint)
        print(f"\n{description}")
        print(f"URL: {endpoint}")
        print(f"Status: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            if data.get('success'):
                courses = data.get('data', {}).get('courses', [])
                pagination = data.get('data', {}).get('pagination', {})
                print(f"Results: {len(courses)} courses")
                print(f"Pagination: page={pagination.get('page')}, size={pagination.get('size')}, total={pagination.get('totalElements')}")
                if courses:
                    print(f"First course: {courses[0].get('title')}")
            else:
                print(f"Error: {data.get('message')}")
        else:
            print(f"Response: {response.text}")
        
        # If we expect failure (validation tests), 400 is success
        if not expect_success:
            return response.status_code == 400
        return response.status_code == 200
        
    except Exception as e:
        print(f"Error testing endpoint: {e}")
        return False

def main():
    """Main testing function"""
    print("="*70)
    print("COURSE LIST API TESTING WITH CREATED TEST DATA")
    print("="*70)
    
    tests = [
        (f"{BASE_URL}", "1. No filters (default)"),
        (f"{BASE_URL}?search=java", "2. Search for 'java'"),
        (f"{BASE_URL}?category=programming", "3. Category = programming"),
        (f"{BASE_URL}?category=programming,design", "4. Multi-category (programming,design)"),
        (f"{BASE_URL}?level=beginner", "5. Level = beginner"),
        (f"{BASE_URL}?level=beginner,intermediate", "6. Multi-level (beginner,intermediate)"),
        (f"{BASE_URL}?language=english", "7. Language = english"),
        (f"{BASE_URL}?language=english,hindi", "8. Multi-language (english,hindi)"),
        (f"{BASE_URL}?minPrice=50", "9. MinPrice = 50"),
        (f"{BASE_URL}?maxPrice=500", "10. MaxPrice = 500"),
        (f"{BASE_URL}?minPrice=50&maxPrice=500", "11. Price range (50-500)"),
        (f"{BASE_URL}?premium=true", "12. Premium = true"),
        (f"{BASE_URL}?free=true", "13. Free = true"),
        (f"{BASE_URL}?paid=true", "14. Paid = true"),
        (f"{BASE_URL}?sort=POPULAR", "15. Sort = POPULAR"),
        (f"{BASE_URL}?sort=MOST_VIEWED", "16. Sort = MOST_VIEWED"),
        (f"{BASE_URL}?sort=MOST_SEARCHED", "17. Sort = MOST_SEARCHED"),
        (f"{BASE_URL}?sort=MOST_ENROLLED", "18. Sort = MOST_ENROLLED"),
        (f"{BASE_URL}?sort=PRICE_LOW_HIGH", "19. Sort = PRICE_LOW_HIGH"),
        (f"{BASE_URL}?sort=PRICE_HIGH_LOW", "20. Sort = PRICE_HIGH_LOW"),
        (f"{BASE_URL}?sort=TITLE_AZ", "21. Sort = TITLE_AZ"),
        (f"{BASE_URL}?sort=TITLE_ZA", "22. Sort = TITLE_ZA"),
        (f"{BASE_URL}?page=0&size=5", "23. Pagination (page=0,size=5)"),
        (f"{BASE_URL}?page=1&size=5", "24. Pagination (page=1,size=5)"),
        (f"{BASE_URL}?search=java&category=programming&level=beginner&sort=MOST_VIEWED&page=0&size=5", "25. Search + filter + sort + pagination"),
        (f"{BASE_URL}?search=python&category=programming,design&level=beginner,intermediate&language=english&sort=MOST_ENROLLED&page=0&size=3", "26. Multi-filter + MOST_ENROLLED + pagination"),
        (f"{BASE_URL}?page=-1", "27. Invalid page (should fail)", False),
        (f"{BASE_URL}?size=0", "28. Invalid size (should fail)", False),
        (f"{BASE_URL}?sort=INVALID_SORT", "29. Invalid sort (should fail)", False),
        (f"{BASE_URL}?free=true&paid=true", "30. Free + paid conflict (should fail)", False),
    ]
    
    passed = 0
    failed = 0
    
    for test in tests:
        if len(test) == 2:
            endpoint, description = test
            expect_success = True
        else:
            endpoint, description, expect_success = test
            
        if test_api(endpoint, description, expect_success):
            passed += 1
        else:
            failed += 1
    
    print("\n" + "="*70)
    print("TEST SUMMARY")
    print("="*70)
    print(f"Total tests: {len(tests)}")
    print(f"Passed: {passed}")
    print(f"Failed: {failed}")
    print(f"Success rate: {passed/len(tests)*100:.1f}%")
    
    # Test the complex filter combination specifically
    print("\n" + "="*70)
    print("COMPLEX FILTER COMBINATION TEST")
    print("="*70)
    # Use a simpler complex filter that we know has matches
    complex_url = f"{BASE_URL}?search=java&category=programming&level=beginner&language=english&minPrice=10&maxPrice=100&premium=false&sort=POPULAR&page=0&size=10"
    test_api(complex_url, "Complex: search=java + category=programming + level=beginner + language=english + price=10-100 + premium=false + sort=POPULAR + pagination", True)

if __name__ == "__main__":
    main()