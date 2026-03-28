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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "parent", "children", "products" })
public class Category {

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

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "hero_url", length = 500)
    private String heroUrl;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "datakart_cat")
    private Integer datakartCat;

    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "level")
    private Integer level = 0; // 0 = root, 1 = subcategory, 2 = sub-subcategory, etc.

    // Self-referencing relationship for hierarchical categories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Category> children = new ArrayList<>();

    // Many-to-many relationship with products
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
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
    private String fullPath; // e.g., "Electronics > Mobile Phones > Smartphones"

    // Helper methods
    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
        child.setLevel(this.level + 1);
    }

    public void removeChild(Category child) {
        children.remove(child);
        child.setParent(null);
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public String getFullPath() {
        String label = (displayName != null && !displayName.isBlank()) ? displayName : name;
        if (label == null || label.isBlank()) {
            label = null;
        }

        if (parent == null) {
            return label;
        }

        // IMPORTANT: Use public methods on parent so Hibernate can initialize LAZY proxies.
        String parentPath = parent.getFullPath();
        if (parentPath == null || parentPath.isBlank()) {
            return label;
        }

        if (label == null) {
            return parentPath;
        }

        return parentPath + " > " + label;
    }
}
