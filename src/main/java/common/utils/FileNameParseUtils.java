
package common.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.date.DateUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.sprintf;
import static common.utils.date.SimpleDates.dateFormats;
import static common.utils.date.SimpleDates.dateTimeFormats;

/**
 *
 * @author Marko
 */
public class FileNameParseUtils {

    private final static String ERROR = FileNameParseUtils.class.getSimpleName();

    // private final static String DATE_WITH_DELIM =
    // "\\d{4}\\W\\d{2}\\W\\d{2}\\s\\d{2}\\W\\d{2}\\W\\d{2}";
    // 2004
    /*
     * . Any character (may or may not match line terminators) \d A digit: [0-9] \D
     * A non-digit: [^0-9] \s A whitespace character: [ \t\n\x0B\f\r] \S A
     * non-whitespace character: [^\s] \w A word character: [a-zA-Z_0-9] \W A
     * non-word character: [^\w]
     */
    // 2004
//	erG;
    private final static String DATE_WITH_SEPARATOR = "\\d{4}\\W\\d{2}\\W\\d{2}[\\w|\\s]{1}\\d{2}\\W\\d{2}\\W\\d{2}"; // 2000-12-13
    // 12.13.14
    // &
    // 2000-12-13_12.13.14
    private final static String DATE_WITH_YMDHMS = "\\d{4}\\d{2}\\d{2}\\d{2}\\d{2}\\d{2}"; // 20001213121314
    // private final static String DATE_WITH_YMD_HMS =
    // "\\d{4}\\d{2}\\d{2}\\d{2}\\d{2}\\d{2}"; //20001213121314

    private final static String DATE_WITH_YMD_HMS = "\\d{4}\\d{2}\\d{2}[\\w|\\s]{1}\\d{2}\\d{2}\\d{2}"; // 20001213
    // 121314
    private final static String DATE_WITH_YMD = "\\d{4}\\d{2}\\d{2}[\\w|\\s]"; // 20001213 121314
    private static final List<String> regex_list = Arrays.asList(DATE_WITH_SEPARATOR, DATE_WITH_YMDHMS,
            DATE_WITH_YMD_HMS);

    private static final Pattern MULTI_DATE_CANDIDATE_PATTERN = Pattern.compile(
            "(?i)(\\d{4}[-./]\\d{2}[-./]\\d{2}(?:\\s+at\\s+|[ T_])\\d{2}[:.-]\\d{2}(?:[:.-]\\d{2})?(?:[.,]\\d{1,3})?(?:\\s?[APMapm]{2})?(?:Z|[+-]\\d{2}:?\\d{2})?"
                    + "|\\d{2}[-./]\\d{2}[-./]\\d{4}(?:[ T_])\\d{2}[:.-]\\d{2}(?:[:.-]\\d{2})?(?:\\s?[APMapm]{2})?"
                    + "|\\d{8}[ _-]\\d{6}(?:[ _-]\\d{3})?"
                    + "|\\d{14}(?:\\d{3})?"
                    + "|\\d{4}[-./]\\d{2}[-./]\\d{2}"
                    + "|\\d{2}[-./]\\d{2}[-./]\\d{4}"
                    + "|\\d{8})");

