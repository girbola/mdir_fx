package common.utils.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SimpleDatesTest {

    /**
     * This class tests the functionality of the `getSimpleDateFormatByString` method.
     * The method attempts to parse a given date string using pre-defined `SimpleDateFormat` objects
     * and returns the first matching format that successfully parses the string.
     * If none match, it returns null.
     */

    @Test
    void testValidFormatMatchingYmdHmsSlashDots() throws ParseException {
        // Arrange
        SimpleDates simpleDates = new SimpleDates();
        String validDateString = "2023/09/16 12.30.45";
        SimpleDateFormat expectedFormat = new SimpleDateFormat("yyyy/MM/dd HH.mm.ss");

        // Act
        SimpleDateFormat result = simpleDates.getSimpleDateFormatByString(validDateString);

        // Assert
        assertNotNull(result, "Expected a matching format but got null");
        assertEquals(expectedFormat.toPattern(), result.toPattern());
        assertNotNull(result.parse(validDateString), "Expected the format to successfully parse the date string");
    }

    @Test
    void testValidFormatMatchingYmdHmsMinusDots() throws ParseException {
        // Arrange
        SimpleDates simpleDates = new SimpleDates();
        String validDateString = "2023-09-16 12.30.45";
        SimpleDateFormat expectedFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");

        // Act
        SimpleDateFormat result = simpleDates.getSimpleDateFormatByString(validDateString);

        // Assert
        assertNotNull(result, "Expected a matching format but got null");
        assertEquals(expectedFormat.toPattern(), result.toPattern());
        assertNotNull(result.parse(validDateString), "Expected the format to successfully parse the date string");
    }

    @Test
    void testInvalidDateString() {
        // Arrange
        SimpleDates simpleDates = new SimpleDates();
        String invalidDateString = "Invalid-String";

        // Act
        SimpleDateFormat result = simpleDates.getSimpleDateFormatByString(invalidDateString);

        // Assert
        assertNull(result, "Expected no matching format for an invalid date string");
    }

    @Test
    void testEmptyDateString() {
        // Arrange
        SimpleDates simpleDates = new SimpleDates();
        String emptyDateString = "";

        // Act
        SimpleDateFormat result = simpleDates.getSimpleDateFormatByString(emptyDateString);

        // Assert
        assertNull(result, "Expected no matching format for an empty date string");
    }

    @Test
    void testNullDateString() {
        // Arrange
        SimpleDates simpleDates = new SimpleDates();

        // Act
        SimpleDateFormat result = simpleDates.getSimpleDateFormatByString(null);

        // Assert
        assertNull(result, "Expected no matching format for a null date string");
    }

    @Test
    void testValidFormatMatchingYmdHmsDashColon() throws ParseException {
        // Arrange
        String validDateString = "2023-09-16 12:30:45";
        DateTimeFormatter expectedFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter result = null;
        for (Map.Entry<String, DateTimeFormatter> entry : SimpleDates.dateTimeFormats.entrySet()) {
            try {
                entry.getValue().parse(validDateString);
                System.out.println("Found it!");
                result = entry.getValue();
                break;
            } catch (java.time.format.DateTimeParseException ignored) {
                // keep searching
            }

        }
        // Act

        // Assert
        assertNotNull(result, "Expected a matching format but got null");
        assertEquals(expectedFormat.toString(), result.toString());
        assertNotNull(result.parse(validDateString), "Expected the format to successfully parse the date string");
    }

    @Test
    void testAllDateTimeFormatsRoundTrip() {
        // A deterministic base instant to construct all temporal types
        java.time.ZonedDateTime zdt = java.time.ZonedDateTime.of(
                2023, 9, 16, 12, 30, 45, 123_000_000, java.time.ZoneOffset.UTC
        );
        java.time.OffsetDateTime odt = zdt.toOffsetDateTime();
        java.time.LocalDateTime ldt = zdt.toLocalDateTime();
        java.time.LocalDate ld = zdt.toLocalDate();
        java.time.LocalTime lt = zdt.toLocalTime();

        for (Map.Entry<String, DateTimeFormatter> entry : SimpleDates.dateTimeFormats.entrySet()) {
            String key = entry.getKey();
            DateTimeFormatter fmt = entry.getValue();

            String sample = null;

            // Try formatting with a set of temporals so patterns with offset/zone or date-only/time-only succeed.
            java.time.temporal.Temporal[] temporals = new java.time.temporal.Temporal[] {
                    zdt, odt, ldt, ld, lt
            };

            for (java.time.temporal.Temporal t : temporals) {
                try {
                    sample = fmt.format(t);
                    break;
                } catch (java.time.temporal.UnsupportedTemporalTypeException | IllegalArgumentException ignored) {
                    // Try next temporal
                }
            }
            System.out.println( "Formatter '" + key + "' produced: " + sample);
            assertNotNull(sample, "Formatter '" + key + "' could not format any compatible temporal");

            // Parse back the produced text using the same formatter
            java.time.temporal.TemporalAccessor parsed = fmt.parse(sample);
            assertNotNull(parsed, "Formatter '" + key + "' failed to parse its own output: " + sample);
        }
    }

    @Test
    void testPartiallyValidDateString() {
        // Arrange
        SimpleDates simpleDates = new SimpleDates();
        String partiallyValidDateString = "2023-09-16 INVALID";

        // Act
        SimpleDateFormat result = simpleDates.getSimpleDateFormatByString(partiallyValidDateString);

        // Assert
        assertNull(result, "Expected no matching format for a partially valid date string");
    }
}