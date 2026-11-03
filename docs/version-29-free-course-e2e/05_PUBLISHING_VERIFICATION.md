# ASIVO LMS — VERSION 29
# 05 PUBLISHING VERIFICATION: LIFECYCLE & DISCOVERY

**Verdict:** PASS  
**Evidence:** Transition from DRAFT to PUBLISHED, pre-publishing enrollment denial, public catalogue indexing.

---

## 1. Draft Course Protection (Pre-Publishing Denial)

Before publishing, student `student_v29_1793628195177@cyberlearnix.com` attempted to enroll in Course 10 while status was `DRAFT`.

- **Request:** `POST /api/v1/courses/10/enroll`
- **HTTP Status:** `400 Bad Request`
- **Response Body:**
```json
{
  "timestamp": "2026-11-02T14:03:19.452Z",
  "status": 400,
  "code": "BAD_REQUEST",
  "message": "Only published courses are eligible for enrollment.",
  "path": "/api/v1/courses/10/enroll",
  "traceId": "9b12a0f4-5231-419b-a312-d021c1092a81"
}
```
**Verdict:** PASS. Unapproved/Draft courses cannot be enrolled by learners.

---

## 2. Course Publishing Workflow

- **Endpoint:** `PATCH /api/v1/courses/10/status`
- **Caller:** Admin / Authorized Staff
- **Payload:** `{"status": "PUBLISHED"}`
- **HTTP Status:** `200 OK`
- **Response:** `{"status": "PUBLISHED"}`

**Database Verification:**
```sql
SELECT id, title, status FROM courses WHERE id = 10;
```
```
 id |                     title                      |  status   
----+------------------------------------------------+-----------
 10 | V29 Distributed Systems & Consensus Architecture | PUBLISHED
(1 row)
```

---

## 3. Public Catalogue Discovery

- **Endpoint:** `GET /api/v1/courses/list?free=true`
- **Caller:** Anonymous Public
- **HTTP Status:** `200 OK`
- **Result:** Course 10 present in returned list.
- **Endpoint:** `GET /api/v1/courses/10`
- **Response:**
```json
{
  "success": true,
  "message": "Course fetched successfully",
  "course": {
    "id": 10,
    "title": "V29 Distributed Systems & Consensus Architecture",
    "price": 0.0,
    "status": "PUBLISHED",
    "category": "Architecture",
    "level": "Advanced"
  }
}
```
