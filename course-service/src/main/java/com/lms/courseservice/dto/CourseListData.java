package com.lms.courseservice.dto;

import com.lms.courseservice.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseListData {
    private List<Course> courses;
    private TrendingPagination pagination;
}