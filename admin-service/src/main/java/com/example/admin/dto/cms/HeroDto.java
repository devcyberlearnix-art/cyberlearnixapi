package com.example.admin.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class HeroDto {

    @Builder.Default
    private String badgeText = "Architecture Curriculum";

    @Builder.Default
    private String verificationLabel = "Verified Production Track";

    @Builder.Default
    private String heading = "Master Engineering On Real Architecture.";

    @Builder.Default
    private String headingEmphasis = "On Real Architecture.";

    @Builder.Default
    private String description = "Zero synthetic boilerplate. Learn distributed microservices, fullstack design systems, security verification, and cloud-native resilience directly from verified engineering leads.";

    private CtaDto primaryCta;
    private CtaDto secondaryCta;

    @Builder.Default
    private String searchPlaceholder = "Search tracks by topic, distributed architecture, or lead...";

    @Builder.Default
    private boolean searchVisible = true;

    @Builder.Default
    private String textAlignment = "center";

    private String backgroundImageUrl;
    private String mobileImageUrl;

    @Builder.Default
    private boolean sectionVisible = true;

    // Helper getter for nested representation if needed
    public Map<String, Object> getBadge() {
        return Map.of("text", badgeText != null ? badgeText : "", "isVisible", true);
    }

    @JsonProperty("badge")
    public void unpackBadge(Map<String, Object> badge) {
        if (badge != null && badge.containsKey("text")) {
            this.badgeText = String.valueOf(badge.get("text"));
        }
    }

    @JsonProperty("verificationLabel")
    public void unpackVerification(Object ver) {
        if (ver instanceof Map map && map.containsKey("text")) {
            this.verificationLabel = String.valueOf(map.get("text"));
        } else if (ver instanceof String s) {
            this.verificationLabel = s;
        }
    }

    @JsonProperty("heading")
    public void unpackHeading(Object h) {
        if (h instanceof Map map) {
            if (map.containsKey("text")) this.heading = String.valueOf(map.get("text"));
            if (map.containsKey("emphasisText")) this.headingEmphasis = String.valueOf(map.get("emphasisText"));
        } else if (h instanceof String s) {
            this.heading = s;
        }
    }

    @JsonProperty("description")
    public void unpackDesc(Object d) {
        if (d instanceof Map map && map.containsKey("text")) {
            this.description = String.valueOf(map.get("text"));
        } else if (d instanceof String s) {
            this.description = s;
        }
    }

    @JsonProperty("search")
    public void unpackSearch(Map<String, Object> search) {
        if (search != null) {
            if (search.containsKey("placeholder")) this.searchPlaceholder = String.valueOf(search.get("placeholder"));
            if (search.containsKey("isVisible")) this.searchVisible = Boolean.TRUE.equals(search.get("isVisible"));
        }
    }

    @JsonProperty("appearance")
    public void unpackAppearance(Map<String, Object> app) {
        if (app != null) {
            if (app.containsKey("textAlignment")) this.textAlignment = String.valueOf(app.get("textAlignment"));
            if (app.containsKey("backgroundImageUrl")) this.backgroundImageUrl = String.valueOf(app.get("backgroundImageUrl"));
        }
    }
}
