# Course Service API Documentation

## Base URL
```
http://localhost:8081/api/v1
```

## Authentication
- Most endpoints require JWT authentication via `Authorization: Bearer <token>` header
- Public endpoints are marked as "No Authentication Required"
- Role-based access control is enforced for instructor/admin operations

---

## Course Management APIs

### 1. Create Course
**Endpoint:** `POST /courses`
**Authentication:** Required (Instructor/Admin only)
**Description:** Create a new course

#### Request Body
```json
{
  "title": "Complete Java Development",
  "subtitle": "From beginner to advanced",
  "description": "Learn Java programming from scratch with hands-on projects",
  "category": "Programming",
  "level": "Beginner",
  "language": "English",
  "price": 49.99,
  "thumbnail": "https://example.com/thumbnail.jpg",
  "instructorId": "uuid-of-instructor",
  "status": "DRAFT",
  "premium": true
}
```

#### Request Validation
- `title`: Required, 3-100 characters
- `subtitle`: Optional, max 200 characters
- `description`: Required, 10-2000 characters
- `category`: Required, max 50 characters
- `level`: Optional, max 50 characters
- `language`: Optional, max 50 characters
- `price`: Required, minimum 0.0
- `thumbnail`: Optional, must be valid URL
- `instructorId`: Required, valid UUID
- `status`: Optional, must be DRAFT, PUBLISHED, or ARCHIVED
- `premium`: Optional, boolean

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Course created successfully",
  "data": {
    "id": 1,
    "title": "Complete Java Development",
    "subtitle": "From beginner to advanced",
    "description": "Learn Java programming from scratch with hands-on projects",
    "category": "Programming",
    "level": "Beginner",
    "language": "English",
    "price": 49.99,
    "thumbnail": "https://example.com/thumbnail.jpg",
    "instructorId": "uuid-of-instructor",
    "status": "DRAFT",
    "premium": true,
    "searchCount": 0,
    "viewCount": 0
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### Error Responses
- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Invalid or missing token
- `403 Forbidden`: Insufficient permissions

---

### 2. Get All Courses
**Endpoint:** `GET /courses`
**Authentication:** Not Required (Public)
**Description:** Retrieve all courses in the system

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "title": "Complete Java Development",
    "subtitle": "From beginner to advanced",
    "description": "Learn Java programming from scratch",
    "category": "Programming",
    "level": "Beginner",
    "language": "English",
    "price": 49.99,
    "thumbnail": "https://example.com/thumbnail.jpg",
    "instructorId": "uuid-of-instructor",
    "status": "PUBLISHED",
    "premium": true,
    "searchCount": 150,
    "viewCount": 320
  }
]
```

---

### 3. Get Course List with Filters
**Endpoint:** `GET /courses/list`
**Authentication:** Not Required (Public)
**Description:** Get paginated list of published courses with advanced filtering

#### Query Parameters
- `search` (optional): Search in title, description, category
- `category` (optional): Filter by category
- `level` (optional): Filter by level (Beginner, Intermediate, Advanced)
- `language` (optional): Filter by language
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter
- `premium` (optional): Filter premium courses (true/false)
- `free` (optional): Filter free courses (true/false)
- `paid` (optional): Filter paid courses (true/false)
- `sort` (optional): Sort option (price_asc, price_desc, rating, newest, popular)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)

#### Example Request
```
GET /courses/list?category=Programming&level=Beginner&minPrice=0&maxPrice=100&page=0&size=10&sort=price_asc
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Courses retrieved successfully",
  "data": {
    "courses": [
      {
        "id": 1,
        "title": "Complete Java Development",
        "subtitle": "From beginner to advanced",
        "description": "Learn Java programming from scratch",
        "category": "Programming",
        "level": "Beginner",
        "language": "English",
        "price": 29.99,
        "thumbnail": "https://example.com/thumbnail.jpg",
        "instructorId": "uuid-of-instructor",
        "status": "PUBLISHED",
        "premium": true,
        "searchCount": 150,
        "viewCount": 320
      }
    ],
    "pagination": {
      "currentPage": 0,
      "totalPages": 5,
      "totalElements": 50,
      "pageSize": 10,
      "hasNext": true,
      "hasPrevious": false
    }
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Invalid filter parameters",
  "data": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

### 4. Get Course by ID
**Endpoint:** `GET /courses/{id}`
**Authentication:** Not Required (Public)
**Description:** Get a specific course by its ID

#### Path Parameters
- `id` (required): Course ID

#### Response (200 OK)
```json
{
  "id": 1,
  "title": "Complete Java Development",
  "subtitle": "From beginner to advanced",
  "description": "Learn Java programming from scratch",
  "category": "Programming",
  "level": "Beginner",
  "language": "English",
  "price": 49.99,
  "thumbnail": "https://example.com/thumbnail.jpg",
  "instructorId": "uuid-of-instructor",
  "status": "PUBLISHED",
  "premium": true,
  "searchCount": 150,
  "viewCount": 320
}
```

#### Error Responses
- `404 Not Found`: Course not found

---

### 5. Update Course (Full)
**Endpoint:** `PUT /courses/{id}`
**Authentication:** Required (Instructor/Admin only)
**Description:** Update entire course information

#### Path Parameters
- `id` (required): Course ID

#### Request Body
```json
{
  "title": "Updated Java Development Course",
  "subtitle": "Updated subtitle",
  "description": "Updated description",
  "category": "Programming",
  "level": "Intermediate",
  "language": "English",
  "price": 59.99,
  "thumbnail": "https://example.com/new-thumbnail.jpg",
  "instructorId": "uuid-of-instructor",
  "status": "PUBLISHED",
  "premium": true
}
```

#### Response (200 OK)
```json
{
  "id": 1,
  "title": "Updated Java Development Course",
  "subtitle": "Updated subtitle",
  "description": "Updated description",
  "category": "Programming",
  "level": "Intermediate",
  "language": "English",
  "price": 59.99,
  "thumbnail": "https://example.com/new-thumbnail.jpg",
  "instructorId": "uuid-of-instructor",
  "status": "PUBLISHED",
  "premium": true,
  "searchCount": 150,
  "viewCount": 320
}
```

---

### 6. Update Course (Partial)
**Endpoint:** `PATCH /courses/{id}`
**Authentication:** Required (Instructor/Admin only)
**Description:** Update specific fields of a course

#### Path Parameters
- `id` (required): Course ID

#### Request Body
```json
{
  "price": 39.99,
  "status": "PUBLISHED"
}
```

#### Response (200 OK)
```json
{
  "id": 1,
  "title": "Complete Java Development",
  "subtitle": "From beginner to advanced",
  "description": "Learn Java programming from scratch",
  "category": "Programming",
  "level": "Beginner",
  "language": "English",
  "price": 39.99,
  "thumbnail": "https://example.com/thumbnail.jpg",
  "instructorId": "uuid-of-instructor",
  "status": "PUBLISHED",
  "premium": true,
  "searchCount": 150,
  "viewCount": 320
}
```

---

### 7. Update Course Status
**Endpoint:** `PATCH /courses/{id}/status`
**Authentication:** Required (Admin service only)
**Description:** Update course status for approval/rejection

#### Path Parameters
- `id` (required): Course ID

#### Request Body
```json
{
  "status": "PUBLISHED"
}
```

#### Response (200 OK)
```json
{
  "id": 1,
  "title": "Complete Java Development",
  "subtitle": "From beginner to advanced",
  "description": "Learn Java programming from scratch",
  "category": "Programming",
  "level": "Beginner",
  "language": "English",
  "price": 49.99,
  "thumbnail": "https://example.com/thumbnail.jpg",
  "instructorId": "uuid-of-instructor",
  "status": "PUBLISHED",
  "premium": true,
  "searchCount": 150,
  "viewCount": 320
}
```

---

### 8. Delete Course
**Endpoint:** `DELETE /courses/{id}`
**Authentication:** Required (Instructor/Admin only)
**Description:** Delete a course

#### Path Parameters
- `id` (required): Course ID

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Course deleted successfully",
  "data": {
    "id": 1,
    "title": "Complete Java Development",
    "description": "Learn Java programming from scratch",
    "category": "Programming",
    "instructorId": "uuid-of-instructor",
    "deleted": true
  }
}
```

---

## Course Details APIs

### 9. Get Course Details
**Endpoint:** `GET /courses/{courseId}/details`
**Authentication:** Optional (Public, but auth provides enrollment status)
**Description:** Get comprehensive course details including curriculum, instructor, reviews, enrollment status

#### Path Parameters
- `courseId` (required): Course ID

#### Headers
- `Authorization` (optional): Bearer token for enrollment status
- `X-Forwarded-For` (optional): Client IP address for audit logging

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Course details fetched successfully",
  "data": {
    "courseInfo": {
      "id": 1,
      "title": "Complete Java Development",
      "subtitle": "From beginner to advanced",
      "description": "Learn Java programming from scratch",
      "category": "Programming",
      "level": "Beginner",
      "language": "English",
      "price": 49.99,
      "thumbnail": "https://example.com/thumbnail.jpg",
      "instructorId": "uuid-of-instructor",
      "status": "PUBLISHED",
      "premium": true,
      "searchCount": 150,
      "viewCount": 320
    },
    "instructorProfile": {
      "instructorId": "uuid-of-instructor",
      "name": "John Doe",
      "email": "john@example.com",
      "headline": "Senior Java Developer",
      "bio": "10+ years of experience in Java development",
      "expertise": ["Java", "Spring", "Hibernate"],
      "rating": 4.8,
      "totalCourses": 15,
      "totalStudents": 5000,
      "verified": true
    },
    "curriculum": {
      "courseId": 1,
      "courseTitle": "Complete Java Development",
      "totalDuration": 7200,
      "totalSections": 10,
      "totalLectures": 45,
      "sections": [
        {
          "sectionId": 1,
          "title": "Introduction to Java",
          "orderIndex": 1,
          "sectionDuration": 600,
          "lectureCount": 5,
          "lectures": [
            {
              "lectureId": 1,
              "title": "What is Java?",
              "description": "Introduction to Java programming",
              "videoUrl": "https://example.com/video1.mp4",
              "duration": 120,
              "orderIndex": 1,
              "previewEnabled": true,
              "resources": "https://example.com/resources1.pdf"
            }
          ]
        }
      ]
    },
    "enrollmentStatus": {
      "isEnrolled": true,
      "enrollmentDate": "2024-01-10T10:00:00",
      "progress": 35.5,
      "status": "ACTIVE"
    },
    "ratingSummary": {
      "averageRating": 4.5,
      "totalRatings": 250,
      "totalReviews": 150,
      "fiveStarCount": 120,
      "fourStarCount": 80,
      "threeStarCount": 30,
      "twoStarCount": 15,
      "oneStarCount": 5
    },
    "courseStats": {
      "totalEnrollments": 500,
      "totalStudents": 480,
      "averageProgress": 45.0,
      "completionCount": 200
    },
    "requirements": [
      {
        "id": 1,
        "requirementType": "Prerequisite",
        "requirementText": "Basic computer skills"
      }
    ],
    "learningOutcomes": [
      {
        "id": 1,
        "outcomeText": "Master Java fundamentals",
        "skillCategory": "Programming"
      }
    ],
    "materials": [
      {
        "id": 1,
        "materialName": "Source Code",
        "materialType": "ZIP",
        "fileUrl": "https://example.com/source.zip",
        "fileSize": 1024000
      }
    ],
    "faqs": [
      {
        "id": 1,
        "question": "Is this course suitable for beginners?",
        "answer": "Yes, this course is designed for complete beginners.",
        "displayOrder": 1
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### Error Response (404 Not Found)
```json
{
  "success": false,
  "message": "Course not found",
  "data": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## Course Discovery APIs

### 10. Get Featured Courses
**Endpoint:** `GET /courses/featured`
**Authentication:** Not Required (Public)
**Description:** Get featured courses based on featured score

#### Query Parameters
- `limit` (optional): Number of courses to return (default: 6)

#### Example Request
```
GET /courses/featured?limit=10
```

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "title": "Complete Java Development",
    "subtitle": "From beginner to advanced",
    "description": "Learn Java programming from scratch",
    "category": "Programming",
    "level": "Beginner",
    "language": "English",
    "price": 49.99,
    "thumbnail": "https://example.com/thumbnail.jpg",
    "instructorId": "uuid-of-instructor",
    "status": "PUBLISHED",
    "premium": true,
    "students": 500,
    "rating": 4.5,
    "totalReviews": 150,
    "searchCount": 150,
    "viewCount": 320,
    "featuredScore": 85.5,
    "tag": "Bestseller"
  }
]
```

---

### 11. Get Trending Courses
**Endpoint:** `GET /courses/trending`
**Authentication:** Not Required (Public)
**Description:** Get paginated trending courses sorted by trending score

#### Query Parameters
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)
- `category` (optional): Filter by category
- `level` (optional): Filter by level

#### Example Request
```
GET /courses/trending?page=0&size=10&category=Programming&level=Beginner
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Trending courses retrieved successfully",
  "data": {
    "courses": [
      {
        "id": 1,
        "title": "Complete Java Development",
        "subtitle": "From beginner to advanced",
        "description": "Learn Java programming from scratch",
        "category": "Programming",
        "level": "Beginner",
        "language": "English",
        "price": 49.99,
        "thumbnail": "https://example.com/thumbnail.jpg",
        "instructorId": "uuid-of-instructor",
        "status": "PUBLISHED",
        "premium": true,
        "students": 500,
        "rating": 4.5,
        "totalReviews": 150,
        "searchCount": 150,
        "viewCount": 320,
        "trendingScore": 92.5
      }
    ],
    "pagination": {
      "currentPage": 0,
      "totalPages": 3,
      "totalElements": 25,
      "pageSize": 10,
      "hasNext": true,
      "hasPrevious": false
    }
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Invalid filter parameters",
  "data": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

### 12. Track Course Impression
**Endpoint:** `POST /courses/{courseId}/impressions`
**Authentication:** Not Required (Public)
**Description:** Track course impressions for analytics

#### Path Parameters
- `courseId` (required): Course ID

#### Query Parameters
- `source` (optional): Source of impression (default: HOME)

#### Example Request
```
POST /courses/1/impressions?source=SEARCH
```

#### Response (200 OK)
No response body (void)

---

### 13. Get Course Stats
**Endpoint:** `GET /courses/stats`
**Authentication:** Not Required (Public)
**Description:** Get overall course statistics

#### Response (200 OK)
```json
{
  "totalCourses": 150,
  "publishedCourses": 120,
  "draftCourses": 25,
  "archivedCourses": 5,
  "totalEnrollments": 5000,
  "totalStudents": 3500,
  "averageRating": 4.3
}
```

---

## Enrollment APIs

### 14. Enroll in Course (Free)
**Endpoint:** `POST /courses/{courseId}/enroll`
**Authentication:** Required (Student only)
**Description:** Enroll a student in a free course

#### Path Parameters
- `courseId` (required): Course ID

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Student enrolled in the course successfully.",
  "data": {
    "courseId": 1,
    "courseTitle": "Complete Java Development",
    "studentId": "uuid-of-student",
    "category": "Programming",
    "status": "Enrolled",
    "enrolledAt": "2024-01-15T10:30:00"
  }
}
```

#### Error Responses
- `400 Bad Request`: Course not free or already enrolled
- `401 Unauthorized`: Invalid or missing token
- `404 Not Found`: Course not found

---

### 15. Get Enrolled Students
**Endpoint:** `GET /courses/{courseId}/students`
**Authentication:** Not Required (Public)
**Description:** Get list of students enrolled in a course

#### Path Parameters
- `courseId` (required): Course ID

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Enrolled students fetched successfully",
  "data": {
    "courseId": 1,
    "courseTitle": "Complete Java Development",
    "totalStudents": 50,
    "students": [
      {
        "studentId": "uuid-of-student",
        "studentName": "Jane Smith",
        "email": "jane@example.com",
        "enrolledAt": "2024-01-10T10:00:00",
        "progress": 45.5
      }
    ]
  }
}
```

