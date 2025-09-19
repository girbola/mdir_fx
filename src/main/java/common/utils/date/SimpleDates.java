package common.utils.date;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import lombok.Getter;

/* imports omitted for shortness */
@Getter
public class SimpleDates {
    // Registry for commonly used formatters
    public static Map<String, DateTimeFormatter> dateTimeFormats = new LinkedHashMap<>();
    public static Map<String, DateTimeFormatter> dateFormats = new LinkedHashMap<>();

    // ---------------------------
    // Pattern constants (Date + Time)
    // ---------------------------
    private static final String YMD_HMS_NOSPACES = "yyyyMMddHHmmss";            // 20250915134512
    private static final String YMD_HMS_SPACES = "yyyy MM dd HH mm ss";         // 2025 09 15 13 45 12
    private static final String YMD_HMS_SPACE = "yyyyMMdd HHmmss";              // 20250915 134512
    private static final String YMD_HMS_SLASHDOTS = "yyyy/MM/dd HH.mm.ss";      // 2025/09/15 13.45.12
    private static final String YMD_HMS_MINUSDOTS_DEFAULT = "yyyy-MM-dd HH.mm.ss"; // 2025-09-15 13.45.12
    private static final String YMD_HMS_SLASHCOLON = "yyyy/MM/dd HH:mm:ss";     // 2025/09/15 13:45:12
    private static final String YMD_HMS_MINUSCOLON = "yyyy-MM-dd HH:mm:ss";     // 2025-09-15 13:45:12
    private static final String YMD_SLASH = "yyyy/MM/dd";                       // 2025/09/15
    private static final String YMD_MINUS = "yyyy-MM-dd";                       // 2025-09-15
    private static final String YMD_H_MINUS = "yyyy-MM-dd HH";                  // 2025-09-15 13
    private static final String YMD_HM_MINUS = "yyyy-MM-dd HH.mm";              // 2025-09-15 13.45
    private static final String YMD_HMS_MINUS = "yyyy-MM-dd HH.mm.ss";          // 2025-09-15 13.45.12
    private static final String YMD_H_SLASH = "yyyy/MM/dd HH";                  // 2025/09/15 13
    private static final String HMS_COLON = "HH:mm:ss";                         // 13:45:12
    private static final String HMS_DOTS = "HH.mm.ss";                          // 13.45.12

    // ISO/US/Compact families
    private static final String ISO_DATETIME_T = "yyyy-MM-dd'T'HH:mm:ss";       // 2025-09-15T13:45:12
    private static final String ISO_DATETIME_SPACE = "yyyy-MM-dd HH:mm:ss";     // 2025-09-15 13:45:12
    private static final String US_DATETIME = "MM-dd-yyyy hh:mm:ss a";          // 09-15-2025 01:45:12 PM
    private static final String US_DATETIME_24H = "MM-dd-yyyy HH:mm:ss";        // 09-15-2025 13:45:12
    private static final String COMPACT_DATETIME = "yyyyMMdd_HHmmss";           // 20250915_134512
    private static final String COMPACT_DATETIME_24H = "yyyyMMddHHmmss";        // 20250915134512
    private static final String ISO_DATE = "yyyy-MM-dd";                         // 2025-09-15
    private static final String US_DATE = "MM-dd-yyyy";                          // 09-15-2025
    private static final String COMPACT_DATE = "yyyyMMdd";                       // 20250915

    // Additional filename-friendly formats
    private static final String ISO_DATETIME_T_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS";           // 2025-09-15T13:45:12.123
    private static final String ISO_DATETIME_T_OFFSET = "yyyy-MM-dd'T'HH:mm:ssXXX";            // 2025-09-15T13:45:12+02:00
    private static final String ISO_DATETIME_T_MILLIS_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"; // 2025-09-15T13:45:12.123+02:00
    private static final String FILENAME_DASHED = "yyyy-MM-dd_HH-mm-ss";                       // 2025-09-15_13-45-12
    private static final String FILENAME_DASHED_NO_UNDERSCORE = "yyyy-MM-dd HH-mm-ss";         // 2025-09-15 13-45-12
    private static final String COMPACT_MILLIS = "yyyyMMddHHmmssSSS";                          // 20250915134512123
    private static final String COMPACT_UNDERSCORE_MILLIS = "yyyyMMdd_HHmmss_SSS";             // 20250915_134512_123
    private static final String DATE_DOTS = "yyyy.MM.dd";                                      // 2025.09.15
    private static final String DMY_DASH = "dd-MM-yyyy";                                       // 15-09-2025
    private static final String DMY_SLASH = "dd/MM/yyyy";                                      // 15/09/2025
    private static final String DMY_DOT = "dd.MM.yyyy";                                        // 15.09.2025
    private static final String DATE_AT_TIME_DOTS = "yyyy-MM-dd 'at' HH.mm.ss";                // 2025-09-15 at 13.45.12
    private static final String DATE_AT_TIME_DASH = "yyyy-MM-dd 'at' HH-mm-ss";                // 2025-09-15 at 13-45-12
    private static final String COMPACT_WITH_DASH = "yyyyMMdd-HHmmss";                         // 20250915-134512

