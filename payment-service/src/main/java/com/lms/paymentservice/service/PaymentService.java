package com.lms.paymentservice.service;

import com.lms.paymentservice.client.EnrollmentClient;
import com.lms.paymentservice.dto.PaymentRequest;
import com.lms.paymentservice.dto.PaymentResponse;
import com.lms.paymentservice.entity.Course;
import com.lms.paymentservice.entity.Payment;
import com.lms.paymentservice.entity.PaymentStatus;
import com.lms.paymentservice.exception.BadRequestException;
import com.lms.paymentservice.dto.InstructorCoursePaymentDto;
import com.lms.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CourseService courseService;
    private final PayUHashService payUHashService;
    private final PayUCommandService payUCommandService;
    private final EnrollmentClient enrollmentClient;
    @Value("${payu.key}")
    private String key;

    @Value("${payu.base-url}")
    private String payuBaseUrl;

    @Value("${payment.callback-base-url:http://localhost:8085}")
    private String callbackBaseUrl;

    public PaymentService(PaymentRepository paymentRepository,
                          CourseService courseService,
                          PayUHashService payUHashService,
                          PayUCommandService payUCommandService,
                          EnrollmentClient enrollmentClient) {

        this.paymentRepository = paymentRepository;
        this.courseService = courseService;
        this.payUHashService = payUHashService;
        this.payUCommandService = payUCommandService;
        this.enrollmentClient = enrollmentClient;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public PaymentResponse createPayment(PaymentRequest request) {

        if (request.getAmount() != null) {
            throw new BadRequestException("Do not send amount; it is resolved from courseId");
        }
        if (request.getProductInfo() != null && !request.getProductInfo().isBlank()) {
            throw new BadRequestException("Do not send productInfo; it is loaded from the course");
        }
        if (request.getInstructorId() != null && !request.getInstructorId().isBlank()) {
            throw new BadRequestException("Do not send instructorId; it is loaded from the course");
        }
        if (request.getCourseId() == null || request.getCourseId().isBlank()) {
            throw new BadRequestException("courseId is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank() ||
                request.getFirstName() == null || request.getFirstName().isBlank() ||
                request.getPhone() == null || request.getPhone().isBlank()) {

            throw new BadRequestException("firstName, email and phone are required");
        }

        Course course = courseService.findCourseOrThrow(request.getCourseId());
        Double amount = course.getAmount();
        String productInfo = courseService.resolveProductInfo(course);
        String courseId = course.getCourseId();
        String instructorId = course.getInstructorName();

        String txnId = UUID.randomUUID().toString().replaceAll("-", "") + System.currentTimeMillis();
        String currency = (request.getCurrency() == null || request.getCurrency().isBlank())
                ? "INR"
                : request.getCurrency().trim().toUpperCase();

        Payment payment = Payment.builder()
                .txnId(txnId)
                .amount(amount)
                .firstName(request.getFirstName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .productInfo(productInfo)
                .courseId(courseId)
                .instructorId(instructorId)
                .payerUserId(request.getPayerUserId())
                .currency(currency)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
        return buildPaymentResponse(payment);
    }
    public void handleCallback(String status,
                               String txnId,
                               String amount,
                               String productInfo,
                               String firstName,
                               String email,
                               String hash,
                               String payuId) {

        if (txnId == null || txnId.isBlank() ||
                status == null || status.isBlank()) {
            throw new BadRequestException("Invalid callback data");
        }

        Payment payment = paymentRepository.findByTxnId(txnId)
                .orElseThrow(() -> new BadRequestException("Payment not found"));

        String normalizedAmount = normalizeAmount(amount, payment.getAmount());
        String normalizedProductInfo = getFirstNonBlank(productInfo, payment.getProductInfo());
        String normalizedFirstName = getFirstNonBlank(firstName, payment.getFirstName());
        String normalizedEmail = getFirstNonBlank(email, payment.getEmail());

        // 1) Validate with callback payload values
        boolean isValid = payUHashService.validatePaymentHash(
                status,
                txnId,
                normalizedAmount,
                normalizedProductInfo,
                normalizedFirstName,
                normalizedEmail,
                hash
        );
        // 2) Fallback for minor callback formatting differences in manual tests
        if (!isValid) {
            isValid = payUHashService.validatePaymentHash(
                    status,
                    txnId,
                    String.format("%.2f", payment.getAmount()),
                    payment.getProductInfo(),
                    payment.getFirstName(),
                    payment.getEmail(),
                    hash
            );
        }

        if (!isValid) {
            throw new BadRequestException("Invalid PayU hash");
        }

        payment.setPayuPaymentId(payuId);

        if ("success".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            
            // ✅ ENROLL STUDENT IN COURSE (after successful payment)
            try {
                Long courseId = Long.parseLong(payment.getCourseId());
                UUID studentId = UUID.fromString(payment.getPayerUserId());
                enrollmentClient.enrollStudentAfterPayment(courseId, studentId);
            } catch (IllegalArgumentException e) {
                System.err.println("⚠️ Invalid courseId or studentId format for enrollment: " + e.getMessage());
                // Continue with payment processing even if enrollment fails - can be retried
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
    }
    public Payment verifyPayment(String txnId) {

        if (txnId == null || txnId.isBlank()) {
            throw new BadRequestException("txnId is required");
        }

        Payment payment = paymentRepository.findByTxnId(txnId)
                .orElseThrow(() -> new BadRequestException("Payment not found"));

        // Call PayU verify API
        Map<String, Object> response = payUCommandService.verifyPayment(txnId);

        Object status = response.get("status");

        if ("1".equals(String.valueOf(status))) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else if ("0".equals(String.valueOf(status))) {
            payment.setStatus(PaymentStatus.FAILED);
        }

        return paymentRepository.save(payment);
    }
    public Payment getPaymentByTxnId(String txnId) {
        return paymentRepository.findByTxnId(txnId)
                .orElseThrow(() -> new BadRequestException("Payment not found"));
    }

    /**
     * Payments for a course owned by the instructor (learners who completed payment or were refunded).
     */
    public List<InstructorCoursePaymentDto> listPaymentsForInstructorCourse(String instructorId, String courseId) {
        if (instructorId == null || instructorId.isBlank() || courseId == null || courseId.isBlank()) {
            throw new BadRequestException("instructorId and courseId are required");
        }
        return paymentRepository
                .findByInstructorIdAndCourseIdOrderByCreatedAtDesc(instructorId.trim(), courseId.trim())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS || p.getStatus() == PaymentStatus.REFUNDED)
                .map(this::toInstructorDto)
                .collect(Collectors.toList());
    }

    private InstructorCoursePaymentDto toInstructorDto(Payment p) {
        String refundStatus = p.getRefundLifecycleStatus() == null
                ? null
                : p.getRefundLifecycleStatus().name();
        return InstructorCoursePaymentDto.builder()
                .paymentRecordId(p.getId())
                .payuPaymentId(p.getPayuPaymentId())
                .txnId(p.getTxnId())
                .payerUserId(p.getPayerUserId())
                .payerName(p.getFirstName())
                .payerEmail(p.getEmail())
                .payerPhone(p.getPhone())
                .amount(p.getAmount())
                .currency(p.getCurrency() != null ? p.getCurrency() : "INR")
                .paymentStatus(p.getStatus().name())
                .refundReason(p.getRefundReason())
                .refundStatus(refundStatus)
                .refundAmount(p.getRefundAmount())
                .refundRequestId(p.getRefundRequestId())
                .paidAt(p.getCreatedAt())
                .build();
    }

    public PaymentResponse getPaymentFormData(String txnId) {
        if (txnId == null || txnId.isBlank()) {
            throw new BadRequestException("txnId is required");
        }
        Payment payment = paymentRepository.findByTxnId(txnId)
                .orElseThrow(() -> new BadRequestException("Payment not found"));
        return buildPaymentResponse(payment);
    }

    private PaymentResponse buildPaymentResponse(Payment payment) {
        String formattedAmount = String.format("%.2f", payment.getAmount());
        String hash = payUHashService.generateHash(
                payment.getTxnId(),
                formattedAmount,
                payment.getProductInfo(),
                payment.getFirstName(),
                payment.getEmail(),
                "", "", "", "", ""
        );

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .txnId(payment.getTxnId())
                .key(key)
                .amount(formattedAmount)
                .firstName(payment.getFirstName())
                .email(payment.getEmail())
                .phone(payment.getPhone())
                .productInfo(payment.getProductInfo())
                .courseId(payment.getCourseId())
                .instructorId(payment.getInstructorId())
                .payerUserId(payment.getPayerUserId())
                .currency(payment.getCurrency())
                .hash(hash)
                .surl(callbackBaseUrl + "/payments/callback/success")
                .furl(callbackBaseUrl + "/payments/callback/failure")
                .actionUrl(callbackBaseUrl + "/payments/checkout/" + payment.getTxnId())
                .payuActionUrl(payuBaseUrl + "/_payment")
                .build();
    }

    private String getFirstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }

    private String normalizeAmount(String callbackAmount, Double storedAmount) {
        if (callbackAmount != null && !callbackAmount.isBlank()) {
            try {
                return String.format("%.2f", Double.parseDouble(callbackAmount.trim()));
            } catch (NumberFormatException ignored) {
                // Fall back to persisted amount when callback amount is malformed.
            }
        }
        if (storedAmount == null) {
            return "";
        }
        return String.format("%.2f", storedAmount);
    }
}