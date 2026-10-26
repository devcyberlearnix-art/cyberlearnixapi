package com.lms.courseservice.repository;

import com.lms.courseservice.entity.LiveClassAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveClassAttendanceRepository extends JpaRepository<LiveClassAttendance, Long> {

    Optional<LiveClassAttendance> findByLiveClassIdAndStudentId(Long liveClassId, UUID studentId);

    List<LiveClassAttendance> findByLiveClassId(Long liveClassId);

    List<LiveClassAttendance> findByStudentId(UUID studentId);

    boolean existsByLiveClassIdAndStudentId(Long liveClassId, UUID studentId);

    @Query("SELECT COUNT(lca) FROM LiveClassAttendance lca WHERE lca.liveClassId = :liveClassId")
    long countByLiveClassId(@Param("liveClassId") Long liveClassId);

    @Query("SELECT COUNT(lca) FROM LiveClassAttendance lca WHERE lca.liveClassId = :liveClassId AND lca.status = 'JOINED'")
    long countCurrentlyJoined(@Param("liveClassId") Long liveClassId);

    @Query("SELECT COUNT(lca) FROM LiveClassAttendance lca WHERE lca.liveClassId = :liveClassId AND lca.status = 'ATTENDED'")
    long countAttended(@Param("liveClassId") Long liveClassId);

    @Query("SELECT SUM(lca.durationSeconds) FROM LiveClassAttendance lca WHERE lca.liveClassId = :liveClassId AND lca.status = 'ATTENDED'")
    Long sumDurationSeconds(@Param("liveClassId") Long liveClassId);

    @Query("SELECT COUNT(lca) FROM LiveClassAttendance lca WHERE lca.liveClassId = :liveClassId AND lca.status = 'ABSENT'")
    long countAbsent(@Param("liveClassId") Long liveClassId);

    @Query("SELECT COUNT(lca) FROM LiveClassAttendance lca WHERE lca.studentId = :studentId")
    long countByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(lca) FROM LiveClassAttendance lca WHERE lca.studentId = :studentId AND lca.status = 'ATTENDED'")
    long countAttendedByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT SUM(lca.durationSeconds) FROM LiveClassAttendance lca WHERE lca.studentId = :studentId AND lca.status = 'ATTENDED'")
    Long sumDurationSecondsByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(DISTINCT lca.liveClassId) FROM LiveClassAttendance lca WHERE lca.studentId = :studentId")
    long countDistinctLiveClassesByStudentId(@Param("studentId") UUID studentId);
}
