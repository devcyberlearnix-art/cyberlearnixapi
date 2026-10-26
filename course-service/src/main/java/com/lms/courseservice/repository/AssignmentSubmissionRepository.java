package com.lms.courseservice.repository;

import com.lms.courseservice.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, UUID studentId);

    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    List<AssignmentSubmission> findByStudentId(UUID studentId);

    List<AssignmentSubmission> findByAssignmentIdAndStatus(Long assignmentId, AssignmentSubmission.SubmissionStatus status);

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, UUID studentId);

    @Query("SELECT COUNT(asub) FROM AssignmentSubmission asub WHERE asub.studentId = :studentId")
    long countByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(asub) FROM AssignmentSubmission asub WHERE asub.studentId = :studentId AND asub.status = 'SUBMITTED'")
    long countSubmittedByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(asub) FROM AssignmentSubmission asub WHERE asub.studentId = :studentId AND asub.status = 'GRADED'")
    long countGradedByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(asub) FROM AssignmentSubmission asub WHERE asub.studentId = :studentId AND asub.status = 'DRAFT'")
    long countPendingByStudentId(@Param("studentId") UUID studentId);
}