package com.example.admin.repository.cms;

import com.example.admin.entity.cms.CmsSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CmsSectionRepository extends JpaRepository<CmsSection, UUID> {
    List<CmsSection> findByPageKeyOrderByDisplayOrderAsc(String pageKey);
    List<CmsSection> findByPageKeyAndIsVisibleTrueOrderByDisplayOrderAsc(String pageKey);
    Optional<CmsSection> findByPageKeyAndSectionKey(String pageKey, String sectionKey);
}

