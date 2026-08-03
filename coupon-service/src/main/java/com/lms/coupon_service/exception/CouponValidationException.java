package com.lms.coupon_service.exception;

import com.lms.coupon_service.dto.ErrorDetail;
import lombok.Getter;

import java.util.List;

@Getter
public class CouponValidationException extends RuntimeException {
    private final List<ErrorDetail> errors;

    public CouponValidationException(String message, List<ErrorDetail> errors) {
        super(message);
        this.errors = errors;
    }
}
