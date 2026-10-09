package com.example.notification.service;

import com.cyberlearnix.commonlibs.dto.UserLoginEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditLogService {

    public void logLoginEvent(UserLoginEvent event) {
        log.info("[AUDIT] User Login: userId={}, email={}, ip={}, device={}, os={}, location={}, timestamp={}",
                event.userId(), event.email(), event.ipAddress(), event.device(),
                event.operatingSystem(), event.location(), event.loginTime());
    }
}
