package com.example.admin.service;

import com.example.admin.client.AdminInstructorServiceClient;
import com.example.admin.client.AdminInstructorServiceClient.InstructorDTO;
import com.example.admin.client.AdminInstructorServiceClient.CourseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorService {

    private final AdminInstructorServiceClient instructorServiceClient;

    public List<InstructorDTO> getAllInstructors() {
        return instructorServiceClient.getAllInstructors();
    }

    public InstructorDTO getInstructorById(Long instructorId) {
        return instructorServiceClient.getInstructorById(instructorId);
    }

    public List<CourseDTO> getCoursesByInstructor(Long instructorId) {
        return instructorServiceClient.getCoursesByInstructor(instructorId);
    }
}