    // Scandinavian/DMY datetime variants (not previously registered)
    private static final String DMY_DOT_COLON = "dd.MM.yyyy HH:mm:ss";                         // 15.09.2025 13:45:12
    private static final String DMY_DOT_DOT = "dd.MM.yyyy HH.mm.ss";                           // 15.09.2025 13.45.12
    private static final String DMY_DASH_COLON = "dd-MM-yyyy HH:mm:ss";                        // 15-09-2025 13:45:12
    private static final String DMY_SLASH_COLON = "dd/MM/yyyy HH:mm:ss";                       // 15/09/2025 13:45:12

    // ---------------------------
    // DateTimeFormatter instances
    // ---------------------------
    private final DateTimeFormatter dtf_ymd_minus = DateTimeFormatter.ofPattern(YMD_MINUS);
    private final DateTimeFormatter dtf_hms_dots = DateTimeFormatter.ofPattern(HMS_DOTS);
    private final DateTimeFormatter dtf_ymd_slash = DateTimeFormatter.ofPattern(YMD_SLASH);
    private final DateTimeFormatter dtf_ymd_hms_minusDots_default = DateTimeFormatter.ofPattern(YMD_HMS_MINUSDOTS_DEFAULT);

    private final DateTimeFormatter dtf_iso_date = DateTimeFormatter.ofPattern(ISO_DATE);
    private final DateTimeFormatter dtf_us_date = DateTimeFormatter.ofPattern(US_DATE);
    private final DateTimeFormatter dtf_compact_date = DateTimeFormatter.ofPattern(COMPACT_DATE);

    private final DateTimeFormatter dtf_iso_datetime_t = DateTimeFormatter.ofPattern(ISO_DATETIME_T);
    private final DateTimeFormatter dtf_iso_datetime_space = DateTimeFormatter.ofPattern(ISO_DATETIME_SPACE);
    private final DateTimeFormatter dtf_us_datetime = DateTimeFormatter.ofPattern(US_DATETIME);
    private final DateTimeFormatter dtf_us_datetime_24h = DateTimeFormatter.ofPattern(US_DATETIME_24H);
    private final DateTimeFormatter dtf_compact_datetime = DateTimeFormatter.ofPattern(COMPACT_DATETIME);
    private final DateTimeFormatter dtf_compact_datetime_24h = DateTimeFormatter.ofPattern(COMPACT_DATETIME_24H);

    // Extended DateTime formatters
    private final DateTimeFormatter dtf_iso_datetime_t_millis = DateTimeFormatter.ofPattern(ISO_DATETIME_T_MILLIS);
    private final DateTimeFormatter dtf_iso_datetime_t_offset = DateTimeFormatter.ofPattern(ISO_DATETIME_T_OFFSET);
    private final DateTimeFormatter dtf_iso_datetime_t_millis_offset = DateTimeFormatter.ofPattern(ISO_DATETIME_T_MILLIS_OFFSET);
    private final DateTimeFormatter dtf_filename_dashed = DateTimeFormatter.ofPattern(FILENAME_DASHED);
    private final DateTimeFormatter dtf_filename_dashed_no_underscore = DateTimeFormatter.ofPattern(FILENAME_DASHED_NO_UNDERSCORE);
    private final DateTimeFormatter dtf_compact_millis = DateTimeFormatter.ofPattern(COMPACT_MILLIS);
    private final DateTimeFormatter dtf_compact_underscore_millis = DateTimeFormatter.ofPattern(COMPACT_UNDERSCORE_MILLIS);
    private final DateTimeFormatter dtf_date_dots = DateTimeFormatter.ofPattern(DATE_DOTS);
    private final DateTimeFormatter dtf_dmy_dash = DateTimeFormatter.ofPattern(DMY_DASH);
    private final DateTimeFormatter dtf_dmy_slash = DateTimeFormatter.ofPattern(DMY_SLASH);
    private final DateTimeFormatter dtf_dmy_dot = DateTimeFormatter.ofPattern(DMY_DOT);
    private final DateTimeFormatter dtf_date_at_time_dots = DateTimeFormatter.ofPattern(DATE_AT_TIME_DOTS);
    private final DateTimeFormatter dtf_date_at_time_dash = DateTimeFormatter.ofPattern(DATE_AT_TIME_DASH);
    private final DateTimeFormatter dtf_compact_with_dash = DateTimeFormatter.ofPattern(COMPACT_WITH_DASH);

