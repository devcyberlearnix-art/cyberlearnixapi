# ASIVO LMS — Deliverable 05: Concurrent Enrollment Stress & Database Race Conditions

**Release Target:** Version 30 Independent Release Verification  
**Audit Date:** 2026-11-02  
**Auditor:** Independent Principal Security & QA Verification Agent  
**Status:** PASS — Race Conditions Resolved & DB Uniqueness Enforced  

---

## 1. Executive Summary

During the initial independent verification of Version 29, a critical concurrency vulnerability was uncovered: submitting 10 simultaneous enrollment requests for the same student and course caused multiple parallel transactions to succeed (`{ '200': 2, '409': 8 }`), inserting duplicate enrollment rows into the PostgreSQL database (`enrollment` IDs 8 and 9). The Version 29 release report claimed concurrency was verified, which proved factually incorrect upon empirical testing.

In Version 30, this architectural defect has been definitively eliminated by applying a PostgreSQL table-level unique constraint (`uk_enrollment_student_course`), updating the JPA entity definition, and catching `DataIntegrityViolationException` to guarantee clean HTTP 409 Conflict responses with transaction rollback.

---

## 2. Root Cause Analysis

In Version 29, `CourseService.java` attempted to prevent duplicate enrollments purely through an application-level check:
```java
if (enrollmentRepository.existsByStudentIdAndCourseId(userId, courseId)) {
    throw new EnrollmentException("Student is already enrolled.");
}
```
Under concurrent load, two or more threads queried `existsByStudentIdAndCourseId` simultaneously before either had committed its row. Because the underlying database table lacked a unique constraint on `(student_id, course_id)`, both threads proceeded to execute `enrollmentRepository.save(enrollment)`, resulting in multiple rows for the same student-course pair.

---

## 3. Version 30 Remediation

### A. Database Uniqueness Constraint (PostgreSQL)
A table-level unique constraint was executed in `cyberlearnix-postgres` (`lms_course_db`):
```sql
ALTER TABLE enrollment ADD CONSTRAINT uk_enrollment_student_course UNIQUE (student_id, course_id);
```

### B. JPA Entity Constraint Mapping (`Enrollment.java`)
```java
@Entity
@Table(name = "enrollment", uniqueConstraints = {
    @UniqueConstraint(name = "uk_enrollment_student_course", columnNames = {"studentId", "courseId"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID studentId;
    private Long courseId;
    private String studentName;
    private LocalDateTime enrolledAt;
    private String status;
    private Double progress;
    private LocalDateTime completedAt;
    private Long lastLectureId;
}
```

### C. Exception Translation & Rollback (`CourseService.java`)
```java
private void createEnrollment(Long courseId, UUID userId) {
    if (enrollmentRepository.existsByStudentIdAndCourseId(userId, courseId)) {
        throw new EnrollmentException("Student is already enrolled.");
    }

    Enrollment enrollment = new Enrollment();
    enrollment.setCourseId(courseId);
    enrollment.setStudentId(userId);
    enrollment.setEnrolledAt(java.time.LocalDateTime.now());
    enrollment.setStatus("ACTIVE");

    try {
        enrollmentRepository.save(enrollment);
    } catch (DataIntegrityViolationException ex) {
        throw new EnrollmentException("Student is already enrolled.");
    }
}
```

---

## 4. Empirical Concurrent Load Test

A test script (`scratch/test-concurrency-v30.mjs`) was executed against the newly deployed Course Service. A fresh course (Course 14) was created and published, and 10 simultaneous enrollment requests were dispatched concurrently using `Promise.all` for Student B (`4d149252-53fb-4e95-8960-7a8fd6882650`).

### Test Execution Output
```
Admin logged in successfully
Create course status: 200
Created Course ID: 14
Admin status publish response code: 200
Student B logged in. ID: 4d149252-53fb-4e95-8960-7a8fd6882650
Firing 10 simultaneous enrollment requests...
Request #1: HTTP 200
Request #2: HTTP 409
Request #3: HTTP 409
Request #4: HTTP 409
Request #5: HTTP 409
Request #6: HTTP 409
Request #7: HTTP 409
Request #8: HTTP 409
Request #9: HTTP 409
Request #10: HTTP 409
--- CONCURRENCY RESULTS BREAKDOWN ---
{ '200': 1, '409': 9 }
```

### PostgreSQL Direct Database Verification
```sql
SELECT id, student_id, course_id, status, enrolled_at 
FROM enrollment 
WHERE course_id = 14;
```

**Query Output:**
```
 id |              student_id              | course_id | status |        enrolled_at         
----+--------------------------------------+-----------+--------+----------------------------
 10 | 4d149252-53fb-4e95-8960-7a8fd6882650 |        14 | ACTIVE | 2026-11-02 20:35:06.132613
(1 row)
```

---

## 5. Audit Conclusion

**VERDICT: PASS**  
Race conditions are completely neutralized. Under 10-way parallel execution, exactly one transaction commits successfully (HTTP 200), and all nine conflicting transactions are rejected with HTTP 409 Conflict. Database inspection proves exactly one record exists with zero corruption.
