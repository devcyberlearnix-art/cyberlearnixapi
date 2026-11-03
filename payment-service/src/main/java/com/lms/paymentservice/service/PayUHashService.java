package com.lms.paymentservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class PayUHashService {

    @Value("${payu.key}")
    private String key;

    @Value("${payu.salt}")
    private String salt;

    /**
     * Generate hash for payment request
     */
    public String generateHash(String txnId,
                               String amount,
                               String productInfo,
                               String firstName,
                               String email,
                               String udf1,
                               String udf2,
                               String udf3,
                               String udf4,
                               String udf5) {

        try {
            // PayU hash format
            String sequence = key + "|" + txnId + "|" + amount + "|" +
                    productInfo + "|" + firstName + "|" + email + "|" +
                    getValue(udf1) + "|" +
                    getValue(udf2) + "|" +
                    getValue(udf3) + "|" +
                    getValue(udf4) + "|" +
                    getValue(udf5) +
                    "||||||" + salt;

            return hashSHA512(sequence);

        } catch (Exception e) {
            throw new RuntimeException("Hash generation failed", e);
        }
    }

    /**
     * Validate PayU response hash
     */
    public boolean validatePaymentHash(String status,
                                       String txnId,
                                       String amount,
                                       String productInfo,
                                       String firstName,
                                       String email,
                                       String hash) {
        return validatePaymentHash(status, txnId, amount, productInfo, firstName, email, null, hash);
    }

    public boolean validatePaymentHash(String status,
                                       String txnId,
                                       String amount,
                                       String productInfo,
                                       String firstName,
                                       String email,
                                       String additionalCharges,
                                       String hash) {
        try {
            if (hash == null || hash.isBlank()) {
                return false;
            }
            String targetHash = hash.trim();

            if (additionalCharges != null && !additionalCharges.isBlank()) {
                String hashWithCharges = generateCallbackHash(status, txnId, amount, productInfo, firstName, email, additionalCharges);
                if (hashWithCharges.equalsIgnoreCase(targetHash)) {
                    return true;
                }
            }

            String standardHash = generateCallbackHash(status, txnId, amount, productInfo, firstName, email, null);
            return standardHash.equalsIgnoreCase(targetHash);
        } catch (Exception e) {
            return false;
        }
    }

    public String generateCallbackHash(String status,
                                       String txnId,
                                       String amount,
                                       String productInfo,
                                       String firstName,
                                       String email) {
        return generateCallbackHash(status, txnId, amount, productInfo, firstName, email, null);
    }

    public String generateCallbackHash(String status,
                                       String txnId,
                                       String amount,
                                       String productInfo,
                                       String firstName,
                                       String email,
                                       String additionalCharges) {
        try {
            StringBuilder sb = new StringBuilder();
            if (additionalCharges != null && !additionalCharges.isBlank()) {
                sb.append(additionalCharges.trim()).append("|");
            }
            sb.append(salt).append("|")
              .append(getValue(status).trim())
              .append("|||||||||||")
              .append(getValue(email).trim()).append("|")
              .append(getValue(firstName).trim()).append("|")
              .append(getValue(productInfo).trim()).append("|")
              .append(getValue(amount).trim()).append("|")
              .append(getValue(txnId).trim()).append("|")
              .append(key);

            return hashSHA512(sb.toString());
        } catch (Exception e) {
            throw new RuntimeException("Callback hash generation failed", e);
        }
    }

    /**
     * Hash for PayU command APIs (refund/verify)
     */
    public String generateCommandHash(String command, String var1) {

        try {
            String sequence = key + "|" + command + "|" + var1 + "|" + salt;
            return hashSHA512(sequence);

        } catch (Exception e) {
            throw new RuntimeException("Command hash generation failed", e);
        }
    }

    public String generateHashWithUdfs(String txnId,
                                       String amount,
                                       String productInfo,
                                       String firstName,
                                       String email,
                                       String udf1,
                                       String udf2,
                                       String udf3,
                                       String udf4,
                                       String udf5) {
        return generateHash(
                txnId,
                amount,
                productInfo,
                firstName,
                email,
                udf1,
                udf2,
                udf3,
                udf4,
                udf5
        );
    }

    /**
     * Helper to avoid null issues
     */
    private String getValue(String value) {
        return value == null ? "" : value;
    }

    /**
     * SHA-512 hashing
     */
    private String hashSHA512(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }
}
