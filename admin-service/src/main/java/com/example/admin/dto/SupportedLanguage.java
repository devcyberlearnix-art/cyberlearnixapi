package com.example.admin.dto;

import java.util.Arrays;

public enum SupportedLanguage {
    EN("English"),
    TE("Telugu"),
    HI("Hindi");

    private final String displayName;

    SupportedLanguage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static boolean isValid(String code) {
        if (code == null) return false;
        return Arrays.stream(values())
                .anyMatch(lang -> lang.name().equalsIgnoreCase(code.trim()));
    }
}
