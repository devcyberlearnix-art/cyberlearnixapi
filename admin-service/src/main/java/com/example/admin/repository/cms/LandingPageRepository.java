package com.example.admin.repository.cms;

import com.example.admin.entity.cms.LandingPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LandingPageRepository extends JpaRepository<LandingPage, UUID> {
    Optional<LandingPage> findByPageKey(String pageKey);
}

