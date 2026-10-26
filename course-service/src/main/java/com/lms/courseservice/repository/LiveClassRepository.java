package com.lms.courseservice.repository;

import com.lms.courseservice.entity.LiveClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveClassRepository extends JpaRepository<LiveClass, Long> {

    List<LiveClass> findByCourseId(Long courseId);

    List<LiveClass> findBySectionId(Long sectionId);

    @Query("SELECT lc FROM LiveClass lc WHERE lc.courseId = :courseId AND lc.scheduledStartTime > :now ORDER BY lc.scheduledStartTime ASC")
    List<LiveClass> findUpcomingByCourseId(@Param("courseId") Long courseId, @Param("now") LocalDateTime now);

    @Query("SELECT lc FROM LiveClass lc WHERE lc.sectionId = :sectionId AND lc.scheduledStartTime > :now ORDER BY lc.scheduledStartTime ASC")
    List<LiveClass> findUpcomingBySectionId(@Param("sectionId") Long sectionId, @Param("now") LocalDateTime now);

    List<LiveClass> findByInstructorId(UUID instructorId);

    @Query("SELECT lc FROM LiveClass lc WHERE lc.instructorId = :instructorId AND lc.scheduledStartTime > :now ORDER BY lc.scheduledStartTime ASC")
    List<LiveClass> findUpcomingByInstructorId(@Param("instructorId") UUID instructorId, @Param("now") LocalDateTime now);

    Optional<LiveClass> findByIdAndCourseId(Long id, Long courseId);

    Optional<LiveClass> findByIdAndInstructorId(Long id, UUID instructorId);

    boolean existsByCourseIdAndInstructorId(Long courseId, UUID instructorId);

    @Query("SELECT COUNT(lc) FROM LiveClass lc WHERE lc.courseId = :courseId AND lc.scheduledStartTime BETWEEN :startTime AND :endTime")
    long countConflictingClasses(@Param("courseId") Long courseId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(lc) FROM LiveClass lc WHERE lc.courseId IN :courseIds")
    long countByCourseIds(@Param("courseIds") List<Long> courseIds);
}
