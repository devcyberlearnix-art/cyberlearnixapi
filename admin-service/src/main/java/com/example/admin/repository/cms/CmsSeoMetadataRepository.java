package com.example.admin.repository.cms;

import com.example.admin.entity.cms.CmsSeoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CmsSeoMetadataRepository extends JpaRepository<CmsSeoMetadata, UUID> {
    Optional<CmsSeoMetadata> findByPageKey(String pageKey);
}

