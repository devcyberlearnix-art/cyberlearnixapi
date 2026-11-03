package com.example.admin.repository.cms;

import com.example.admin.entity.cms.CmsNavigationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CmsNavigationItemRepository extends JpaRepository<CmsNavigationItem, UUID> {
    List<CmsNavigationItem> findAllByOrderByDisplayOrderAsc();
    List<CmsNavigationItem> findByIsVisibleTrueOrderByDisplayOrderAsc();
}

