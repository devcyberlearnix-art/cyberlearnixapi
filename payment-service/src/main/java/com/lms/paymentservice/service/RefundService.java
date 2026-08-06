package com.lms.paymentservice.service;

import com.lms.paymentservice.dto.RefundRequest;
import com.lms.paymentservice.dto.RefundResponse;
import com.lms.paymentservice.entity.Payment;
import com.lms.paymentservice.entity.PaymentStatus;
import com.lms.paymentservice.entity.RefundLifecycleStatus;
import com.lms.paymentservice.exception.BadRequestException;
import com.lms.paymentservice.exception.RefundFailedException;
import com.lms.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final PayUCommandService payUCommandService;

    public RefundService(PaymentRepository paymentRepository, PayUCommandService payUCommandService) {
        this.paymentRepository = paymentRepository;
        this.payUCommandService = payUCommandService;
    }

    public RefundResponse initiateRefund(RefundRequest request) {
        if (request.getTxnId() == null || request.getTxnId().isBlank()) {
            throw new BadRequestException("txnId is required");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BadRequestException("amount must be greater than zero");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new BadRequestException("reason is required");
        }

        String currency = (request.getCurrency() == null || request.getCurrency().isBlank())
                ? "INR"
                : request.getCurrency().trim().toUpperCase();
        if (!"INR".equals(currency)) {
            throw new BadRequestException("Only INR is supported");
        }

        Payment payment = resolvePaymentByTxnId(request.getTxnId().trim());

        if (!(payment.getStatus().equals(PaymentStatus.SUCCESS) || payment.getStatus().equals(PaymentStatus.REFUNDED))) {
            throw new RefundFailedException("Can only refund successful payments");
        }
        if (payment.getPayuPaymentId() == null || payment.getPayuPaymentId().isBlank()) {
            throw new RefundFailedException("Missing gateway payment id (mihpayid). Complete successful callback first.");
        }

        double paid = payment.getAmount() == null ? 0.0 : payment.getAmount();
        if (request.getAmount() > paid + 0.009) {
            throw new BadRequestException("Refund amount cannot exceed paid amount");
        }

        String token = (request.getRefundRequestId() == null || request.getRefundRequestId().isBlank())
                ? ("RF" + UUID.randomUUID().toString().replace("-", ""))
                : request.getRefundRequestId().trim();

        Map<String, Object> payuResponse = payUCommandService.initiateRefund(
                payment.getPayuPaymentId(),
                token,
                request.getAmount()
        );

        String gatewayStatus = String.valueOf(payuResponse.getOrDefault("status", ""));
        String gatewayMessage = String.valueOf(payuResponse.getOrDefault("msg", payuResponse.getOrDefault("message", "Unknown gateway response")));
        String gatewayRequestId = String.valueOf(
                payuResponse.getOrDefault(
                        "request_id",
                        payuResponse.getOrDefault("txn_update_id", "")
                )
        );

        if ("1".equals(gatewayStatus) || isManualOrPending(gatewayMessage)) {
            payment.setRefundId(token);
            if (gatewayRequestId != null && !gatewayRequestId.isBlank() && !"null".equalsIgnoreCase(gatewayRequestId)) {
                payment.setRefundRequestId(gatewayRequestId);
            } else {
                payment.setRefundRequestId(token);
            }
            payment.setRefundAmount(request.getAmount());
            payment.setRefundReason(request.getReason().trim());
            payment.setRefundLifecycleStatus(RefundLifecycleStatus.PENDING);
            paymentRepository.save(payment);

            return RefundResponse.builder()
                    .status("PENDING")
                    .message("Refund request accepted by gateway: " + gatewayMessage)
                    .amount(request.getAmount())
                    .currency("INR")
                    .txnId(payment.getTxnId())
                    .refundId(token)
                    .gatewayRequestId(
                            gatewayRequestId == null || gatewayRequestId.isBlank() || "null".equalsIgnoreCase(gatewayRequestId)
                                    ? null
                                    : gatewayRequestId
                    )
                    .reason(payment.getRefundReason())
                    .build();
        }

        throw new RefundFailedException("Refund failed from gateway: " + gatewayMessage);
    }

    public RefundResponse getRefundStatus(String txnId) {
        Payment payment = paymentRepository.findByTxnId(txnId)
                .orElseThrow(() -> new BadRequestException("Payment not found for txnId"));

        if (payment.getRefundId() == null) {
            throw new BadRequestException("No refund associated with this payment");
        }

        String requestId = payment.getRefundRequestId();
        if (requestId == null || requestId.isBlank()) {
            throw new BadRequestException("Refund request id missing. Re-initiate refund or store PayU request_id from gateway response.");
        }

        Map<String, Object> payuResponse = payUCommandService.checkRefundStatus(requestId);
        String gatewayStatus = String.valueOf(payuResponse.getOrDefault("status", ""));
        String gatewayMessage = String.valueOf(payuResponse.getOrDefault("msg", payuResponse.getOrDefault("message", "No status message")));

        if ("1".equals(gatewayStatus)) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundLifecycleStatus(RefundLifecycleStatus.SUCCESS);
            paymentRepository.save(payment);
            return buildRefundResponse("SUCCESS", payment, "Refund completed: " + gatewayMessage);
        }

        if ("0".equals(gatewayStatus)) {
            if (payment.getRefundLifecycleStatus() != RefundLifecycleStatus.SUCCESS) {
                payment.setRefundLifecycleStatus(RefundLifecycleStatus.FAILURE);
                paymentRepository.save(payment);
                return buildRefundResponse("FAILURE", payment, "Refund failed at gateway: " + gatewayMessage);
            }
            return buildRefundResponse("SUCCESS", payment, "Refund already completed; latest gateway message: " + gatewayMessage);
        }

        if (payment.getRefundLifecycleStatus() != RefundLifecycleStatus.SUCCESS) {
            payment.setRefundLifecycleStatus(RefundLifecycleStatus.PENDING);
            paymentRepository.save(payment);
        }
        return buildRefundResponse("PENDING", payment, "Refund status from PayU: " + gatewayMessage);
    }

    private RefundResponse buildRefundResponse(String normalizedStatus, Payment payment, String message) {
        return RefundResponse.builder()
                .status(normalizedStatus)
                .message(message)
                .amount(payment.getRefundAmount())
                .currency("INR")
                .txnId(payment.getTxnId())
                .refundId(payment.getRefundId())
                .gatewayRequestId(payment.getRefundRequestId())
                .reason(payment.getRefundReason())
                .build();
    }

    private Payment resolvePaymentByTxnId(String txnId) {
        return paymentRepository.findByTxnId(txnId)
                .orElseThrow(() -> new BadRequestException("Payment not found for txnId"));
    }

    private boolean isManualOrPending(String gatewayMessage) {
        if (gatewayMessage == null) {
            return false;
        }
        String normalized = gatewayMessage.toLowerCase();
        return normalized.contains("manual")
                || normalized.contains("follow-up")
                || normalized.contains("pending")
                || normalized.contains("queued")
                || normalized.contains("processing")
                || normalized.contains("request received");
    }
}
