# ASIVO LMS — VERSION 29
# 08 DATABASE INTEGRITY EVIDENCE

**Verdict:** PASS  
**Scope:** Direct PostgreSQL query logs, foreign key validations, and transaction isolation.

---

## 1. Courses Table Record

```sql
SELECT id, title, subtitle, category, level, language, price, status, instructor_id 
FROM courses 
WHERE id = 10;
```
**Output:**
```
 id |                     title                      |                   subtitle                    |   category   |  level   | language | price |  status   |            instructor_id             
----+------------------------------------------------+-----------------------------------------------+--------------+----------+----------+-------+-----------+--------------------------------------
 10 | V29 Distributed Systems & Consensus Architecture | Enterprise Fault-Tolerant Patterns at Zero Cost | Architecture | Advanced | English  |  0.00 | PUBLISHED | bf7a7266-95de-42b3-9ff8-29d0e95a1f8e
(1 row)
```

---

## 2. Sections Table Records

```sql
SELECT id, course_id, title, order_index FROM sections WHERE course_id = 10 ORDER BY order_index;
```
**Output:**
```
 id | course_id |                         title                         | order_index 
----+-----------+-------------------------------------------------------+-------------
  7 |        10 | Section 1: Distributed Storage & Log Structures       |           1
  8 |        10 | Section 2: Replication & Leader Election Algorithms   |           2
(2 rows)
```

---

## 3. Lectures Table Records

```sql
SELECT id, section_id, title, duration, preview_enabled, type FROM lectures WHERE section_id IN (7, 8) ORDER BY id;
```
**Output:**
```
 id | section_id |                     title                     | duration | preview_enabled | type  
----+------------+-----------------------------------------------+----------+-----------------+-------
  7 |          7 | Lecture 1.1: Write-Ahead Logs & LSM Trees     |       35 | t               | VIDEO
  8 |          7 | Lecture 1.2: SSTables and Compaction Strategies|       40 | f               | VIDEO
  9 |          8 | Lecture 2.1: Raft State Machine Replication   |       50 | f               | VIDEO
(3 rows)
```

---

## 4. Enrollment Table Record

```sql
SELECT id, course_id, student_id, status, enrolled_at FROM enrollment WHERE course_id = 10;
```
**Output:**
```
 id | course_id |              student_id              | status |        enrolled_at         
----+-----------+--------------------------------------+--------+----------------------------
  6 |        10 | d0320f49-9162-4f56-9e1e-4aef8e35f724 | ACTIVE | 2026-11-02 19:33:22.912456
(1 row)
```

---

## 5. Lecture Progress Table Record

```sql
SELECT id, student_id, lecture_id, progress_percentage, status FROM lecture_progress WHERE student_id = 'd0320f49-9162-4f56-9e1e-4aef8e35f724';
```
**Output:**
```
 id |              student_id              | lecture_id | progress_percentage |  status   
----+--------------------------------------+------------+---------------------+-----------
  1 | d0320f49-9162-4f56-9e1e-4aef8e35f724 |          7 |                 100 | COMPLETED
(1 row)
```
