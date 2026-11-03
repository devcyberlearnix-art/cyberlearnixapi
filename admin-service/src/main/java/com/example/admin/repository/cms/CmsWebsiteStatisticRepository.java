package com.example.admin.repository.cms;

import com.example.admin.entity.cms.CmsWebsiteStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CmsWebsiteStatisticRepository extends JpaRepository<CmsWebsiteStatistic, UUID> {
    List<CmsWebsiteStatistic> findAllByOrderByDisplayOrderAsc();
    List<CmsWebsiteStatistic> findByIsVisibleTrueOrderByDisplayOrderAsc();
}

