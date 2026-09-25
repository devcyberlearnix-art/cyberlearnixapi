package com.user.register.dto.unified;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthProviderResponse {

    private boolean success;
    private String message;
    private List<ProviderInfo> providers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderInfo {
        private String id;
        private String name;
        private String authorizationUrl;
        private String icon;
        private String scope;
        private boolean enabled;
    }
}
