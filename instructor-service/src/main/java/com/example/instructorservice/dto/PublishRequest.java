package com.example.instructorservice.dto;

import lombok.Data;

@Data
public class PublishRequest {
    private boolean publish; // true = publish, false = unpublish
}