---

### 16. Check Enrollment Status
**Endpoint:** `GET /enrollments/check/{courseId}`
**Authentication:** Required
**Description:** Check if the current user is enrolled in a course

#### Path Parameters
- `courseId` (required): Course ID

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Student is enrolled in this course.",
  "data": {
    "courseId": 1,
    "courseName": "Complete Java Development",
    "studentId": "uuid-of-student",
    "enrolled": true,
    "enrollmentStatus": "Enrolled"
  }
}
```

#### Response (200 OK - Not Enrolled)
```json
{
  "success": true,
  "message": "Student is not enrolled in this course.",
  "data": {
    "courseId": 1,
    "courseName": "Complete Java Development",
    "studentId": "uuid-of-student",
    "enrolled": false,
    "enrollmentStatus": "Not Enrolled"
  }
}
```

---

### 17. Get User Enrollments
**Endpoint:** `GET /enrollments/users/{userId}`
**Authentication:** Required
**Description:** Get all enrollments for a specific user

#### Path Parameters
- `userId` (required): User ID

#### Response (200 OK)
```json
[
  {
    "courseId": 1,
    "courseTitle": "Complete Java Development",
    "studentId": "uuid-of-student",
    "category": "Programming",
    "status": "ACTIVE",
    "enrolledAt": "2024-01-10T10:00:00"
  },
  {
    "courseId": 2,
    "courseTitle": "Python for Beginners",
    "studentId": "uuid-of-student",
    "category": "Programming",
    "status": "ACTIVE",
    "enrolledAt": "2024-01-12T15:30:00"
  }
]
```

---

### 18. Internal Enrollment (After Payment)
**Endpoint:** `POST /enrollments/internal/enroll`
**Authentication:** Required (Internal service)
**Description:** Internal endpoint to enroll student after payment processing

#### Request Body
```json
{
  "courseId": 1,
  "userId": "uuid-of-student"
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Student enrolled in the course successfully after payment.",
  "data": {
    "courseId": 1,
    "courseTitle": "Complete Java Development",
    "studentId": "uuid-of-student",
    "category": "Programming",
    "status": "Enrolled",
    "enrolledAt": "2024-01-15T10:30:00"
  }
}
```

---

## Course Preview APIs

### 19. Create Course Preview
**Endpoint:** `POST /courses/{courseId}/preview`
**Authentication:** Required (Instructor/Admin only)
**Description:** Create a course preview video

#### Path Parameters
- `courseId` (required): Course ID

#### Request Body
```json
{
  "title": "Course Introduction",
  "videoUrl": "https://res.cloudinary.com/...",
  "duration": 180
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Course preview created successfully",
  "data": {
    "id": 1,
    "title": "Course Introduction",
    "videoUrl": "https://res.cloudinary.com/...",
    "duration": 180,
    "courseId": 1,
    "courseTitle": "Complete Java Development"
  }
}
```

---

### 20. Get Course Previews
**Endpoint:** `GET /courses/{courseId}/preview`
**Authentication:** Not Required (Public)
**Description:** Get all preview videos for a course

#### Path Parameters
- `courseId` (required): Course ID

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Found 2 preview(s) for course id: 1",
  "data": [
    {
      "id": 1,
      "title": "Course Introduction",
      "videoUrl": "https://res.cloudinary.com/...",
      "duration": 180,
      "courseId": 1,
      "courseTitle": "Complete Java Development"
    },
    {
      "id": 2,
      "title": "What You'll Learn",
      "videoUrl": "https://res.cloudinary.com/...",
      "duration": 120,
      "courseId": 1,
      "courseTitle": "Complete Java Development"
    }
  ]
}
```

