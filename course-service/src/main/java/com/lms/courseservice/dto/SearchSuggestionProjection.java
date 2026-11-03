package com.lms.courseservice.dto;

public interface SearchSuggestionProjection {
    String getType();  // Enum Output mapping: 'TITLE', 'SUBTITLE', 'CATEGORY', 'INSTRUCTOR'
    String getValue(); // Exact targeted text token string matches
}