    private static final List<DateTimeFormatter> STRICT_DATE_TIME_FORMATTERS = Arrays.asList(
            strictFormatter("uuuu-MM-dd HH:mm:ss"),
            strictFormatter("uuuu-MM-dd HH:mm"),
            strictFormatter("uuuu-MM-dd HH.mm.ss"),
            strictFormatter("uuuu-MM-dd HH-mm-ss"),
            strictFormatter("uuuu/MM/dd HH:mm:ss"),
            strictFormatter("uuuu/MM/dd HH:mm"),
            strictFormatter("uuuu/MM/dd HH.mm.ss"),
            strictFormatter("uuuu/MM/dd HH-mm-ss"),
            strictFormatter("uuuu.MM.dd HH:mm:ss"),
            strictFormatter("uuuu.MM.dd HH:mm"),
            strictFormatter("uuuu.MM.dd HH.mm.ss"),
            strictFormatter("uuuu.MM.dd HH-mm-ss"),
            strictFormatter("uuuu-MM-dd'T'HH:mm:ss"),
            strictFormatter("uuuu-MM-dd'T'HH:mm:ss.SSS"),
            strictFormatter("uuuu-MM-dd 'at' HH.mm.ss"),
            strictFormatter("uuuu-MM-dd 'at' HH-mm-ss"),
            strictFormatter("dd.MM.uuuu HH:mm:ss"),
            strictFormatter("dd.MM.uuuu HH.mm.ss"),
            strictFormatter("dd-MM-uuuu HH:mm:ss"),
            strictFormatter("dd/MM/uuuu HH:mm:ss"),
            strictFormatter("MM-dd-uuuu HH:mm:ss"),
            strictFormatter("MM/dd/uuuu HH:mm:ss"),
            strictFormatter("MM-dd-uuuu hh:mm:ss a"),
            strictFormatter("MM/dd/uuuu hh:mm:ss a"),
            strictFormatter("uuuuMMddHHmmss"),
            strictFormatter("uuuuMMddHHmmssSSS"),
            strictFormatter("uuuuMMdd HHmmss"),
            strictFormatter("uuuuMMdd_HHmmss"),
            strictFormatter("uuuuMMdd-HHmmss"),
            strictFormatter("uuuuMMdd_HHmmss_SSS"));

    private static final List<DateTimeFormatter> STRICT_OFFSET_DATE_TIME_FORMATTERS = Arrays.asList(
            strictFormatter("uuuu-MM-dd'T'HH:mm:ssXXX"),
            strictFormatter("uuuu-MM-dd'T'HH:mm:ss.SSSXXX"),
            strictFormatter("uuuu-MM-dd HH:mm:ssXXX"),
            strictFormatter("uuuu-MM-dd HH:mmXXX"));

    private static final List<DateTimeFormatter> STRICT_DATE_FORMATTERS = Arrays.asList(
            strictFormatter("uuuu-MM-dd"),
            strictFormatter("uuuu/MM/dd"),
            strictFormatter("uuuu.MM.dd"),
            strictFormatter("uuuuMMdd"),
            strictFormatter("dd-MM-uuuu"),
            strictFormatter("dd/MM/uuuu"),
            strictFormatter("dd.MM.uuuu"),
            strictFormatter("MM-dd-uuuu"),
            strictFormatter("MM/dd/uuuu"),
            strictFormatter("MM.dd.uuuu"));

    /* Filename parse utils START */
    /**
     * Split file name & path & extension
     *
     */
    public static String[] imageFileNamePrefix = {"CIMG", "DSC_", "DSCF", "DSCN", "DSC_", "IMG_", "MVI_", "SAM_"};

    /**
     * Parse known imagename running number IMG_1234 return 1234
     *
     * @param str
     * @return Known imagenane running number IMG_1234 return 1234
     */
    @Deprecated
    public static Integer getFileNameRunningNumber(String str) {
        int value = 0;

        String number = "";
        for (int i = 0; i < str.length(); i++) {
            Character character = str.charAt(i);
            if (Character.isDigit(character)) {
                number += character;
            }
        }
        if (number.length() > 1) {
            value = Integer.parseInt(number);
        }
        return value;
    }

    /**
     * Check if known image name format
     *
     * @param path
     * @return knownPrefix
     */
    @Deprecated
    public static String knownImageName(Path path) {
        // 20070909_DCS0197.jpg
        // 20070909_IMG_0198.jpg

        for (String str : imageFileNamePrefix) {
            if (path.getFileName().toString().toLowerCase().contains(str.toLowerCase())) {
                return str;
            }
        }
        return null;
    }

    /**
     * parse Beginning Of fileName
     *
     * @param path
     * @return Begin of fileName stripping from delims till end
     */
    @Deprecated
    public static String parseBOFfileName(Path path) {
        String delims = knownImageName(path);
        if (delims.length() > 1) {
            return path.getFileName().toString().substring(0, path.getFileName().toString().indexOf(delims));
        }
        // 1233456_IMG_800.jpg to 123456
        return null;
    }

