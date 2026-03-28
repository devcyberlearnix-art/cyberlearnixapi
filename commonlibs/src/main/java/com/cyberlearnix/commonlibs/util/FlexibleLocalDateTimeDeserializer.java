package com.cyberlearnix.commonlibs.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Deserializes a date/datetime string into {@link LocalDateTime}.
 *
 * <p>Handles the following formats in order:
 * <ol>
 *   <li>{@code yyyy-MM-dd'T'HH:mm:ss.SSS}  (e.g. 2026-01-17T10:30:00.000)</li>
 *   <li>{@code yyyy-MM-dd'T'HH:mm:ss}       (e.g. 2026-01-17T10:30:00)</li>
 *   <li>{@code yyyy-MM-dd'T'HH:mm}          (e.g. 2026-01-17T10:30)</li>
 *   <li>{@code yyyy-MM-dd}                  (e.g. 2026-01-17) → midnight</li>
 *   <li>Long epoch-millis                   (e.g. 1705468800000)</li>
 * </ol>
 *
 * <p>Used to avoid deserialization failures when Elasticsearch stores
 * date-only values where the mapping allows both date and datetime.
 */
public class FlexibleLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final DateTimeFormatter[] DATETIME_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
    };

    public FlexibleLocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String raw = p.getText();
        if (raw == null || raw.isBlank()) {
            return null;
        }

        // Try to parse as epoch millis first
        try {
            long epochMillis = Long.parseLong(raw);
            return java.time.Instant.ofEpochMilli(epochMillis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (NumberFormatException ignored) {
            // not a number — fall through to string parsing
        }

        // Try all datetime patterns
        for (DateTimeFormatter fmt : DATETIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(raw, fmt);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }

        // Last resort: date-only (e.g. "2026-01-17") → start of day
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IOException(
                "Unable to parse '" + raw + "' as LocalDateTime or LocalDate", e);
        }
    }
}
