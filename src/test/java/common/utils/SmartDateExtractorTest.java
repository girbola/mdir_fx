package common.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartDateExtractorTest {

    /**
     * Tests for the extractInstant method in the SmartDateExtractor class.
     * This method attempts to parse a given text and extract a valid Instant
     * object based on various date-time patterns.
     */

    @Test
    void testExtractInstantWithSeparatedDateTime() {
        String input = "2025-09-04 14:55:12";
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertTrue(result.isPresent());
        assertEquals("2025-09-04T14:55:12Z", result.get().toString());
    }

    @Test
    void testExtractInstantWithCompactDateTime() {
        String input = "20250904145512";
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertTrue(result.isPresent());
        assertEquals("2025-09-04T14:55:12Z", result.get().toString());
    }

    @Test
    void testExtractInstantWithSeparatedDate() {
        String input = "2025-09-04";
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertTrue(result.isPresent());
        assertEquals("2025-09-04T00:00:00Z", result.get().toString());
    }

    @Test
    void testExtractInstantWithCompactDate() {
        String input = "20250904";
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertTrue(result.isPresent());
        assertEquals("2025-09-04T00:00:00Z", result.get().toString());
    }

    @Test
    void testExtractInstantWithCustomZone() {
        String input = "2025-09-04 14:55:12";
        ZoneId customZone = ZoneId.of("America/New_York");
        Optional<Instant> result = SmartDateExtractor.extractInstant(input, customZone);
        assertTrue(result.isPresent());
        Instant expected = Instant.parse("2025-09-04T18:55:12Z");
        assertEquals(expected, result.get());
    }

    @Test
    void testExtractInstantWithInvalidDate() {
        String input = "2025-13-04";
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertFalse(result.isPresent());
    }

    @Test
    void testExtractInstantWithInvalidDateTimeFormat() {
        String input = "04-09-2025 14:55";
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertFalse(result.isPresent());
    }

    @Test
    void testExtractInstantWithPartialDateTime() {
        String input = "2025-09-04 14";
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertFalse(result.isPresent());
    }

    @Test
    void testExtractInstantWithTextContainingValidDate() {
        String input = "Some text and date 2025-09-04 14:55:12 here.";
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertTrue(result.isPresent());
        assertEquals("2025-09-04T14:55:12Z", result.get().toString());
    }

    @Test
    void testExtractInstantWithTextContainingCompactDate() {
        String input = "Here is a compact date 20250904";
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertTrue(result.isPresent());
        assertEquals("2025-09-04T00:00:00Z", result.get().toString());
    }

    @Test
    void testExtractInstantWithEmptyInput() {
        String input = "";
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertFalse(result.isPresent());
    }

    @Test
    void testExtractInstantWithNullInput() {
        String input = null;
        Optional<Instant> result = SmartDateExtractor.extractInstant(input);
        assertFalse(result.isPresent());
    }
}