#### Response (200 OK - No Previews)
```json
{
  "success": true,
  "message": "No previews found for course id: 1",
  "data": []
}
```

---

### 21. Update Course Preview
**Endpoint:** `PATCH /courses/{courseId}/preview`
**Authentication:** Required (Instructor/Admin only)
**Description:** Update course preview (partial update)

#### Path Parameters
- `courseId` (required): Course ID

#### Request Body
```json
{
  "title": "Updated Course Introduction",
  "videoUrl": "https://res.cloudinary.com/new-url",
  "duration": 200
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Course preview updated successfully",
  "data": {
    "id": 1,
    "title": "Updated Course Introduction",
    "videoUrl": "https://res.cloudinary.com/new-url",
    "duration": 200,
    "courseId": 1,
    "courseTitle": "Complete Java Development"
  }
}
```

---

## Section Management APIs

### 22. Create Section
**Endpoint:** `POST /courses/{courseId}/sections`
**Authentication:** Required (Instructor/Admin only)
**Description:** Create a new section in a course

#### Path Parameters
- `courseId` (required): Course ID

#### Request Body
```json
{
  "title": "Introduction to Java",
  "orderIndex": 1
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Section created successfully",
  "data": {
    "id": 1,
    "title": "Introduction to Java",
    "orderIndex": 1,
    "courseId": 1,
    "courseTitle": "Complete Java Development"
  }
}
```

