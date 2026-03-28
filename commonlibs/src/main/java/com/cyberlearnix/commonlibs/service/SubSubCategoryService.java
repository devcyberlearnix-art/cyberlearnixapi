package com.cyberlearnix.commonlibs.service;

import com.cyberlearnix.commonlibs.dto.SubSubCategoryDTO;
import com.cyberlearnix.commonlibs.entity.SubSubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SubSubCategoryService {

    SubSubCategory createSubSubCategory(SubSubCategoryDTO dto, String createdBy);

    SubSubCategory updateSubSubCategory(Long id, SubSubCategoryDTO dto, String updatedBy);

    Optional<SubSubCategory> getSubSubCategoryById(Long id);

    Optional<SubSubCategory> getSubSubCategoryByName(String name);

    Optional<SubSubCategory> getSubSubCategoryBySlug(String slug);

    List<SubSubCategory> getActiveBySubCategoryId(Long subCategoryId);

    Page<SubSubCategory> getBySubCategoryWithPagination(Long subCategoryId, Pageable pageable);

    void deleteSubSubCategory(Long id, String deletedBy);

    boolean existsByNameAndSubCategoryId(String name, Long subCategoryId);

    boolean existsBySlug(String slug);

    String generateUniqueSlug(String name);
}
