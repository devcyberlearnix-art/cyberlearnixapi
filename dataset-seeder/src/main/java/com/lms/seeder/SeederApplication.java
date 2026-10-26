package com.lms.seeder;

import com.lms.seeder.service.DatasetSeeder;
import com.lms.seeder.verification.DatabaseVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class SeederApplication {

    private final DatasetSeeder datasetSeeder;
    private final DatabaseVerifier databaseVerifier;

    public static void main(String[] args) {
        SpringApplication.run(SeederApplication.class, args);
    }

    @Bean
    public CommandLineRunner runSeeder() {
        return args -> {
            log.info("========================================");
            log.info("Starting LMS Dataset Seeder");
            log.info("========================================");
            
            try {
                datasetSeeder.seedAll();
                
                log.info("========================================");
                log.info("Running database verification...");
                log.info("========================================");
                databaseVerifier.verifyAll();
                
                log.info("========================================");
                log.info("Dataset seeding completed successfully!");
                log.info("========================================");
                System.exit(0);
            } catch (Exception e) {
                log.error("========================================");
                log.error("Dataset seeding failed!", e);
                log.error("========================================");
                System.exit(1);
            }
        };
    }
}
