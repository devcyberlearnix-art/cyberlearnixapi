package com.example.admin.repository.cms;

import com.example.admin.entity.cms.CmsFooterLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CmsFooterLinkRepository extends JpaRepository<CmsFooterLink, UUID> {
    List<CmsFooterLink> findAllByOrderByGroupNameAscDisplayOrderAsc();
    List<CmsFooterLink> findByIsVisibleTrueOrderByGroupNameAscDisplayOrderAsc();
}

