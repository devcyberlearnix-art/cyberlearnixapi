package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Course;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class CourseSpecifications {

    public static Specification<Course> withStatus(String status) {
        return (root, query, cb) -> 
            status != null ? cb.equal(cb.lower(root.get("status")), status.toLowerCase()) : null;
    }

    public static Specification<Course> withSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String searchTerm = "%" + search + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), searchTerm),
                cb.like(cb.lower(root.get("subtitle")), searchTerm),
                cb.like(cb.lower(root.get("description")), searchTerm)
            );
        };
    }

    public static Specification<Course> withCategories(List<String> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) {
                return null;
            }
            return cb.lower(root.get("category")).in(categories);
        };
    }

    public static Specification<Course> withLevels(List<String> levels) {
        return (root, query, cb) -> {
            if (levels == null || levels.isEmpty()) {
                return null;
            }
            return cb.lower(root.get("level")).in(levels);
        };
    }

    public static Specification<Course> withLanguages(List<String> languages) {
        return (root, query, cb) -> {
            if (languages == null || languages.isEmpty()) {
                return null;
            }
            return cb.lower(root.get("language")).in(languages);
        };
    }

    public static Specification<Course> withMinPrice(BigDecimal minPrice) {
        return (root, query, cb) -> 
            minPrice != null ? cb.greaterThanOrEqualTo(root.get("price"), minPrice) : null;
    }

    public static Specification<Course> withMaxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> 
            maxPrice != null ? cb.lessThanOrEqualTo(root.get("price"), maxPrice) : null;
    }

    public static Specification<Course> withPremium(Boolean premium) {
        return (root, query, cb) -> 
            premium != null ? cb.equal(root.get("premium"), premium) : null;
    }

    public static Specification<Course> withFree(Boolean isFree) {
        return (root, query, cb) -> {
            if (isFree == null || !isFree) {
                return null;
            }
            return cb.or(
                cb.equal(root.get("price"), BigDecimal.ZERO),
                cb.isNull(root.get("price"))
            );
        };
    }

    public static Specification<Course> withPaid(Boolean isPaid) {
        return (root, query, cb) -> {
            if (isPaid == null || !isPaid) {
                return null;
            }
            return cb.and(
                cb.isNotNull(root.get("price")),
                cb.greaterThan(root.get("price"), BigDecimal.ZERO)
            );
        };
    }
}