    /**
     * parse bold filename 2013 09 09 IMG_1234.jpg to known image name format
     * IMG_1234.jpg
     *
     * @param path
     * @return foobarIMG_1234.jpg into IMG_1234.jpg
     */
    @Deprecated
    public static String parseEOFfilename(Path path) {
        String delims = knownImageName(path);
        if (delims.length() != 0) {
            if (delims.length() > 1) {
                return path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf(delims));
            }
        }
        return null;
    }

    /**
     * Parse file name extension example: C:/Temp/IMG_1234.jpg will be IMG_1234
     *
     * @param path
     * @return
     */
    public static String parseFileExtentension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    public static long hasFileNameDate(Path path) {
        return parseEpochMillisFromText(parseFileExtentension(path));
    }

    protected static long tryFileNameDateKnownFormat(Path path) {

        for (String regex : regex_list) {
            String result = "";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(parseFileExtentension(path));

            if (m.find()) {
                result = m.group();
                // sprintf("match found; " + result);
                SimpleDateFormat simpleDateFormat = simpleDates.getSimpleDateFormatByString(result);
                if (simpleDateFormat != null) {
                    sprintf("simpleDateFormat: " + simpleDateFormat.toPattern());
                }
            }

            if (!result.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                result = m.group(); // 2016-10-29 14:31:44
                for (char s : result.toCharArray()) {
                    if (s >= '0' && s <= '9') {
                        sb.append(s);
                    } else {
                        sb.append("");
                    }
                }
                // sprintf("SB RESULT: " + sb);
                // String d = hasDate(sb
                if (!sb.toString().isEmpty()) {
                    long date = 0;
                    try {
                        date = Conversion.stringDateToLong(sb.toString(), simpleDates.getSdf_ymd_hms_nospaces());
                        return date;
                    } catch (Exception ex) {
                        sprintf("Exception d = hasDate(sb); " + result + " exception is: " + ex.getMessage());
                        return 0;
                    }
                }
            }
            result = null;

        }
        return 0;
    }

    public static long findDateFromFilename(Path path, String dateDelim) {
        String result = "";
        Pattern p = Pattern.compile(dateDelim);
        Matcher m = p.matcher(parseFileExtentension(path));

        if (m.find()) {
            result = m.group();
        }

        if (!result.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            result = m.group(); // 2016-10-29 14:31:44
            for (char s : result.toCharArray()) {
                if (s >= '0' && s <= '9') {
                    sb.append(s);
                } else {
                    sb.append(" ");
                }
            }
            sprintf("SB RESULT: " + sb);
            // String d = hasDate(sb);
            if (!sb.toString().isEmpty()) {
                long date = 0;
                try {
                    date = Conversion.stringDateToLong(sb.toString(), simpleDates.getSdf_ymd_hms_spaces());
                    // sprintf("stringDateToLong is: " + date);
                    return date;
                } catch (Exception ex) {
                    sprintf("Exception d = hasDate(sb); " + result + " exception is: " + ex.getMessage());
                    return 0;
                }
            }
        }
        return 0;
    }

    public static long tryFileNameDate(Path path, String dateDelim) {
        Messages.sprintf("tryFileNameDate: " + path);
        String result = "";
        Pattern p = Pattern.compile(dateDelim);
        Matcher m = p.matcher(parseFileExtentension(path));

        if (m.find()) {
            result = m.group();
            Messages.sprintf("Result of trying find filename from date: " + result);
        }

        if (!result.isEmpty()) {
            return parseEpochMillisFromText(result);
        }
        return 0;
    }

    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    public static long tryParseDateTimeAsLong(FileInfo fileInfo) {
        Path filename = Paths.get(fileInfo.getOrgPath());
        long epoch = parseEpochMillisFromText(parseFileExtentension(filename));
        if (epoch > 0L) {
            fileInfo.setDate(epoch);
        }
        return epoch;
    }

    private static DateTimeFormatter getDateTimeFromString(Map<String, DateTimeFormatter> dateFormats, String datePart) {
        for (java.time.format.DateTimeFormatter fmt : dateFormats.values()) {
            try {
                // date-only parsing branch
                java.time.LocalDate.parse(datePart, fmt);
                return fmt;
            } catch (java.time.format.DateTimeParseException ignored) {

            }
        }
        return null;
    }

    private static String extractDatePart(String filename) {
        // Capture anything that looks like a date or datetime
        String date = filename.replaceAll(".*?(\\d{4}[-/]\\d{2}[-/]\\d{2}([ T_]\\d{2}[-:]\\d{2}[-:]\\d{2}( [APMapm]{2})?)?|\\d{8}(_\\d{6})?|\\d{14}).*", "$1");
        if (date != null && !date.isBlank() && !date.equals(filename)) {
            return date.replaceAll("[_T]", " ").replaceAll("[.:]", "-").trim();
        }
        return null;
    }
