package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "sub_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"category", "products"})
public class SubCategory {

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
    private String slug; // URL-friendly version of name

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "card_url", length = 500)
    private String cardUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "datakart_sub_category_id", length = 50)
    private String datakartSubCategoryId;

    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "level")
    private Integer level = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_sub_category_id")
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "category", "products", "children"})
    private SubCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "category", "products", "parent", "children"})
    private List<SubCategory> children = new ArrayList<>();

    // Relationship with Category (Many subcategories belong to one category)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "children", "products", "parent"})
    private Category category;

    // One-to-many relationship with products
    @OneToMany(mappedBy = "subCategoryEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "categories", "primaryCategory", "subCategoryEntity"})
    private List<Product> products = new ArrayList<>();

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes", columnDefinition = "jsonb")
    private Map<String, Object> customAttributes;

    // Computed fields
    @Transient
    private Long productCount;

    @Transient
    private String fullPath; // e.g., "Dairy > Butter"

    // Helper methods
    public void addProduct(Product product) {
        products.add(product);
        product.setSubCategoryEntity(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setSubCategoryEntity(null);
    }

    @JsonIgnore
    public String getFullPath() {
        if (fullPath != null) {
            return fullPath;
        }

        if (category != null) {
            fullPath = category.getDisplayName() + " > " + displayName;
        } else {
            fullPath = displayName;
        }
        return fullPath;
    }

    @JsonIgnore
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    @JsonIgnore
    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : null;
    }

    @JsonIgnore
    public String getCategorySlug() {
        return category != null ? category.getSlug() : null;
    }

    @JsonIgnore
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }
}
