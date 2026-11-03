package com.example.admin.repository.cms;

import com.example.admin.entity.cms.CmsSiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CmsSiteSettingRepository extends JpaRepository<CmsSiteSetting, UUID> {
    Optional<CmsSiteSetting> findBySettingKey(String settingKey);
    List<CmsSiteSetting> findBySettingGroup(String settingGroup);
}

