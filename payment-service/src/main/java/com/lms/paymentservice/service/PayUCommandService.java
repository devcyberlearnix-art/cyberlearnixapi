package com.lms.paymentservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.paymentservice.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PayUCommandService {

    private final RestTemplate restTemplate;
    private final PayUHashService payUHashService;
    private final ObjectMapper objectMapper;

    @Value("${payu.base-url}")
    private String payuBaseUrl;

    @Value("${payu.postservice-path:/merchant/postservice}")
    private String payuPostservicePath;

    @Value("${payu.key}")
    private String key;

    public PayUCommandService(PayUHashService payUHashService) {
        this.payUHashService = payUHashService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> verifyPayment(String txnId) {
        return invokePayuCommand("verify_payment", txnId, null, null);
    }

    /**
     * PayU Check Payment API:
     * command=check_payment
     * var1=mihpayid (PayU payment id)
     */
    public Map<String, Object> checkPayment(String mihpayid) {
        return invokePayuCommand("check_payment", mihpayid, null, null);
    }

    /**
     * PayU Get TDR API:
     * command=get_TDR
     * var1=mihpayid (PayU payment id)
     */
    public Map<String, Object> getTdr(String mihpayid) {
        return invokePayuCommand("get_TDR", mihpayid, null, null);
    }

    /**
     * PayU Get Transaction Details API:
     * command=get_Transaction_Details
     * var1=fromDate (yyyy-mm-dd)
     * var2=toDate (yyyy-mm-dd)
     */
    public Map<String, Object> getTransactionDetails(String fromDate, String toDate) {
        return invokePayuCommand("get_Transaction_Details", fromDate, toDate, null);
    }

    /**
     * PayU Refund/Cancel API:
     * command=cancel_refund_transaction
     * var1=mihpayid (PayU payment id)
     * var2=token (unique per refund attempt)
     * var3=amount
     */
    public Map<String, Object> initiateRefund(String mihpayid, String token, Double amount) {
        return invokePayuCommand("cancel_refund_transaction", mihpayid, token, amount);
    }

    public Map<String, Object> checkRefundStatus(String refundTxnId) {
        // Check refund/cancel status using request_id returned by refund call.
        return invokePayuCommand("check_action_status_txnid", refundTxnId, null, null);
    }

    /**
     * Generic PayU command API call.
     *
     * Supported combinations used in this service:
     * - verify_payment: var1=txnid
     * - cancel_refund_transaction: var1=mihpayid, var2=token, var3=amount
     * - check_action_status_txnid: var1=request_id
     */
    private Map<String, Object> invokePayuCommand(String command, String var1, String var2, Double var3) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("key", key);
        body.add("command", command);
        body.add("var1", var1);
        if (var2 != null) {
            body.add("var2", var2);
        }
        if (var3 != null) {
            body.add("var3", String.format("%.2f", var3));
        }
        body.add("hash", payUHashService.generateCommandHash(command, var1));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        String url = payuBaseUrl + payuPostservicePath + "?form=2";

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );
        } catch (RestClientException ex) {
            throw new BadRequestException("Payment gateway request failed: " + ex.getMessage());
        }

        String responseBody = response.getBody();
        if (responseBody == null || responseBody.isBlank()) {
            throw new BadRequestException("No response from payment gateway");
        }

        String trimmed = responseBody.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                return objectMapper.readValue(trimmed, new TypeReference<Map<String, Object>>() {});
            } catch (Exception ex) {
                throw new BadRequestException("Payment gateway returned malformed JSON: " + ex.getMessage());
            }
        }

        // PayU can return HTML when credentials/endpoint/environment is incorrect.
        throw new BadRequestException(
                "Payment gateway returned non-JSON response (likely wrong base-url/key/salt or rejected command). "
                        + "Response preview: " + preview(trimmed)
        );
    }

    private String preview(String responseBody) {
        String singleLine = responseBody.replaceAll("\\s+", " ").trim();
        if (singleLine.length() <= 220) {
            return singleLine;
        }
        return singleLine.substring(0, 220) + "...";
    }
}
