package com.cyberlearnix.commonlibs.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for Indian Standard Time (IST - Asia/Kolkata) operations.
 * All time-related operations in the system should use IST timezone.
 * 
 * IST is UTC+5:30 (no daylight saving time)
 */
public class ISTTimeUtil {
    
    /**
     * Indian Standard Time Zone (Asia/Kolkata - UTC+5:30)
     */
    public static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    
    /**
     * UTC Zone for reference
     */
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    
    // ==================== Current Time Methods ====================
    
    /**
     * Get current LocalTime in IST
     * @return Current time in IST timezone
     */
    public static LocalTime now() {
        return LocalTime.now(IST_ZONE);
    }
    
    /**
     * Get current LocalDate in IST
     * @return Current date in IST timezone
     */
    public static LocalDate today() {
        return LocalDate.now(IST_ZONE);
    }
    
    /**
     * Get current LocalDateTime in IST
     * @return Current date-time in IST timezone
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now(IST_ZONE);
    }
    
    /**
     * Get current ZonedDateTime in IST
     * @return Current zoned date-time in IST timezone
     */
    public static ZonedDateTime nowZoned() {
        return ZonedDateTime.now(IST_ZONE);
    }
    
    /**
     * Get current Instant (timezone independent)
     * @return Current instant
     */
    public static Instant nowInstant() {
        return Instant.now();
    }
    
    // ==================== Conversion Methods ====================
    
    /**
     * Convert Instant to LocalDateTime in IST
     * @param instant The instant to convert
     * @return LocalDateTime in IST
     */
    public static LocalDateTime toISTDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, IST_ZONE);
    }
    
    /**
     * Convert Instant to ZonedDateTime in IST
     * @param instant The instant to convert
     * @return ZonedDateTime in IST
     */
    public static ZonedDateTime toISTZoned(Instant instant) {
        return ZonedDateTime.ofInstant(instant, IST_ZONE);
    }
    
    /**
     * Convert LocalDateTime to Instant (assumes IST timezone)
     * @param dateTime The LocalDateTime to convert (assumed to be in IST)
     * @return Instant
     */
    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime.atZone(IST_ZONE).toInstant();
    }
    
    // ==================== Date/Time Calculation Methods ====================
    
    /**
     * Get LocalDateTime N hours from now in IST
     * @param hours Number of hours to add
     * @return LocalDateTime in IST
     */
    public static LocalDateTime plusHours(long hours) {
        return nowDateTime().plusHours(hours);
    }
    
    /**
     * Get LocalDateTime N days from now in IST
     * @param days Number of days to add
     * @return LocalDateTime in IST
     */
    public static LocalDateTime plusDays(long days) {
        return nowDateTime().plusDays(days);
    }
    
    /**
     * Get LocalDateTime N minutes from now in IST
     * @param minutes Number of minutes to add
     * @return LocalDateTime in IST
     */
    public static LocalDateTime plusMinutes(long minutes) {
        return nowDateTime().plusMinutes(minutes);
    }
    
    /**
     * Get LocalDate N days ago in IST
     * @param days Number of days to subtract
     * @return LocalDate in IST
     */
    public static LocalDate minusDays(long days) {
        return today().minusDays(days);
    }
    
    /**
     * Get LocalDateTime N hours ago in IST
     * @param hours Number of hours to subtract
     * @return LocalDateTime in IST
     */
    public static LocalDateTime minusHours(long hours) {
        return nowDateTime().minusHours(hours);
    }
    
    /**
     * Get LocalDateTime N minutes ago in IST
     * @param minutes Number of minutes to subtract
     * @return LocalDateTime in IST
     */
    public static LocalDateTime minusMinutes(long minutes) {
        return nowDateTime().minusMinutes(minutes);
    }
    
    // ==================== Formatting Methods ====================
    
    /**
     * Format current IST time as yyyyMMddHHmmss
     * @return Formatted timestamp string
     */
    public static String timestampString() {
        return nowDateTime().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
    
    /**
     * Format current IST time as yyMMddHHmmss (short format)
     * @return Formatted timestamp string
     */
    public static String shortTimestampString() {
        return nowDateTime().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
    }
    
    /**
     * Format LocalDateTime to ISO string
     * @param dateTime The LocalDateTime to format
     * @return ISO formatted string
     */
    public static String toISOString(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Get current day of week in IST
     * @return DayOfWeek enum
     */
    public static DayOfWeek currentDayOfWeek() {
        return today().getDayOfWeek();
    }
    
    /**
     * Check if current time in IST is between two times
     * @param start Start time (inclusive)
     * @param end End time (inclusive)
     * @return true if current time is within range
     */
    public static boolean isTimeBetween(LocalTime start, LocalTime end) {
        LocalTime current = now();
        return !current.isBefore(start) && !current.isAfter(end);
    }
    
    /**
     * Check if today in IST matches any of the given days
     * @param days Array of day names (e.g., "SUNDAY", "MONDAY")
     * @return true if today matches any day
     */
    public static boolean isTodayAny(String... days) {
        DayOfWeek today = currentDayOfWeek();
        if (days == null) return false;
        
        for (String day : days) {
            if (day != null && day.equalsIgnoreCase(today.name())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get timezone offset string (e.g., "+05:30")
     * @return Timezone offset string
     */
    public static String getTimezoneOffset() {
        return nowZoned().getOffset().toString();
    }
    
    /**
     * Get IST ZoneId
     * @return ZoneId for Asia/Kolkata
     */
    public static ZoneId getZoneId() {
        return IST_ZONE;
    }
}
