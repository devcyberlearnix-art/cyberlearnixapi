package com.user.register.util;

import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamically resolves Device ID, Device Name, Device Type, Browser, OS,
 * and Geolocation (Latitude & Longitude) from incoming HTTP requests without
 * requiring the client to pass these in the request body.
 */
@Slf4j
@Component
public class DynamicDeviceAndLocationResolver {

    public static final String DEVICE_COOKIE_NAME = "_cbl_device_id";
    private final RestTemplate restTemplate;

    // Cache recent IP lookups in memory to prevent rate limits and speed up responses
    private final Map<String, GeoLocation> geoCache = new ConcurrentHashMap<>();
    private volatile GeoLocation cachedLocalEgressGeo = null;

    public DynamicDeviceAndLocationResolver() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(2).toMillis());
        this.restTemplate = new RestTemplate(factory);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientContext {
        private String deviceId;
        private String deviceName;
        private String deviceType;
        private String browser;
        private String operatingSystem;
        private String ipAddress;
        private Double latitude;
        private Double longitude;
        private String city;
        private String country;
    }

    public ClientContext resolve(HttpServletRequest request) {
        return resolve(request, null);
    }

    public ClientContext resolve(HttpServletRequest request, HttpServletResponse response) {
        if (request == null) {
            return ClientContext.builder()
                    .deviceId(UUID.randomUUID().toString())
                    .deviceName("Unknown Device")
                    .deviceType("WEB")
                    .browser("Unknown Browser")
                    .operatingSystem("Unknown OS")
                    .ipAddress("127.0.0.1")
                    .latitude(12.971598)
                    .longitude(77.594566)
                    .city("Bengaluru")
                    .country("India")
                    .build();
        }

        String ip = extractClientIp(request);
        String userAgentHeader = request.getHeader("User-Agent");
        UserAgent ua = UserAgent.parseUserAgentString(userAgentHeader != null ? userAgentHeader : "");

        // 1. Resolve OS & Browser dynamically with extensive fallbacks (curl, Postman, mobile SDKs)
        String osName = detectOS(ua, userAgentHeader, request);
        String browser = detectBrowser(ua, userAgentHeader);
        String deviceType = resolveDeviceType(ua.getOperatingSystem(), userAgentHeader);

        // 2. Resolve Device Name
        String explicitDeviceName = request.getHeader("X-Device-Name");
        String deviceName = (explicitDeviceName != null && !explicitDeviceName.isBlank())
                ? explicitDeviceName.trim()
                : buildDeviceName(browser, osName, userAgentHeader);

        // 3. Resolve Device ID
        String deviceId = getOrGenerateDeviceId(request, response);

        // 4. Resolve Dynamic Latitude & Longitude Geolocation
        GeoLocation geo = resolveGeoLocation(ip, request);

        return ClientContext.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .deviceType(deviceType)
                .browser(browser)
                .operatingSystem(osName)
                .ipAddress(ip)
                .latitude(geo.latitude)
                .longitude(geo.longitude)
                .city(geo.city)
                .country(geo.country)
                .build();
    }

    private String buildDeviceName(String browser, String os, String userAgent) {
        if (userAgent != null) {
            String lower = userAgent.toLowerCase();
            if (lower.contains("postman")) {
                return "Postman API Client on " + os;
            }
            if (lower.contains("insomnia")) {
                return "Insomnia REST Client on " + os;
            }
            if (lower.contains("thunder client") || lower.contains("thunder-client")) {
                return "Thunder Client on " + os;
            }
            if (lower.contains("curl")) {
                return "cURL CLI on " + os;
            }
            if (lower.contains("okhttp") || lower.contains("dart") || lower.contains("expo") || lower.contains("react-native")) {
                return "Mobile App on " + os;
            }
        }
        return browser + " on " + os;
    }

    private String detectBrowser(UserAgent ua, String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown Client";

        String lower = userAgent.toLowerCase();
        if (lower.contains("postmanruntime") || lower.contains("postman")) return "Postman";
        if (lower.contains("insomnia")) return "Insomnia";
        if (lower.contains("thunder client") || lower.contains("thunder-client")) return "Thunder Client";
        if (lower.contains("curl")) return "cURL";
        if (lower.contains("edg/")) return "Microsoft Edge";
        if (lower.contains("chrome/") && !lower.contains("edg/")) return "Google Chrome";
        if (lower.contains("safari/") && !lower.contains("chrome/")) return "Apple Safari";
        if (lower.contains("firefox/")) return "Mozilla Firefox";
        if (lower.contains("opera/") || lower.contains("opr/")) return "Opera";
        if (lower.contains("okhttp")) return "OkHttp";
        if (lower.contains("dart")) return "Flutter/Dart";

        if (ua.getBrowser() != null && ua.getBrowser().getName() != null && !"Unknown".equalsIgnoreCase(ua.getBrowser().getName())) {
            return ua.getBrowser().getName();
        }
        return "HTTP Client";
    }

    private String detectOS(UserAgent ua, String userAgent, HttpServletRequest request) {
        // 1. Check Sec-CH-UA-Platform header (sent by modern browsers & clients)
        if (request != null) {
            String platformHeader = request.getHeader("Sec-CH-UA-Platform");
            if (platformHeader != null && !platformHeader.isBlank()) {
                String platform = platformHeader.replace("\"", "").trim();
                if (!platform.equalsIgnoreCase("unknown")) {
                    return platform;
                }
            }
        }

        // 2. Parse User-Agent string
        if (userAgent != null) {
            String lower = userAgent.toLowerCase();
            if (lower.contains("windows nt 10.0") || lower.contains("windows 10") || lower.contains("windows 11")) return "Windows 11/10";
            if (lower.contains("windows nt 6.3")) return "Windows 8.1";
            if (lower.contains("windows nt 6.1")) return "Windows 7";
            if (lower.contains("windows")) return "Windows";
            if (lower.contains("iphone")) return "iOS (iPhone)";
            if (lower.contains("ipad")) return "iPadOS";
            if (lower.contains("android")) return "Android";
            if (lower.contains("macintosh") || lower.contains("mac os x") || lower.contains("darwin")) return "macOS";
            if (lower.contains("linux")) return "Linux";
            if (lower.contains("x11")) return "Unix/Linux";
        }

        // 3. UserAgentUtils fallback
        OperatingSystem os = ua.getOperatingSystem();
        if (os != null && os.getName() != null && !"Unknown".equalsIgnoreCase(os.getName())) {
            return os.getName();
        }

        // 4. Host OS fallback
        String hostOs = System.getProperty("os.name");
        return hostOs != null ? hostOs : "Windows";
    }

    private String resolveDeviceType(OperatingSystem os, String userAgent) {
        if (userAgent != null) {
            String lower = userAgent.toLowerCase();
            if (lower.contains("postman") || lower.contains("insomnia") || lower.contains("curl") || lower.contains("thunder client")) {
                return "API_CLIENT";
            }
            if (lower.contains("mobile") || lower.contains("android") || lower.contains("iphone")) {
                return "MOBILE";
            }
            if (lower.contains("ipad") || lower.contains("tablet")) {
                return "TABLET";
            }
        }
        if (os != null && os.getDeviceType() == DeviceType.MOBILE) {
            return "MOBILE";
        }
        if (os != null && os.getDeviceType() == DeviceType.TABLET) {
            return "TABLET";
        }
        return "WEB";
    }

    private String getOrGenerateDeviceId(HttpServletRequest request, HttpServletResponse response) {
        // A. Check for existing device ID cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (DEVICE_COOKIE_NAME.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }

        // B. Check standard headers if sent by frontend / mobile apps
        String headerDeviceId = request.getHeader("X-Device-Id");
        if (headerDeviceId != null && !headerDeviceId.isBlank()) {
            return headerDeviceId.trim();
        }

        // C. Generate a unique device ID and set cookie if response is available
        String newDeviceId = UUID.randomUUID().toString();
        if (response != null) {
            try {
                Cookie cookie = new Cookie(DEVICE_COOKIE_NAME, newDeviceId);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year persistence
                response.addCookie(cookie);
            } catch (Exception e) {
                log.debug("Could not attach deviceId cookie to response: {}", e.getMessage());
            }
        }
        return newDeviceId;
    }

    private record GeoLocation(Double latitude, Double longitude, String city, String country) {}

    private GeoLocation resolveGeoLocation(String ip, HttpServletRequest request) {
        // A. Explicit headers from client GPS (if sent by mobile / frontend)
        String explicitLat = request.getHeader("X-Latitude");
        String explicitLon = request.getHeader("X-Longitude");
        if (explicitLat != null && explicitLon != null) {
            try {
                return new GeoLocation(
                        Double.parseDouble(explicitLat),
                        Double.parseDouble(explicitLon),
                        request.getHeader("X-City") != null ? request.getHeader("X-City") : "Current Location",
                        request.getHeader("X-Country") != null ? request.getHeader("X-Country") : "Current Location"
                );
            } catch (Exception ignored) {}
        }

        // B. CDN / Cloudflare Geo Headers
        String cfLat = request.getHeader("CF-IPLatitude");
        String cfLon = request.getHeader("CF-IPLongitude");
        String cfCity = request.getHeader("CF-IPCity");
        String cfCountry = request.getHeader("CF-IPCountry");

        if (cfLat != null && cfLon != null) {
            try {
                return new GeoLocation(
                        Double.parseDouble(cfLat),
                        Double.parseDouble(cfLon),
                        cfCity != null ? cfCity : "Unknown",
                        cfCountry != null ? cfCountry : "Unknown"
                );
            } catch (Exception ignored) { }
        }

        // Check cache first
        if (geoCache.containsKey(ip)) {
            return geoCache.get(ip);
        }

        // C. Dynamic GeoIP lookup for public IPs
        if (isPublicIp(ip)) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> geoResponse = restTemplate.getForObject(
                        "http://ip-api.com/json/" + ip + "?fields=status,country,city,lat,lon",
                        Map.class
                );

                if (geoResponse != null && "success".equals(geoResponse.get("status"))) {
                    Double lat = geoResponse.get("lat") instanceof Number n ? n.doubleValue() : 12.971598;
                    Double lon = geoResponse.get("lon") instanceof Number n ? n.doubleValue() : 77.594566;
                    String city = geoResponse.get("city") != null ? geoResponse.get("city").toString() : "Bengaluru";
                    String country = geoResponse.get("country") != null ? geoResponse.get("country").toString() : "India";
                    GeoLocation loc = new GeoLocation(lat, lon, city, country);
                    geoCache.put(ip, loc);
                    return loc;
                }
            } catch (Exception ex) {
                log.debug("Dynamic GeoIP resolution failed for IP {}: {}", ip, ex.getMessage());
            }
        }

        // D. For localhost / private network requests during development, resolve host's public egress IP location
        if (cachedLocalEgressGeo != null) {
            return cachedLocalEgressGeo;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> localGeo = restTemplate.getForObject(
                    "http://ip-api.com/json/?fields=status,country,city,lat,lon",
                    Map.class
            );
            if (localGeo != null && "success".equals(localGeo.get("status"))) {
                Double lat = localGeo.get("lat") instanceof Number n ? n.doubleValue() : 12.971598;
                Double lon = localGeo.get("lon") instanceof Number n ? n.doubleValue() : 77.594566;
                String city = localGeo.get("city") != null ? localGeo.get("city").toString() : "Bengaluru";
                String country = localGeo.get("country") != null ? localGeo.get("country").toString() : "India";
                cachedLocalEgressGeo = new GeoLocation(lat, lon, city, country);
                return cachedLocalEgressGeo;
            }
        } catch (Exception e) {
            log.debug("Could not resolve local egress geolocation: {}", e.getMessage());
        }

        // Default coordinate fallback
        return new GeoLocation(12.971598, 77.594566, "Bengaluru", "India");
    }

    private boolean isPublicIp(String ip) {
        if (ip == null || ip.isBlank()) return false;
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) return false;
        if (ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("172.16.") || ip.startsWith("169.254.")) return false;
        return true;
    }

    public String extractClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "CF-Connecting-IP",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip.trim();
            }
        }

        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
