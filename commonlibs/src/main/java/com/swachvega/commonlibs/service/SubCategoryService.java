package com.cyberlearnix.commonlibs.service;

import com.cyberlearnix.commonlibs.entity.SubCategory;
import com.cyberlearnix.commonlibs.dto.SubCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SubCategoryService {

    // Create subcategory
    SubCategory createSubCategory(SubCategoryDTO subCategoryDTO, String createdBy);
    
    // Create subcategory by category name
    SubCategory createSubCategoryByCategoryName(SubCategoryDTO subCategoryDTO, String categoryName, String createdBy);
    
    // Update subcategory
    SubCategory updateSubCategory(Long id, SubCategoryDTO subCategoryDTO, String updatedBy);
    
    // Get subcategory by ID
    Optional<SubCategory> getSubCategoryById(Long id);
    
    // Get subcategory by name
    Optional<SubCategory> getSubCategoryByName(String name);
    
    // Get subcategory by slug
    Optional<SubCategory> getSubCategoryBySlug(String slug);
    
    // Get all active subcategories
    List<SubCategory> getAllActiveSubCategories();
    
    // Get subcategories by category ID
    List<SubCategory> getSubCategoriesByCategoryId(Long categoryId);
    
    // Get subcategories by multiple category IDs
    Map<Long, List<SubCategory>> getSubCategoriesByCategoryIds(List<Long> categoryIds);
    
    // Get active subcategories by category ID
    List<SubCategory> getActiveSubCategoriesByCategoryId(Long categoryId);
    
    // Get subcategories by category name
    List<SubCategory> getSubCategoriesByCategoryName(String categoryName);
    
    // Get featured subcategories
    List<SubCategory> getFeaturedSubCategories();
    
    // Search subcategories
    List<SubCategory> searchSubCategories(String searchTerm);
    
    // Get subcategories with pagination
    Page<SubCategory> getSubCategoriesWithPagination(Pageable pageable);
    
    // Get subcategories by category with pagination
    Page<SubCategory> getSubCategoriesByCategoryWithPagination(Long categoryId, Pageable pageable);
    
    // Delete subcategory (soft delete)
    void deleteSubCategory(Long id, String deletedBy);
    
    // Activate/Deactivate subcategory
    void toggleSubCategoryStatus(Long id, boolean isActive, String updatedBy);
    
    // Set featured status
    void setFeaturedStatus(Long id, boolean isFeatured, String updatedBy);
    
    // Update sort order
    void updateSortOrder(Long id, Integer sortOrder, String updatedBy);
    
    // Get product count for subcategory
    Long getProductCountBySubCategoryId(Long subCategoryId);
    
    // Check if subcategory exists
    boolean existsByNameAndCategoryId(String name, Long categoryId);
    
    // Check if slug exists
    boolean existsBySlug(String slug);
    
    // Generate unique slug
    String generateUniqueSlug(String name);
}
