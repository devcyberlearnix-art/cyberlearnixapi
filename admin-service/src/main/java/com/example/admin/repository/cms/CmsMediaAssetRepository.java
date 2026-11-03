package com.example.admin.repository.cms;

import com.example.admin.entity.cms.CmsMediaAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CmsMediaAssetRepository extends JpaRepository<CmsMediaAsset, UUID> {
    Page<CmsMediaAsset> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