---

### 23. Get Course Sections
**Endpoint:** `GET /courses/{courseId}/sections`
**Authentication:** Not Required (Public)
**Description:** Get all sections for a course

#### Path Parameters
- `courseId` (required): Course ID

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "title": "Introduction to Java",
    "orderIndex": 1,
    "courseId": 1,
    "lectures": []
  },
  {
    "id": 2,
    "title": "Java Basics",
    "orderIndex": 2,
    "courseId": 1,
    "lectures": []
  }
]
```

---

### 24. Update Section
**Endpoint:** `PATCH /courses/sections/{sectionId}`
**Authentication:** Required (Instructor/Admin only)
**Description:** Update a section

#### Path Parameters
- `sectionId` (required): Section ID

#### Request Body
```json
{
  "title": "Updated Introduction to Java",
  "orderIndex": 1
}
```

#### Response (200 OK)
```json
{
  "id": 1,
  "title": "Updated Introduction to Java",
  "orderIndex": 1,
  "courseId": 1,
  "lectures": []
}
```

---

### 25. Delete Section
**Endpoint:** `DELETE /courses/sections/{sectionId}`
**Authentication:** Required (Instructor/Admin only)
**Description:** Delete a section

#### Path Parameters
- `sectionId` (required): Section ID

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Section deleted successfully",
  "data": {
    "id": 1,
    "title": "Introduction to Java",
    "courseId": 1,
    "deleted": true
  }
}
```

