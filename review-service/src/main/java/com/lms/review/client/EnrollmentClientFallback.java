package com.lms.review.client;

import com.lms.review.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class EnrollmentClientFallback implements EnrollmentClient {

    @Override
    public List<EnrollmentInfo> getEnrollmentsByUserId(UUID userId) {
        log.warn("Enrollment service unavailable for userId={}", userId);
        throw new BusinessException("Enrollment service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
