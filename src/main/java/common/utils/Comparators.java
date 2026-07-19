package common.utils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Comparators {

    public static void compareInt(List<Path> paths) {
        // Sorting the list by the length of the string representation of the Path
        Collections.sort(paths, (Path o1, Path o2) -> {
            return Integer.compare(o1.toString().length(), o2.toString().length()); // Return the result of the length comparison
        });
    }

    public static void sortStringsWithNumbers(List<String> strings) {
        // Custom comparator that handles numeric values
        strings.sort(Comparators::compareStrings);
    }

    private static int compareStrings(String s1, String s2) {
        Pattern pattern = Pattern.compile("(\\d+|\\D+)");
        Matcher m1 = pattern.matcher(s1);
        Matcher m2 = pattern.matcher(s2);

        while (m1.find() && m2.find()) {
            String part1 = m1.group();
            String part2 = m2.group();

            // Compare numeric parts as integers
            if (isNumeric(part1) && isNumeric(part2)) {
                int num1 = Integer.parseInt(part1);
                int num2 = Integer.parseInt(part2);
                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            } else {
                // Compare non-numeric parts as strings
                int cmp = part1.compareTo(part2);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }
        return Integer.compare(m1.groupCount(), m2.groupCount()); // Handle different lengths
    }

    private static boolean isNumeric(String str) {
        return str.chars().allMatch(Character::isDigit);
    }

}
