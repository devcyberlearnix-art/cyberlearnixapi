package com.lms.courseservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseSearchControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnEmptyCollectionWhenQueryIsLessThanTwoCharacters() throws Exception {
        mockMvc.perform(get("/api/v1/courses/suggestions")
                .param("q", "k")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void shouldReturnSuggestionsPayloadOnValidPrefixQuery() throws Exception {
        mockMvc.perform(get("/api/v1/courses/suggestions")
                .param("q", "sec")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").greaterThanOrEqualTo(1))
                .andExpect(jsonPath("$[0].type").exists())
                .andExpect(jsonPath("$[0].value").exists());
    }
}

