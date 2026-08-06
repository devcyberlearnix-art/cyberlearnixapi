package com.example.notification.repository;

import com.example.notification.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, String> {
    Optional<Template> findByName(String name);
    boolean existsByName(String name);
    Page<Template> findByChannel(String channel, Pageable pageable);
    Page<Template> findByActive(Boolean active, Pageable pageable);
    Page<Template> findByChannelAndActive(String channel, Boolean active, Pageable pageable);
}