    // Hour granularity formatters
    private final DateTimeFormatter dtf_ymd_h_minus = DateTimeFormatter.ofPattern(YMD_H_MINUS);
    private final DateTimeFormatter dtf_ymd_hm_minus = DateTimeFormatter.ofPattern(YMD_HM_MINUS);
    private final DateTimeFormatter dtf_ymd_hms_minus = DateTimeFormatter.ofPattern(YMD_HMS_MINUS);

    private DateTimeFormatter dtf_ymd_hms_nospaces = DateTimeFormatter.ofPattern(YMD_HMS_NOSPACES);

    // ---------------------------
    // Legacy SimpleDateFormat instances (kept for existing usages)
    // ---------------------------
    private final SimpleDateFormat sdf_ymd_hms_minusColon = new SimpleDateFormat(YMD_HMS_MINUSCOLON);
    private final SimpleDateFormat sdf_ymd_hms_minusDots_default = new SimpleDateFormat(YMD_HMS_MINUSDOTS_DEFAULT);
    private final SimpleDateFormat sdf_ymd_hms_nospaces = new SimpleDateFormat(YMD_HMS_NOSPACES);
    private final SimpleDateFormat sdf_ymd_hms_slashColon = new SimpleDateFormat(YMD_HMS_SLASHCOLON);
    private final SimpleDateFormat sdf_ymd_hms_slashDots = new SimpleDateFormat(YMD_HMS_SLASHDOTS);
    private SimpleDateFormat sdf_ymd_hms_space = new SimpleDateFormat(YMD_HMS_SPACE);
    private final SimpleDateFormat sdf_ymd_hms_spaces = new SimpleDateFormat(YMD_HMS_SPACES);
    private final SimpleDateFormat sdf_ymd_minus = new SimpleDateFormat(YMD_MINUS);
    private final SimpleDateFormat sdf_ymd_slash = new SimpleDateFormat(YMD_SLASH);
    private final SimpleDateFormat sdf_ymd_h_slash = new SimpleDateFormat(YMD_H_SLASH);
    private final SimpleDateFormat sdf_hms_colon = new SimpleDateFormat(HMS_COLON);
    private final SimpleDateFormat sdf_hms_dots = new SimpleDateFormat(HMS_DOTS);
    private final SimpleDateFormat sdf_Year = new SimpleDateFormat("yyyy");
    private final SimpleDateFormat sdf_Month = new SimpleDateFormat("MM");
    private final SimpleDateFormat sdf_Day = new SimpleDateFormat("dd");
    private final SimpleDateFormat sdf_Hour = new SimpleDateFormat("HH");
    private final SimpleDateFormat sdf_Min = new SimpleDateFormat("mm");
    private final SimpleDateFormat sdf_Sec = new SimpleDateFormat("ss");

    private final List<SimpleDateFormat> sdf_list = Arrays.asList(sdf_ymd_hms_minusDots_default, sdf_ymd_hms_slashDots);