---

## Common Response Structures

### ApiResponse Structure
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Pagination Structure
```json
{
  "currentPage": 0,
  "totalPages": 5,
  "totalElements": 50,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false
}
```

### Error Response Structure
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## HTTP Status Codes

- `200 OK`: Request successful
- `400 Bad Request`: Invalid request parameters or validation errors
- `401 Unauthorized`: Authentication required or invalid token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

---

## Common Error Messages

- "Course not found"
- "Invalid filter parameters"
- "Course not free or already enrolled"
- "Insufficient permissions"
- "Validation error: [field] is required"

---

## Rate Limiting
- Public endpoints: 100 requests per minute per IP
- Authenticated endpoints: 200 requests per minute per user
- Internal endpoints: No rate limiting

---

## Notes for Frontend Team

1. **Authentication**: Most endpoints require JWT token in `Authorization: Bearer <token>` header
2. **Pagination**: Use the pagination structure to implement infinite scroll or pagination controls
3. **Error Handling**: Always check the `success` field in response and handle error messages appropriately
4. **Date Format**: All timestamps are in ISO 8601 format (UTC)
5. **Price Format**: Prices are returned as decimal numbers, format according to locale
6. **Image URLs**: Thumbnail and video URLs are absolute URLs, use directly in image/video components
7. **Course Status**: Filter by status if needed (DRAFT, PUBLISHED, ARCHIVED)
8. **Enrollment Check**: Use the enrollment check endpoint before showing enrollment/purchase options
9. **Preview Videos**: Use preview endpoints to show course preview videos on course cards
10. **Search and Filter**: The course list endpoint supports comprehensive filtering for advanced search functionality
