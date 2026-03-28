package com.cyberlearnix.commonlibs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Device information for security and analytics")
public class DeviceInfoDTO {
    
    @Schema(description = "Unique device identifier", example = "device123")
    private String deviceId;
    
    @Schema(description = "Device name", example = "John's iPhone")
    private String deviceName;
    
    @Schema(description = "Device type", example = "mobile")
    private String deviceType;
    
    @Schema(description = "Operating system name", example = "iOS")
    private String osName;
    
    @Schema(description = "Operating system version", example = "15.0")
    private String osVersion;
    
    @Schema(description = "Application version", example = "1.0.0")
    private String appVersion;
    
    @Schema(description = "Browser name", example = "Safari")
    private String browserName;
    
    @Schema(description = "Browser version", example = "15.0")
    private String browserVersion;
    
    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;
    
    @Schema(description = "User agent string", example = "Mozilla/5.0...")
    private String userAgent;
    
    @Schema(description = "Location", example = "Mumbai, India")
    private String location;
    
    @Schema(description = "Timezone", example = "Asia/Kolkata")
    private String timezone;
}