    // ---------------------------
    // Static registry init
    // ---------------------------
    static {
        // Fill common registries in a predictable order
        // Fill common registries in a predictable order
        dateTimeFormats.put("ISO_DATETIME_T", DateTimeFormatter.ofPattern(ISO_DATETIME_T));                 // e.g., 2025-09-15T13:45:12
        dateTimeFormats.put("ISO_DATETIME_SPACE", DateTimeFormatter.ofPattern(ISO_DATETIME_SPACE));         // e.g., 2025-09-15 13:45:12
        dateTimeFormats.put("US_DATETIME", DateTimeFormatter.ofPattern(US_DATETIME));                       // e.g., 09-15-2025 01:45:12 PM
        dateTimeFormats.put("US_DATETIME_24H", DateTimeFormatter.ofPattern(US_DATETIME_24H));               // e.g., 09-15-2025 13:45:12
        dateTimeFormats.put("COMPACT_DATETIME", DateTimeFormatter.ofPattern(COMPACT_DATETIME));             // e.g., 20250915_134512
        dateTimeFormats.put("COMPACT_DATETIME_24H", DateTimeFormatter.ofPattern(COMPACT_DATETIME_24H));     // e.g., 20250915134512
        dateTimeFormats.put("ISO_DATETIME_T_MILLIS", DateTimeFormatter.ofPattern(ISO_DATETIME_T_MILLIS));   // e.g., 2025-09-15T13:45:12.123
        dateTimeFormats.put("ISO_DATETIME_T_OFFSET", DateTimeFormatter.ofPattern(ISO_DATETIME_T_OFFSET));   // e.g., 2025-09-15T13:45:12+02:00
        dateTimeFormats.put("ISO_DATETIME_T_MILLIS_OFFSET", DateTimeFormatter.ofPattern(ISO_DATETIME_T_MILLIS_OFFSET)); // e.g., 2025-09-15T13:45:12.123+02:00
        // Also register additional filename-friendly and regional patterns
        dateTimeFormats.put("YMD_HMS_MINUS", DateTimeFormatter.ofPattern(YMD_HMS_MINUS));                   // e.g., 2025-09-15 13.45.12
        dateTimeFormats.put("YMD_HMS_SLASHCOLON", DateTimeFormatter.ofPattern(YMD_HMS_SLASHCOLON));         // e.g., 2025/09/15 13:45:12
        dateTimeFormats.put("YMD_HMS_SLASHDOTS", DateTimeFormatter.ofPattern(YMD_HMS_SLASHDOTS));           // e.g., 2025/09/15 13.45.12
        dateTimeFormats.put("FILENAME_DASHED", DateTimeFormatter.ofPattern(FILENAME_DASHED));               // e.g., 2025-09-15_13-45-12
        dateTimeFormats.put("FILENAME_DASHED_NO_UNDERSCORE", DateTimeFormatter.ofPattern(FILENAME_DASHED_NO_UNDERSCORE)); // e.g., 2025-09-15 13-45-12
        dateTimeFormats.put("COMPACT_MILLIS", DateTimeFormatter.ofPattern(COMPACT_MILLIS));                 // e.g., 20250915134512123
        dateTimeFormats.put("COMPACT_UNDERSCORE_MILLIS", DateTimeFormatter.ofPattern(COMPACT_UNDERSCORE_MILLIS)); // e.g., 20250915_134512_123
        dateTimeFormats.put("COMPACT_WITH_DASH", DateTimeFormatter.ofPattern(COMPACT_WITH_DASH));           // e.g., 20250915-134512
        dateTimeFormats.put("DATE_AT_TIME_DOTS", DateTimeFormatter.ofPattern(DATE_AT_TIME_DOTS));           // e.g., 2025-09-15 at 13.45.12
        dateTimeFormats.put("DATE_AT_TIME_DASH", DateTimeFormatter.ofPattern(DATE_AT_TIME_DASH));           // e.g., 2025-09-15 at 13-45-12
        // Scandinavian/DMY datetime registrations
        dateTimeFormats.put("DMY_DOT_COLON", DateTimeFormatter.ofPattern(DMY_DOT_COLON));                   // e.g., 15.09.2025 13:45:12
        dateTimeFormats.put("DMY_DOT_DOT", DateTimeFormatter.ofPattern(DMY_DOT_DOT));                       // e.g., 15.09.2025 13:45:12
        dateTimeFormats.put("DMY_DASH_COLON", DateTimeFormatter.ofPattern(DMY_DASH_COLON));                 // e.g., 15-09-2025 13:45:12
        dateTimeFormats.put("DMY_SLASH_COLON", DateTimeFormatter.ofPattern(DMY_SLASH_COLON));               // e.g., 15/09/2025 13:45:12

        dateFormats.put("ISO_DATE", DateTimeFormatter.ofPattern(ISO_DATE));                                 // e.g., 2025-09-15
        dateFormats.put("US_DATE", DateTimeFormatter.ofPattern(US_DATE));                                   // e.g., 09-15-2025
        dateFormats.put("COMPACT_DATE", DateTimeFormatter.ofPattern(COMPACT_DATE));                         // e.g., 20250915
        dateFormats.put("DATE_DOTS", DateTimeFormatter.ofPattern(DATE_DOTS));                               // e.g., 2025.09.15
        dateFormats.put("DMY_DASH", DateTimeFormatter.ofPattern(DMY_DASH));                                 // e.g., 15-09-2025
        dateFormats.put("DMY_SLASH", DateTimeFormatter.ofPattern(DMY_SLASH));                               // e.g., 15/09/2025
        dateFormats.put("DMY_DOT", DateTimeFormatter.ofPattern(DMY_DOT));                                   // e.g., 15.09.2025
    }

    // ---------------------------
    // TimeZone defaults for legacy SDFs
    // ---------------------------
    {
        sdf_ymd_hms_minusColon.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdf_ymd_hms_minusDots_default.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdf_ymd_hms_slashColon.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdf_ymd_hms_slashDots.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdf_ymd_hms_spaces.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdf_ymd_hms_nospaces.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdf_ymd_minus.setTimeZone(TimeZone.getTimeZone("UTC"));
        // ... existing code ...
    }

    // ---------------------------
    // Fixed: return only when parsing succeeds
    // ---------------------------
    public SimpleDateFormat getSimpleDateFormatByString(String value) {
        for (SimpleDateFormat sdf : sdf_list) {
            try {
                Date date = sdf.parse(value);
                if (date != null) {
                    return sdf;
                }
            } catch (Exception ignore) {
                // try next formatter
            }
        }
        return null;
    }
}
