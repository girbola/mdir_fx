package common.utils;
// Java
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SmartDateExtractor {

    // Order matters: more specific patterns first
    private static final Pattern DT_SEPARATED =
            Pattern.compile("(\\d{4})[-_. ]?(\\d{2})[-_. ]?(\\d{2})[ T_]?+(\\d{2})[:._-]?(\\d{2})[:._-]?(\\d{2})");
    private static final Pattern DT_COMPACT_14 = Pattern.compile("\\b\\d{14}\\b"); // yyyyMMddHHmmss

    private static final Pattern D_SEPARATED =
            Pattern.compile("(\\d{4})[-_. ]?(\\d{2})[-_. ]?(\\d{2})");
    private static final Pattern D_COMPACT_8 = Pattern.compile("\\b\\d{8}\\b"); // yyyyMMdd

    // Strict formatters
    private static final DateTimeFormatter F_YMD_HMS =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("uuuu-MM-dd HH:mm:ss")
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter F_YMD =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("uuuu-MM-dd")
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter F_YMD_HMS_COMPACT =
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss").withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter F_YMD_COMPACT =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

    private SmartDateExtractor() {}

    public static Optional<Instant> extractInstant(String text) {
        return extractInstant(text, ZoneId.systemDefault());
    }

    public static Optional<Instant> extractInstant(String text, ZoneId zone) {
        // 1) yyyy[-_. ]MM[-_. ]dd[ T_]?HH[:._-]?mm[:._-]?ss
        Matcher m1 = DT_SEPARATED.matcher(text);
        if (m1.find()) {
            String normalized = normYmdHms(
                    m1.group(1), m1.group(2), m1.group(3),
                    m1.group(4), m1.group(5), m1.group(6)
            );
            try {
                LocalDateTime ldt = LocalDateTime.parse(normalized, F_YMD_HMS);
                return Optional.of(ldt.atZone(zone).toInstant());
            } catch (Exception ignored) {}
        }

        // 2) yyyyMMddHHmmss (14 digits)
        Matcher m2 = DT_COMPACT_14.matcher(text);
        if (m2.find()) {
            String s = m2.group();
            try {
                LocalDateTime ldt = LocalDateTime.parse(s, F_YMD_HMS_COMPACT);
                return Optional.of(ldt.atZone(zone).toInstant());
            } catch (Exception ignored) {}
        }

        // 3) yyyy[-_. ]MM[-_. ]dd (date only -> start of day)
        Matcher m3 = D_SEPARATED.matcher(text);
        if (m3.find()) {
            String normalized = normYmd(m3.group(1), m3.group(2), m3.group(3));
            try {
                LocalDate ld = LocalDate.parse(normalized, F_YMD);
                return Optional.of(ld.atStartOfDay(zone).toInstant());
            } catch (Exception ignored) {}
        }

        // 4) yyyyMMdd (8 digits -> start of day)
        Matcher m4 = D_COMPACT_8.matcher(text);
        if (m4.find()) {
            String s = m4.group();
            try {
                LocalDate ld = LocalDate.parse(s, F_YMD_COMPACT);
                return Optional.of(ld.atStartOfDay(zone).toInstant());
            } catch (Exception ignored) {}
        }

        return Optional.empty();
    }

//    public static OptionalLong extractEpochMillis(String text) {
//        return extractInstant(text).map(i -> OptionalLong.of(i.toEpochMilli())).orElseGet(OptionalLong::empty);
//    }

    private static String normYmdHms(String y, String m, String d, String hh, String mm, String ss) {
        return pad4(y) + "-" + pad2(m) + "-" + pad2(d) + " " + pad2(hh) + ":" + pad2(mm) + ":" + pad2(ss);
    }

    private static String normYmd(String y, String m, String d) {
        return pad4(y) + "-" + pad2(m) + "-" + pad2(d);
    }

    private static String pad2(String s) {
        return (s.length() == 2) ? s : ("0" + s).substring(("0" + s).length() - 2);
    }

    private static String pad4(String s) {
        return ("0000" + s).substring(("0000" + s).length() - 4);
    }
}