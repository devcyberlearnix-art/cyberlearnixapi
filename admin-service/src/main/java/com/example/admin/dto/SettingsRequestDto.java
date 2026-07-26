package com.example.admin.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SettingsRequestDto {

    private Map<String, Object> config;
}