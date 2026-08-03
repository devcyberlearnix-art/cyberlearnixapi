package com.example.instructorservice.repository;

import com.example.instructorservice.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    // Optional: Get all modules for a specific course
    List<Module> findByCourseId(Long courseId);

    // Optional: Find module by courseId and module title
    Module findByCourseIdAndTitle(Long courseId, String title);
}
