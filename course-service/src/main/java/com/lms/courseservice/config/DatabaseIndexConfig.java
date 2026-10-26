package com.lms.courseservice.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class DatabaseIndexConfig implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        // Skip PostgreSQL-specific index creation for H2/localtest
        String dialect = entityManager.getEntityManagerFactory().getProperties()
                .get("hibernate.dialect").toString();
        
        if (dialect.contains("H2")) {
            log.info("Skipping PostgreSQL-specific index creation for H2 database");
            return;
        }
        
        createActiveCertificateUniqueIndex();
    }

    private void createActiveCertificateUniqueIndex() {
        try {
            // Check if the index already exists
            String checkIndexSql = """
                SELECT indexname 
                FROM pg_indexes 
                WHERE tablename = 'certificates' 
                AND indexname = 'uk_active_certificate'
                """;

            @SuppressWarnings("unchecked")
            List<String> existingIndexes = entityManager
                    .createNativeQuery(checkIndexSql)
                    .getResultList();

            if (!existingIndexes.isEmpty()) {
                log.info("Partial unique index 'uk_active_certificate' already exists");
                return;
            }

            // Create the partial unique index for ACTIVE certificates only
            String createIndexSql = """
                CREATE UNIQUE INDEX uk_active_certificate 
                ON certificates (student_id, course_id) 
                WHERE status = 'ACTIVE'
                """;

            entityManager.createNativeQuery(createIndexSql).executeUpdate();
            log.info("Successfully created partial unique index 'uk_active_certificate' for ACTIVE certificates");

        } catch (Exception e) {
            log.error("Failed to create partial unique index for ACTIVE certificates", e);
            // Don't fail the application startup, but log the error
        }
    }
}
