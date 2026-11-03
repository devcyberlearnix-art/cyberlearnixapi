package com.example.admin.repository.cms;

import com.example.admin.entity.cms.CmsTestimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CmsTestimonialRepository extends JpaRepository<CmsTestimonial, UUID> {
    List<CmsTestimonial> findAllByOrderByDisplayOrderAsc();
    List<CmsTestimonial> findByIsVisibleTrueOrderByDisplayOrderAsc();
}

