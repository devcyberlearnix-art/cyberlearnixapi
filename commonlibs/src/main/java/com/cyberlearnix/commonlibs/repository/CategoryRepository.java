package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * Base interface for Category repository operations
 * Implementations should be provided in individual services
 */
public interface CategoryRepository {

    /**
     * Find category by ID
     */
    Optional<Category> findById(Long id);

    /**
     * Find categories by list of IDs
     */
    List<Category> findAllById(List<Long> ids);

    /**
     * Find category by name (case insensitive)
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Find all active categories
     */
    List<Category> findByIsActiveTrueOrderBySortOrderAscNameAsc();

    /**
     * Save category
     */
    Category save(Category category);
}
