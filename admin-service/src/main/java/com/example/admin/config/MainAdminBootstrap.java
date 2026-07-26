package com.example.admin.config;

import com.example.admin.entity.Admin;
import com.example.admin.entity.AdminApprovalStatus;
import com.example.admin.entity.AdminType;
import com.example.admin.entity.AssignedService;
import com.example.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MainAdminBootstrap implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final MainAdminProperties mainAdminProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        adminRepository.findByEmail(mainAdminProperties.getEmail())
                .ifPresentOrElse(this::ensureMainAdminFlags, this::createMainAdmin);
    }

    private void ensureMainAdminFlags(Admin admin) {
        admin.setRole("MAIN_ADMIN");
        admin.setAdminType(AdminType.MAIN_ADMIN);
        admin.setAssignedService(AssignedService.ALL);
        admin.setApprovalStatus(AdminApprovalStatus.APPROVED);
        admin.setVerified(true);
        admin.setPassword(passwordEncoder.encode(mainAdminProperties.getPassword()));
        adminRepository.save(admin);
    }

    private void createMainAdmin() {
        Admin admin = Admin.builder()
                .email(mainAdminProperties.getEmail())
                .password(passwordEncoder.encode(mainAdminProperties.getPassword()))
                .role("MAIN_ADMIN")
                .adminType(AdminType.MAIN_ADMIN)
                .assignedService(AssignedService.ALL)
                .approvalStatus(AdminApprovalStatus.APPROVED)
                .verified(true)
                .firstName(mainAdminProperties.getFirstName())
                .lastName(mainAdminProperties.getLastName())
                .mobileNumber(mainAdminProperties.getMobileNumber())
                .alternateMobileNumber(mainAdminProperties.getMobileNumber())
                .build();
        adminRepository.save(admin);
        System.out.println("Main Admin bootstrapped: " + mainAdminProperties.getEmail());
    }
}
