package com.lms.review.client;

import com.lms.review.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnrollmentClientFallback implements EnrollmentClient {

    @Override
    public EnrollmentCheckResponse checkEnrollment(Long courseId) {
        log.warn("Enrollment service unavailable for courseId={}", courseId);
        throw new BusinessException("Enrollment service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
