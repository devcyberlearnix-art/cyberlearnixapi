package com.example.notification.repository;

import com.example.notification.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    java.util.List<Announcement> findByCourseIdAndActiveTrueOrderByCreatedAtDesc(Long courseId);
}