// extractDateFromFileName

    public static String extractDateFromFileName(String fileName) {
        for (String candidate : extractDateCandidates(fileName)) {
            if (isSupportedDateOrDateTime(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static long parseEpochMillisFromText(String text) {
        if (text == null || text.isBlank()) {
            return 0L;
        }
        for (String candidate : extractDateCandidates(text)) {
            long parsed = parseCandidateToEpochMillis(candidate);
            if (parsed > 0L) {
                return parsed;
            }
        }
        return 0L;
    }

    private static List<String> extractDateCandidates(String text) {
        Set<String> candidates = new LinkedHashSet<>();
        Matcher matcher = MULTI_DATE_CANDIDATE_PATTERN.matcher(text);
        while (matcher.find()) {
            String candidate = normalizeDateCandidate(matcher.group());
            if (!candidate.isBlank()) {
                candidates.add(candidate);
            }
        }

        String fallback = extractDatePart(text);
        if (fallback != null && !fallback.isBlank()) {
            candidates.add(normalizeDateCandidate(fallback));
        }
        return new ArrayList<>(candidates);
    }

    private static long parseCandidateToEpochMillis(String candidate) {
        for (DateTimeFormatter fmt : STRICT_OFFSET_DATE_TIME_FORMATTERS) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(candidate, fmt);
                return odt.toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
            }
        }
        for (DateTimeFormatter fmt : STRICT_DATE_TIME_FORMATTERS) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(candidate, fmt);
                return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
            }
        }
        for (DateTimeFormatter fmt : STRICT_DATE_FORMATTERS) {
            try {
                LocalDate ld = LocalDate.parse(candidate, fmt);
                return ld.atTime(12, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
            }
        }
        return 0L;
    }

    private static boolean isSupportedDateOrDateTime(String candidate) {
        return parseCandidateToEpochMillis(candidate) > 0L;
    }

    private static DateTimeFormatter strictFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT);
    }

    private static String normalizeDateCandidate(String value) {
        return value.replaceAll("(?i)\\s+at\\s+", " ").replaceAll("\\s+", " ").trim();
    }

    private static DateTimeFormatter getDateTimeFormatterIfFound(Map<String, DateTimeFormatter> dateTimeFormats, String text, FileInfo fileInfo) {
        for (DateTimeFormatter fmt : dateTimeFormats.values()) {
            try {
                LocalDateTime parsedDate = LocalDateTime.parse(text, fmt);
//                DateUtils.parseLocalDateTimeToEpochMillis(parsedDate, fmt);
                long epochMillis = DateUtils.parseLocalDateTimeToEpochMillis(parsedDate.format(fmt), fmt);
                fileInfo.setDate(epochMillis);
                return fmt;
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static DateTimeFormatter getDateFormatterIfFound(Map<String, DateTimeFormatter> dateFormats, String text) {
        for (DateTimeFormatter fmt : dateFormats.values()) {
            try {
                LocalDate.parse(text, fmt);
                Messages.sprintf("Date detected: " + text + " fmt:::: " + fmt.toString());
                return fmt;
            } catch (DateTimeParseException ignored) {
            }
        }
        Messages.sprintf("Date not detected: " + text);
        return null;
    }


    private static boolean isDateTime(String text) {
        for (DateTimeFormatter fmt : dateTimeFormats.values()) {
            try {
                LocalDateTime.parse(text, fmt);
                return true;
            } catch (DateTimeParseException ignored) {
            }
        }
        return false;
    }

    private static boolean isDate(String text) {
        for (DateTimeFormatter fmt : dateFormats.values()) {
            try {
                LocalDate.parse(text, fmt);
                Messages.sprintf("Date detected: " + text);
                return true;
            } catch (DateTimeParseException ignored) {
            }
        }
        return false;
    }

}
