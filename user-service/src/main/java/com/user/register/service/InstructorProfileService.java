package com.user.register.service;

import com.user.register.dto.InstructorProfileDTO;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstructorProfileService {

    private final UserRepository userRepository;

    public InstructorProfileDTO getInstructorProfile(UUID instructorId) {
        User user = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        if (user.getRole() != User.Role.INSTRUCTOR && !Boolean.TRUE.equals(user.getIsInstructorApproved())) {
            throw new RuntimeException("User is not an approved instructor");
        }

        return InstructorProfileDTO.builder()
                .instructorId(user.getId())
                .name(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .headline(user.getSkills() != null ? extractHeadline(user.getSkills()) : "Expert Instructor")
                .bio(user.getOrganization() != null ? user.getOrganization() : "Professional instructor")
                .expertise(user.getSkills() != null ? parseSkills(user.getSkills()) : List.of())
                .rating(4.5) // Default rating - would come from instructor service in production
                .totalCourses(0) // Would come from course service in production
                .totalStudents(0) // Would come from enrollment service in production
                .verified(Boolean.TRUE.equals(user.getIsInstructorApproved()))
                .build();
    }

    private String extractHeadline(String skills) {
        if (skills == null || skills.isEmpty()) {
            return "Expert Instructor";
        }
        String[] skillArray = skills.split(",");
        return skillArray.length > 0 ? skillArray[0].trim() : "Expert Instructor";
    }

    private List<String> parseSkills(String skills) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }
        return List.of(skills.split(","));
    }
}
