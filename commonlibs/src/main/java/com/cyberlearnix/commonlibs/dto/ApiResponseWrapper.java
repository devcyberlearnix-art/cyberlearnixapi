package com.cyberlearnix.commonlibs.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponseWrapper<T> {
    private boolean success;
    private String message;
    private T data;
    private Map<String, Object> meta;
    private Map<String, Object> error;

    public ApiResponseWrapper(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.meta = new HashMap<>();
        this.error = new HashMap<>();
    }

    public ApiResponseWrapper(boolean success, String message, T data, Map<String, Object> meta) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.meta = meta != null ? meta : new HashMap<>();
        this.error = new HashMap<>();
    }
}
