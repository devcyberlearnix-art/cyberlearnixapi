package com.lms.paymentservice.controller;

import com.lms.paymentservice.dto.PaymentRequest;
import com.lms.paymentservice.dto.PaymentResponse;
import com.lms.paymentservice.dto.PaymentCallbackRequest;
import com.lms.paymentservice.dto.PaymentLinkCreateRequest;
import com.lms.paymentservice.dto.PaymentLinkResponse;
import com.lms.paymentservice.dto.PayUConsentCheckoutRequest;
import com.lms.paymentservice.dto.PaymentVerificationResponse;
import com.lms.paymentservice.dto.PaymentVerifyRequest;
import com.lms.paymentservice.dto.RefundRequest;
import com.lms.paymentservice.dto.RefundResponse;
import com.lms.paymentservice.dto.InstructorCoursePaymentDto;
import com.lms.paymentservice.dto.InvoiceDto;
import com.lms.paymentservice.entity.Payment;
import com.lms.paymentservice.service.InvoiceService;
import com.lms.paymentservice.service.PayUCommandService;
import com.lms.paymentservice.service.PayUPaymentLinkService;
import com.lms.paymentservice.service.PayUHashService;
import com.lms.paymentservice.service.PaymentService;
import com.lms.paymentservice.service.RefundService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;
    private final InvoiceService invoiceService;
    private final PayUCommandService payUCommandService;
    private final PayUPaymentLinkService payUPaymentLinkService;
    private final PayUHashService payUHashService;

    @Value("${payu.base-url}")
    private String payuBaseUrl;

    @Value("${payu.key}")
    private String payuKey;

    public PaymentController(PaymentService paymentService,
                             RefundService refundService,
                             InvoiceService invoiceService,
                             PayUCommandService payUCommandService,
                             PayUPaymentLinkService payUPaymentLinkService,
                             PayUHashService payUHashService) {
        this.paymentService = paymentService;
        this.refundService = refundService;
        this.invoiceService = invoiceService;
        this.payUCommandService = payUCommandService;
        this.payUPaymentLinkService = payUPaymentLinkService;
        this.payUHashService = payUHashService;
    }

    // GET /payments - List all payments (for admin service)
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // 1. POST /payments/create
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    // Backend-only flow: create payment and auto-post to PayU without frontend.
    @PostMapping(value = "/create-and-pay", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> createAndPay(@RequestBody PaymentRequest request) {
        PaymentResponse payment = paymentService.createPayment(request);

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Redirecting to PayU</title></head><body>"
                + "<p>Redirecting to payment gateway...</p>"
                + "<form id='payuForm' method='post' action='" + escapeHtml(payment.getPayuActionUrl()) + "'>"
                + hidden("key", payment.getKey())
                + hidden("txnid", payment.getTxnId())
                + hidden("amount", payment.getAmount())
                + hidden("productinfo", payment.getProductInfo())
                + hidden("firstname", payment.getFirstName())
                + hidden("email", payment.getEmail())
                + hidden("phone", payment.getPhone())
                + hidden("surl", payment.getSurl())
                + hidden("furl", payment.getFurl())
                + hidden("hash", payment.getHash())
                + "</form>"
                + "<script>document.getElementById('payuForm').submit();</script>"
                + "</body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    // Browser-driven flow: open actionUrl from /create response.
    @GetMapping(value = "/checkout/{txnId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> checkoutByTxnId(@PathVariable String txnId) {
        PaymentResponse payment = paymentService.getPaymentFormData(txnId);
        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Redirecting to PayU</title></head><body>"
                + "<p>Redirecting to payment gateway...</p>"
                + "<form id='payuForm' method='post' action='" + escapeHtml(payment.getPayuActionUrl()) + "'>"
                + hidden("key", payment.getKey())
                + hidden("txnid", payment.getTxnId())
                + hidden("amount", payment.getAmount())
                + hidden("productinfo", payment.getProductInfo())
                + hidden("firstname", payment.getFirstName())
                + hidden("email", payment.getEmail())
                + hidden("phone", payment.getPhone())
                + hidden("surl", payment.getSurl())
                + hidden("furl", payment.getFurl())
                + hidden("hash", payment.getHash())
                + "</form>"
                + "<script>document.getElementById('payuForm').submit();</script>"
                + "</body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    /**
     * PayU calls surl with POST (form fields). Browser GET is only informational.
     */
    @GetMapping("/callback/success")
    public ResponseEntity<String> successCallbackInfo() {
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(
                "Payment success callback endpoint. PayU submits POST (form-urlencoded) here after checkout; "
                        + "opening this URL in the browser does not complete a callback. "
                        + "Confirm payment with POST /payments/verify (JSON {\"txnId\":\"...\"}).");
    }

    @GetMapping("/callback/failure")
    public ResponseEntity<String> failureCallbackInfo() {
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(
                "Payment failure callback endpoint. PayU submits POST here; browser GET does not simulate a failure callback.");
    }

    // 2. POST /payments/callback/success
    @PostMapping(value = "/callback/success", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> successCallback(@RequestParam Map<String, String> params) {
        paymentService.handleCallback(
                params.get("status"),
                params.get("txnid"),
                params.get("amount"),
                params.get("productinfo"),
                params.get("firstname"),
                params.get("email"),
                params.get("hash"),
                params.get("mihpayid")
        );
        return ResponseEntity.ok("Payment Success Handled");
    }

    // JSON testing endpoint for success callback
    @PostMapping(value = "/callback/success", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> successCallbackJson(@RequestBody PaymentCallbackRequest request) {
        paymentService.handleCallback(
                request.getStatus(),
                request.getTxnid(),
                request.getAmount(),
                request.getProductinfo(),
                request.getFirstname(),
                request.getEmail(),
                request.getHash(),
                request.getMihpayid()
        );
        return ResponseEntity.ok("Payment Success Handled");
    }

    // 3. POST /payments/callback/failure
    @PostMapping(value = "/callback/failure", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> failureCallback(@RequestParam Map<String, String> params) {
        paymentService.handleCallback(
                params.get("status"),
                params.get("txnid"),
                params.get("amount"),
                params.get("productinfo"),
                params.get("firstname"),
                params.get("email"),
                params.get("hash"),
                params.get("mihpayid")
        );
        return ResponseEntity.ok("Payment Failure Handled");
    }

    // JSON testing endpoint for failure callback
    @PostMapping(value = "/callback/failure", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> failureCallbackJson(@RequestBody PaymentCallbackRequest request) {
        paymentService.handleCallback(
                request.getStatus(),
                request.getTxnid(),
                request.getAmount(),
                request.getProductinfo(),
                request.getFirstname(),
                request.getEmail(),
                request.getHash(),
                request.getMihpayid()
        );
        return ResponseEntity.ok("Payment Failure Handled");
    }

    // 4. POST /payments/verify
    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentVerificationResponse> verifyPayment(@RequestBody PaymentVerifyRequest request) {
        Payment payment = paymentService.verifyPayment(request.getTxnId());
        return ResponseEntity.ok(
                PaymentVerificationResponse.builder()
                        .paymentId(payment.getId())
                        .txnId(payment.getTxnId())
                        .status(payment.getStatus().name())
                        .message("Payment verified and status updated")
                        .build()
        );
    }

    /**
     * Gateway-only verify endpoint.
     * Mirrors PayU's verify_payment command API and returns PayU JSON as-is.
     *
     * Example:
     * curl -X POST "http://localhost:8080/payments/payu/verify" -H "Content-Type: application/x-www-form-urlencoded" -d "var1=IhfgcZnXR4o4nB"
     */
    @PostMapping(value = "/payu/verify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> verifyPaymentGateway(@RequestParam("var1") String txnId) {
        return ResponseEntity.ok(payUCommandService.verifyPayment(txnId));
    }

    /**
     * Mirrors PayU command=check_payment (var1=mihpayid).
     */
    @PostMapping(value = "/payu/check-payment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkPaymentGateway(@RequestParam("var1") String mihpayid) {
        return ResponseEntity.ok(payUCommandService.checkPayment(mihpayid));
    }

    /**
     * Mirrors PayU command=get_TDR (var1=mihpayid).
     */
    @PostMapping(value = "/payu/tdr", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getTdrGateway(@RequestParam("var1") String mihpayid) {
        return ResponseEntity.ok(payUCommandService.getTdr(mihpayid));
    }

    /**
     * Mirrors PayU command=get_Transaction_Details:
     * var1=fromDate (yyyy-mm-dd)
     * var2=toDate (yyyy-mm-dd)
     */
    @PostMapping(value = "/payu/transaction-details", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getTransactionDetailsGateway(@RequestParam("var1") String fromDate,
                                                                           @RequestParam("var2") String toDate) {
        return ResponseEntity.ok(payUCommandService.getTransactionDetails(fromDate, toDate));
    }

    /**
     * PayU OneAPI: Create Payment Link.
     * Mirrors:
     * POST https://uatoneapi.payu.in/payment-links/
     * Headers: merchantId, Authorization: Bearer <token>
     */
    @PostMapping(value = "/payu/payment-links", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentLinkResponse> createPaymentLink(@RequestBody PaymentLinkCreateRequest request) {
        return ResponseEntity.ok(
                payUPaymentLinkService.createPaymentLink(
                        request.getMerchantId(),
                        request.getAccessToken(),
                        request.getPayload()
                )
        );
    }

    /**
     * PayU Hosted Checkout - Consent / SI form.
     * Returns an auto-submitting HTML page that POSTs to PayU /_payment with SI fields.
     */
    @PostMapping(value = "/payu/consent-checkout", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> payuConsentCheckout(@RequestBody PayUConsentCheckoutRequest req) {
        String key = (req.getKey() == null || req.getKey().isBlank()) ? payuKey : req.getKey().trim();
        String txnid = req.getTxnid();
        String amount = req.getAmount();

        String hash = payUHashService.generateHashWithUdfs(
                txnid,
                amount,
                req.getProductinfo(),
                req.getFirstname(),
                req.getEmail(),
                req.getUdf1(),
                req.getUdf2(),
                req.getUdf3(),
                req.getUdf4(),
                req.getUdf5()
        );

        String actionUrl = escapeHtml(payuBaseUrl + "/_payment");

        String html = "<!doctype html><html><body onload=\"document.forms.payu.submit()\">"
                + "<form name=\"payu\" method=\"post\" action=\"" + actionUrl + "\">"
                + hidden("key", key)
                + hidden("txnid", txnid)
                + hidden("amount", amount)
                + hidden("productinfo", req.getProductinfo())
                + hidden("firstname", req.getFirstname())
                + hidden("email", req.getEmail())
                + hidden("phone", req.getPhone())
                + hidden("surl", req.getSurl())
                + hidden("furl", req.getFurl())
                + hidden("lastname", req.getLastname())
                + hidden("address1", req.getAddress1())
                + hidden("address2", req.getAddress2())
                + hidden("city", req.getCity())
                + hidden("state", req.getState())
                + hidden("country", req.getCountry())
                + hidden("zipcode", req.getZipcode())
                + hidden("udf1", req.getUdf1())
                + hidden("udf2", req.getUdf2())
                + hidden("udf3", req.getUdf3())
                + hidden("udf4", req.getUdf4())
                + hidden("udf5", req.getUdf5())
                + hidden("api_version", req.getApi_version())
                + hidden("si_details", req.getSi_details())
                + hidden("si", req.getSi())
                + hidden("hash", hash)
                + "<input type=\"submit\" value=\"Submit Payment\">"
                + "</form></body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    /**
     * Initiate refund using PayU mihpayid (or internal payment id) in the JSON body.
     * POST /payments/refund
     */
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> initiateRefund(@RequestBody RefundRequest request) {
        return ResponseEntity.ok(refundService.initiateRefund(request));
    }

    /**
     * Instructor: learners who paid for a given course (SUCCESS or REFUNDED), with refund reason/status when present.
     */
    @GetMapping("/instructor/{instructorId}/courses/{courseId}/payments")
    public ResponseEntity<List<InstructorCoursePaymentDto>> listCoursePaymentsForInstructor(
            @PathVariable String instructorId,
            @PathVariable String courseId) {
        return ResponseEntity.ok(paymentService.listPaymentsForInstructorCourse(instructorId, courseId));
    }

    // 6. GET /payments/{txnId}/refund-status
    @GetMapping("/{txnId}/refund-status")
    public ResponseEntity<RefundResponse> getRefundStatus(@PathVariable String txnId) {
        return ResponseEntity.ok(refundService.getRefundStatus(txnId));
    }

    // 7. GET /payments/{txnId}/invoice
    @GetMapping(value = "/{txnId}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getInvoice(@PathVariable String txnId) {
        byte[] pdfBytes = invoiceService.generateInvoice(txnId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_" + txnId + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // 8. GET /payments/{txnId}/invoice-json
    @GetMapping(value = "/{txnId}/invoice-json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InvoiceDto> getInvoiceJson(@PathVariable String txnId) {
        return ResponseEntity.ok(invoiceService.generateInvoiceJson(txnId));
    }

    private String hidden(String name, String value) {
        return "<input type='hidden' name='" + escapeHtml(name) + "' value='" + escapeHtml(value) + "' />";
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

}
