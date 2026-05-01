package com.lms.cart_service.client;

// ADD THIS LINE (use your actual package name where the Course class is)
import com.lms.cart_service.dto.CartItem;
import com.lms.cart_service.dto.CourseDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name = "course-service", url = "${course-service.url}")
public interface CourseClient {
    // Return CourseDetails (the DTO) instead of CartItem
    @GetMapping("/courses/{courseId}")
    CourseDetails getCourseById(@PathVariable("courseId") String courseId);
}