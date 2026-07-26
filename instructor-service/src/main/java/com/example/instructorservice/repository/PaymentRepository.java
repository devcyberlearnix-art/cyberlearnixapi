package com.example.instructorservice.repository;

import com.example.instructorservice.dto.MonthlyEarningDTO;
import com.example.instructorservice.entity.Course;
import com.example.instructorservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.course = :course")
    double totalRevenueByCourse(Course course);
    @Query(value = """
SELECT 
    CONCAT(EXTRACT(YEAR FROM p.paid_at), '-', EXTRACT(MONTH FROM p.paid_at)) AS month,
    SUM(p.amount) AS revenue
FROM payment p
JOIN course c ON p.course_id = c.id
WHERE c.instructor_id = :instructorId
GROUP BY EXTRACT(YEAR FROM p.paid_at), EXTRACT(MONTH FROM p.paid_at)
ORDER BY EXTRACT(YEAR FROM p.paid_at), EXTRACT(MONTH FROM p.paid_at)
""", nativeQuery = true)

    List<MonthlyEarningDTO> getMonthlyEarningsByInstructor(UUID instructorId);
}