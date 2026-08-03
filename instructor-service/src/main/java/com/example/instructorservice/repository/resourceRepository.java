package com.example.instructorservice.repository;


import com.example.instructorservice.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface resourceRepository extends JpaRepository<Resource, UUID> {
}
