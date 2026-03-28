package com.cyberlearnix.commonlibs.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sub_sub_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"subCategory"})
public class SubSubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "slug", unique = true, nullable = false, length = 100)
    private String slug;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SubCategory subCategory;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Transient
    private Long productCount;

    public Long getSubCategoryId() {
        return subCategory != null ? subCategory.getId() : null;
    }
}
