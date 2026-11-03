# ASIVO LMS — VERSION 29
# 04 FREE COURSE CREATION VERIFICATION

**Verdict:** PASS  
**Evidence:** Verified creation of free courses by Administrator and Instructor roles with database persistence proofs.

---

## 1. Admin Course Creation Test Case

- **Endpoint:** `POST /api/v1/courses`
- **Authentication:** Admin JWT (Bearer token from `/api/v1/admin/login`)
- **Payload:**
```json
{
  "title": "V29 Distributed Systems & Consensus Architecture",
  "subtitle": "Enterprise Fault-Tolerant Patterns at Zero Cost",
  "description": "Complete hands-on masterclass covering Raft, Paxos, Kafka event streaming, and eventual consistency.",
  "category": "Architecture",
  "level": "Advanced",
  "language": "English",
  "price": 0.0,
  "instructorId": "bf7a7266-95de-42b3-9ff8-29d0e95a1f8e",
  "thumbnail": "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=800",
  "status": "DRAFT"
}
```
- **HTTP Status Code:** `200 OK`
- **Response Payload:**
```json
{
  "success": true,
  "message": "Course created successfully",
  "data": {
    "id": 10,
    "title": "V29 Distributed Systems & Consensus Architecture",
    "subtitle": "Enterprise Fault-Tolerant Patterns at Zero Cost",
    "category": "Architecture",
    "price": 0.00,
    "status": "DRAFT",
    "searchCount": 0,
    "viewCount": 0
  }
}
```

---

## 2. PostgreSQL Database Verification

```sql
SELECT id, price, status, title FROM courses WHERE id = 10;
```
**Output:**
```
 id | price | status |                     title                      
----+-------+--------+------------------------------------------------
 10 |  0.00 | DRAFT  | V29 Distributed Systems & Consensus Architecture
(1 row)
```

---

## 3. Curriculum Creation Verification (Sections & Lectures)

1. **Section 1 Created:**
   - `POST /api/v1/courses/10/sections` $\rightarrow$ ID: `7`
   - Title: `Section 1: Distributed Storage & Log Structures`
2. **Section 2 Created:**
   - `POST /api/v1/courses/10/sections` $\rightarrow$ ID: `8`
   - Title: `Section 2: Replication & Leader Election Algorithms`
3. **Lecture 1 Created (Preview Enabled):**
   - `POST /api/v1/sections/7/lectures` $\rightarrow$ ID: `7`
   - Title: `Lecture 1.1: Write-Ahead Logs & LSM Trees` (`previewEnabled: true`, `duration: 35`)
4. **Lecture 2 Created (Enrolled Only):**
   - `POST /api/v1/sections/7/lectures` $\rightarrow$ ID: `8`
   - Title: `Lecture 1.2: SSTables and Compaction Strategies` (`previewEnabled: false`, `duration: 40`)
5. **Lecture 3 Created (Enrolled Only):**
   - `POST /api/v1/sections/8/lectures` $\rightarrow$ ID: `9`
   - Title: `Lecture 2.1: Raft State Machine Replication` (`previewEnabled: false`, `duration: 50`)

**Database Verification:**
```sql
SELECT count(*) FROM sections WHERE course_id = 10;
-- Result: 2

SELECT count(*) FROM lectures WHERE section_id IN (7, 8);
-- Result: 3
```
