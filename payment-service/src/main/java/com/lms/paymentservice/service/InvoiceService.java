package com.lms.paymentservice.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.lms.paymentservice.dto.InvoiceDto;
import com.lms.paymentservice.entity.Payment;
import com.lms.paymentservice.entity.RefundLifecycleStatus;
import com.lms.paymentservice.entity.PaymentStatus;
import com.lms.paymentservice.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private final PaymentService paymentService;

    @Value("${invoice.merchant.name:ABC Technologies}")
    private String merchantName;

    @Value("${invoice.merchant.gstin:}")
    private String merchantGstin;

    @Value("${invoice.merchant.email:}")
    private String merchantEmail;

    @Value("${invoice.gst.rate:0.18}")
    private double gstRate;

    public InvoiceService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public InvoiceDto generateInvoiceJson(String txnId) {
        Payment payment = paymentService.getPaymentByTxnId(txnId);

        String invoiceNumber = "INV-" + payment.getId();
        String invoiceDate = LocalDate.now().toString();

        double subtotal = safeAmount(payment.getAmount());
        double gst = round2(subtotal * gstRate);
        double grandTotal = round2(subtotal + gst);

        InvoiceDto.Merchant merchant = InvoiceDto.Merchant.builder()
                .name(merchantName)
                .gstin(merchantGstin)
                .email(merchantEmail)
                .build();

        InvoiceDto.Customer customer = InvoiceDto.Customer.builder()
                .name(payment.getFirstName())
                .email(payment.getEmail())
                .phone(payment.getPhone())
                .build();

        InvoiceDto.Item item = InvoiceDto.Item.builder()
                .name(payment.getProductInfo())
                .qty(1)
                .price(round2(subtotal))
                .total(round2(subtotal))
                .build();

        InvoiceDto.PaymentInfo paymentInfo = InvoiceDto.PaymentInfo.builder()
                .gateway("PayU")
                .mihpayid(payment.getPayuPaymentId())
                .status(payment.getStatus().name())
                .build();

        String refundStatus = null;
        if (payment.getRefundLifecycleStatus() != null) {
            refundStatus = payment.getRefundLifecycleStatus().name();
        } else if (payment.getStatus() == PaymentStatus.REFUNDED) {
            refundStatus = RefundLifecycleStatus.SUCCESS.name();
        }

        InvoiceDto.RefundInfo refundInfo = InvoiceDto.RefundInfo.builder()
                .request_id(payment.getRefundRequestId())
                .status(refundStatus)
                .amount(payment.getRefundAmount())
                .reason(payment.getRefundReason())
                .build();

        String invoiceCurrency = payment.getCurrency() != null && !payment.getCurrency().isBlank()
                ? payment.getCurrency().toUpperCase()
                : "INR";

        InvoiceDto.Summary summary = InvoiceDto.Summary.builder()
                .currency(invoiceCurrency)
                .subtotal(round2(subtotal))
                .gst(round2(gst))
                .grandTotal(round2(grandTotal))
                .build();

        return InvoiceDto.builder()
                .currency(invoiceCurrency)
                .invoiceNumber(invoiceNumber)
                .invoiceDate(invoiceDate)
                .merchant(merchant)
                .customer(customer)
                .items(List.of(item))
                .payment(paymentInfo)
                .refund(refundInfo)
                .summary(summary)
                .build();
    }

    public byte[] generateInvoice(String txnId) {
        Payment payment = paymentService.getPaymentByTxnId(txnId);

        if (!payment.getStatus().equals(PaymentStatus.SUCCESS)) {
            throw new BadRequestException("Invoice can only be generated for successful payments");
        }

        String invoiceCurrency = payment.getCurrency() != null && !payment.getCurrency().isBlank()
                ? payment.getCurrency().toUpperCase()
                : "INR";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("INVOICE").setFontSize(24).setBold());
            document.add(new Paragraph("--------------------------------------------------"));
            
            document.add(new Paragraph("Payment ID: " + payment.getId()));
            document.add(new Paragraph("Transaction ID: " + payment.getTxnId()));
            document.add(new Paragraph("Date: " + payment.getCreatedAt().toString()));
            document.add(new Paragraph("Customer Email: " + payment.getEmail()));
            document.add(new Paragraph("Product: " + payment.getProductInfo()));
            document.add(new Paragraph("Status: " + payment.getStatus().name()));
            document.add(new Paragraph("PayU ID: " + payment.getPayuPaymentId()));
            
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph(
                    "Amount Paid: \u20B9 " + round2(safeAmount(payment.getAmount())) + " " + invoiceCurrency
            ).setBold());

            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private double safeAmount(Double amount) {
        return amount == null ? 0.0 : amount;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
