package com.example.instructorservice.repository;

import com.example.instructorservice.entity.CourseMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseMessageRepository extends JpaRepository<CourseMessage, UUID> {
}
