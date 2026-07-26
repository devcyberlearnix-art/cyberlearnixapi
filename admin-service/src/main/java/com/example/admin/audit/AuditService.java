package com.example.admin.audit;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuditService {

    public void logAction(UUID userId, String action) {

        System.out.println(
                "AUDIT LOG -> UserId: " + userId +
                        " Action: " + action +
                        " Time: " + LocalDateTime.now()
        );
    }
}
