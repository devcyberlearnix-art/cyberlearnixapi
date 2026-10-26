package com.user.register.service;

import com.user.register.entity.AuditLog;
import com.user.register.entity.User;
import com.user.register.repository.AuditLogRepository;
import com.user.register.repository.InstructorApplicationRepository;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.UnifiedJwtService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = UserDeletionTransactionTest.JpaTestConfiguration.class,
        properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserDeletionTransactionTest {

        @SpringBootConfiguration
        @EnableAutoConfiguration
        @EntityScan(basePackageClasses = User.class)
        @EnableJpaRepositories(basePackageClasses = UserRepository.class)
        @Import(UserService.class)
        static class JpaTestConfiguration {
        }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository users;

    @Autowired
    private AuditLogRepository auditLogs;

    @Autowired
    private InstructorApplicationRepository instructorApplications;

    @Autowired
    private UserSessionRepository userSessions;

    @MockBean
    private UnifiedJwtService jwtService;

    @MockBean
    private SessionService sessions;

    @MockBean
    private TokenBlacklistService blacklist;

    @MockBean
    private GoogleDriveService googleDrive;

    @Disabled("Email change repositories removed - test no longer applicable")
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Rollback(false)
    void databaseFailureRollsBackUserAndEarlierDependentDeletes() {
        // This test uses EmailChangeRequest repository which was removed
    }

    @Disabled("Email change repositories removed - test no longer applicable")
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Rollback(false)
    void hardDeleteRemovesTemporaryRowsAndRetainsEmailAuditHistory() {
        // This test uses EmailChangeRequest and EmailChangeAuditLog repositories
        // which were removed in the Google Drive migration
    }
}
