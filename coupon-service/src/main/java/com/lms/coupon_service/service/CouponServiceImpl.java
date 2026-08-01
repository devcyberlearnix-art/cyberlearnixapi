package com.lms.coupon_service.service;

import com.lms.coupon_service.dto.*;
import com.lms.coupon_service.entity.Coupon;
import com.lms.coupon_service.entity.CouponUsage;
import org.springframework.web.client.RestTemplate;
import com.lms.coupon_service.exception.CouponAlreadyExistsException;
import com.lms.coupon_service.exception.CouponValidationException;
import com.lms.coupon_service.repository.CouponRepository;
import com.lms.coupon_service.repository.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository repository;
    private final CouponUsageRepository usageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ValidationResponse validateCoupon(ValidateRequest request, String userId) {
        if (request == null || request.getCouponCode() == null || request.getCouponCode().isBlank()) {
            return buildValidationError("Coupon code is required.");
        }
        if (request.getCourseId() != null) {
            validateCourseExists(request.getCourseId().toString());
        }

        Coupon coupon = findByCode(request.getCouponCode());
        LocalDateTime now = LocalDateTime.now();

        if (!coupon.isActive())
            return buildValidationError("Coupon is inactive");

        if (coupon.getStartTime() != null && coupon.getStartTime().isAfter(now)) {
            return buildValidationError("Coupon not active yet. Starts at: " + coupon.getStartTime());
        }

        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(now)) {
            return buildValidationError("Coupon has expired.");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return buildValidationError("Maximum global usage limit reached.");
        }

        if (coupon.getPerUserLimit() != null) {
            long userUsageCount = usageRepository.countByUserIdAndCouponCode(userId, coupon.getCode());
            if (userUsageCount >= coupon.getPerUserLimit()) {
                return buildValidationError("Personal limit reached.");
            }
        }

        if (coupon.getCourses() != null && !coupon.getCourses().isEmpty()) {
            if (!coupon.getCourses().contains(request.getCourseId())) {
                return buildValidationError("Not valid for this course.");
            }
        }

        double discount = calculateDiscountAmount(coupon, request.getPrice());

        return ValidationResponse.builder()
                .valid(true)
                .discount(discount)
                .finalPrice(request.getPrice() - discount)
                .message("Valid")
                .build();
    }

    @Override
    @Transactional
    public ValidationResponse autoApply(AutoApplyRequest r, String userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> eligible = repository.findAll().stream()
                .filter(c -> c.isActive())
                .filter(c -> (c.getStartTime() == null || c.getStartTime().isBefore(now)))
                .filter(c -> (c.getEndTime() == null || c.getEndTime().isAfter(now)))
                .filter(c -> c.getCourses() == null || c.getCourses().isEmpty()
                        || c.getCourses().contains(r.getCourseId()))
                .filter(c -> c.getAssignedUserId() == null || c.getAssignedUserId().equals(userId))
                .collect(Collectors.toList());

        if (eligible.isEmpty())
            return buildValidationError("No applicable coupons found.");

        Coupon best = eligible.stream()
                .max(Comparator.comparingDouble(c -> calculateDiscountAmount(c, r.getPrice())))
                .orElse(null);

        double discount = calculateDiscountAmount(best, r.getPrice());

        return ValidationResponse.builder()
                .valid(true)
                .discount(discount)
                .finalPrice(r.getPrice() - discount)
                .message("Applied: " + best.getCode())
                .build();
    }

    @Override
    @Transactional
    public BulkResponse bulkGenerate(BulkRequest r) {
        String batchId = "BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if ("INSTRUCTOR".equalsIgnoreCase(r.getCreatorRole())
                && (r.getCourseId() == null || r.getCourseId().isEmpty())) {
            throw new RuntimeException("Instructors must provide a Course ID for bulk generation.");
        }

        for (int i = 0; i < r.getTotalCoupons(); i++) {
            String code = r.getCampaignName().toUpperCase() + "-"
                    + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

            repository.save(Coupon.builder()
                    .code(code)
                    .campaignId(r.getCampaignName())
                    .discountType(r.getDiscountType())
                    .discountValue(r.getDiscountValue())
                    .startTime(r.getStartTime() != null ? r.getStartTime() : LocalDateTime.now())
                    .endTime(r.getExpiryDate())
                    .courses(r.getCourseId() != null ? List.of(Long.parseLong(r.getCourseId())) : null)
                    .isActive(true)
                    .usedCount(0)
                    .build());
        }

        return BulkResponse.builder()
                .batchId(batchId)
                .totalGenerated(r.getTotalCoupons())
                .campaignName(r.getCampaignName())
                .build();
    }

    @Override
    @Transactional
    public Map<String, Object> redeemCoupon(RedeemRequest request, String userId) {
        if (request != null && request.getCourseId() != null) {
            validateCourseExists(request.getCourseId().toString());
        }

        Coupon coupon = findByCode(request.getCouponCode());

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(now)) {
            throw new RuntimeException("Coupon expired.");
        }

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        repository.save(coupon);

        usageRepository.save(CouponUsage.builder()
                .userId(userId)
                .couponCode(coupon.getCode())
                .courseId(request.getCourseId())
                .usedAt(LocalDateTime.now())
                .build());

        return Map.of("success", true, "message", "Redeemed successfully");
    }

    @Override
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public void deleteById(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Coupon not found or already deleted");
        }
        repository.deleteById(id);
    }

    private void validateCourseExists(String courseId) {
        try {
            String url = "http://localhost:8083/api/v1/courses/" + courseId;
            var response = restTemplate.getForEntity(url, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new CouponValidationException("Course not found with id: " + courseId,
                        List.of(new ErrorDetail("courseId", "Course not found with id: " + courseId)));
            }
        } catch (Exception ex) {
            throw new CouponValidationException("Course not found with id: " + courseId,
                    List.of(new ErrorDetail("courseId", "Course not found with id: " + courseId)));
        }
    }

    private double calculateDiscountAmount(Coupon c, double price) {
        if (c == null)
            return 0.0;
        if ("PERCENT".equalsIgnoreCase(c.getDiscountType()) || "PERCENTAGE".equalsIgnoreCase(c.getDiscountType())) {
            return (price * c.getDiscountValue()) / 100.0;
        }
        return c.getDiscountValue();
    }

    private ValidationResponse buildValidationError(String m) {
        return ValidationResponse.builder().valid(false).discount(0.0).finalPrice(0.0).message(m).build();
    }

    @Override
    @Cacheable(value = "coupons", key = "#code")
    public Coupon findByCode(String code) {
        return repository.findByCode(code).orElseThrow(() -> new RuntimeException("Coupon not found."));
    }

    @Override
    public List<CouponDetailsResponse> getAllCoupons() {
        return repository.findAll().stream().map(this::mapToDetailsResponse).collect(Collectors.toList());
    }

    @Override
    public CouponDetailsResponse getCouponDetails(String couponId) {
        return mapToDetailsResponse(findEntityById(couponId));
    }

    @Override
    @Transactional
    public CouponDetailsResponse updateCoupon(String couponId, CouponCreateRequest request, String updatedById,
            String updatedByRole) {
        Coupon coupon = findEntityById(couponId);

        if (request.getCode() != null && !request.getCode().isBlank()) {
            if (!request.getCode().equalsIgnoreCase(coupon.getCode()) && repository.existsByCode(request.getCode())) {
                throw new CouponAlreadyExistsException("A coupon with the specified code already exists.");
            }
            coupon.setCode(request.getCode());
        }

        if (request.getTitle() != null)
            coupon.setTitle(request.getTitle());
        if (request.getDescription() != null)
            coupon.setDescription(request.getDescription());
        if (request.getDiscountType() != null)
            coupon.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue() != null)
            coupon.setDiscountValue(request.getDiscountValue());
        if (request.getMinimumOrderAmount() != null)
            coupon.setMinimumOrderAmount(request.getMinimumOrderAmount());
        if (request.getMaximumDiscountAmount() != null)
            coupon.setMaximumDiscountAmount(request.getMaximumDiscountAmount());
        if (request.getUsageLimit() != null)
            coupon.setUsageLimit(request.getUsageLimit());
        if (request.getUsageLimitPerUser() != null)
            coupon.setPerUserLimit(request.getUsageLimitPerUser());
        if (request.getValidFrom() != null)
            coupon.setStartTime(LocalDateTime.ofInstant(request.getValidFrom(), ZoneOffset.UTC));
        if (request.getValidUntil() != null)
            coupon.setEndTime(LocalDateTime.ofInstant(request.getValidUntil(), ZoneOffset.UTC));
        if (request.getApplicableTo() != null)
            coupon.setApplicableTo(request.getApplicableTo());
        if (request.getApplicableCourseIds() != null)
            coupon.setCourses(request.getApplicableCourseIds().stream()
                .map(Long::valueOf)
                .toList());
        if (request.getStackable() != null)
            coupon.setStackable(request.getStackable());
        if (request.getStatus() != null) {
            coupon.setStatus(request.getStatus());
            coupon.setActive("ACTIVE".equalsIgnoreCase(request.getStatus()));
        }
        if (updatedById != null)
            coupon.setCreatedById(updatedById);
        if (updatedByRole != null)
            coupon.setCreatedByRole(updatedByRole);

        return mapToDetailsResponse(repository.save(coupon));
    }

    @Override
    @Transactional
    public CouponDetailsResponse activateCoupon(String couponId) {
        Coupon coupon = findEntityById(couponId);
        coupon.setActive(true);
        coupon.setStatus("ACTIVE");
        return mapToDetailsResponse(repository.save(coupon));
    }

    @Override
    @Transactional
    public CouponDetailsResponse deactivateCoupon(String couponId) {
        Coupon coupon = findEntityById(couponId);
        coupon.setActive(false);
        coupon.setStatus("INACTIVE");
        return mapToDetailsResponse(repository.save(coupon));
    }

    @Override
    public List<CouponDetailsResponse> getMyCoupons(String userId) {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        return repository.findByAssignedUserId(userId).stream().map(this::mapToDetailsResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getCampaignNames() {
        return repository.findAll().stream()
                .map(Coupon::getCampaignId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Coupon> getCouponsByUserId(String id) {
        return repository.findByAssignedUserId(id);
    }

    @Override
    public List<Coupon> getCouponsByCampaignId(String id) {
        return repository.findByCampaignId(id);
    }

    @Override
    public Map<String, Object> createCoupon(Coupon c) {
        return Map.of("id", repository.save(c).getId());
    }

    @Override
    public CouponDetailsResponse createCoupon(CouponCreateRequest request, String createdById, String createdByRole) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new CouponValidationException("Coupon validation failed.",
                    List.of(new ErrorDetail("code", "Coupon code is required.")));
        }

        if (repository.existsByCode(request.getCode())) {
            throw new CouponAlreadyExistsException("A coupon with the specified code already exists.");
        }

        List<ErrorDetail> errors = new ArrayList<>();
        if (request.getDiscountValue() == null || request.getDiscountValue() <= 0) {
            errors.add(new ErrorDetail("discountValue", "Discount value must be greater than zero."));
        }

        if (request.getValidFrom() == null || request.getValidUntil() == null) {
            errors.add(new ErrorDetail("validFrom", "Both validFrom and validUntil are required."));
        } else if (request.getValidFrom().isAfter(request.getValidUntil())) {
            errors.add(new ErrorDetail("validUntil", "validUntil must be after validFrom."));
        }

        if (request.getDiscountType() == null || request.getDiscountType().isBlank()) {
            errors.add(new ErrorDetail("discountType", "Discount type is required."));
        }

        if (request.getApplicableTo() != null && request.getApplicableTo().equalsIgnoreCase("COURSES") &&
                (request.getApplicableCourseIds() == null || request.getApplicableCourseIds().isEmpty())) {
            errors.add(new ErrorDetail("applicableCourseIds",
                    "At least one applicable course id is required for COURSES coupons."));
        }

        if (!errors.isEmpty()) {
            throw new CouponValidationException("Coupon validation failed.", errors);
        }

        Coupon saved = repository.save(Coupon.builder()
                .code(request.getCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minimumOrderAmount(request.getMinimumOrderAmount())
                .maximumDiscountAmount(request.getMaximumDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .perUserLimit(request.getUsageLimitPerUser())
                .startTime(LocalDateTime.ofInstant(request.getValidFrom(), ZoneOffset.UTC))
                .endTime(LocalDateTime.ofInstant(request.getValidUntil(), ZoneOffset.UTC))
                .courses(request.getApplicableCourseIds() != null ? 
                    request.getApplicableCourseIds().stream()
                        .map(Long::valueOf)
                        .toList() : null)
                .stackable(Boolean.TRUE.equals(request.getStackable()))
                .status(request.getStatus())
                .isActive("ACTIVE".equalsIgnoreCase(request.getStatus()))
                .createdById(createdById)
                .createdByRole(createdByRole)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .usedCount(0)
                .build());

        return mapToDetailsResponse(saved);
    }

    @Override
    @Transactional
    public Map<String, Object> assignCouponToUser(String code, String userId) {
        Coupon coupon = findByCode(code);
        coupon.setAssignedUserId(userId);
        repository.save(coupon);
        return Map.of("success", true, "message", "Assigned to " + userId);
    }

    @Override
    @Transactional
    public Map<String, Object> generateReferralCoupon(String referrerId) {
        String code = "REF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        repository.save(Coupon.builder().code(code).discountType("PERCENT").discountValue(10.0).isActive(true).build());
        return Map.of("success", true, "code", code);
    }

    @Override
    @CacheEvict(value = "coupons", allEntries = true)
    public Map<String, Object> createCouponFromMap(Map<String, Object> data) {
        Coupon saved = repository.save(Coupon.builder()
                .code((String) data.get("code"))
                .discountValue(Double.valueOf(data.get("discountValue").toString()))
                .discountType((String) data.get("discountType"))
                .startTime(data.containsKey("startTime") ? LocalDateTime.parse(data.get("startTime").toString())
                        : LocalDateTime.now())
                .endTime(data.containsKey("endTime") ? LocalDateTime.parse(data.get("endTime").toString()) : null)
                .isActive(true)
                .usedCount(0)
                .build());
        return Map.of("success", true, "id", saved.getId());
    }

    private Coupon findEntityById(String couponId) {
        return repository.findById(couponId).orElseThrow(() -> new RuntimeException("Coupon not found."));
    }

    private CouponDetailsResponse mapToDetailsResponse(Coupon coupon) {
        if (coupon == null) {
            return null;
        }
        return CouponDetailsResponse.builder()
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .title(coupon.getTitle())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minimumOrderAmount(coupon.getMinimumOrderAmount())
                .maximumDiscountAmount(coupon.getMaximumDiscountAmount())
                .usageLimit(coupon.getUsageLimit())
                .usageLimitPerUser(coupon.getPerUserLimit())
                .status(coupon.getStatus())
                .validFrom(coupon.getStartTime() != null ? coupon.getStartTime().atOffset(ZoneOffset.UTC).toInstant()
                        : null)
                .validUntil(
                        coupon.getEndTime() != null ? coupon.getEndTime().atOffset(ZoneOffset.UTC).toInstant() : null)
                .createdBy(CouponDetailsResponse.CreatedBy.builder()
                        .id(coupon.getCreatedById())
                        .role(coupon.getCreatedByRole())
                        .build())
                .createdAt(coupon.getCreatedAt() != null ? coupon.getCreatedAt().atOffset(ZoneOffset.UTC).toInstant()
                        : null)
                .applicableCourseIds(coupon.getCourses() != null ? 
                    coupon.getCourses().stream()
                        .map(String::valueOf)
                        .toList() : null)
                .stackable(Boolean.TRUE.equals(coupon.getStackable()))
                .build();
    }
}