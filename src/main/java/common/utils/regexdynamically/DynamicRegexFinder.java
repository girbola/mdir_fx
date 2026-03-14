package common.utils.regexdynamically;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DynamicRegexFinder {

    public static Map<String, List<Path>> groupFilesByFilenamePattern(Path currentPath) {
        if (currentPath == null) {
            throw new IllegalArgumentException("currentPath cannot be null");
        }

        List<Path> files = new ArrayList<>();

        try (Stream<Path> stream = Files.list(currentPath)) {
            List<Path> collectedFiles = stream
                    .filter(Files::isRegularFile)
                    .toList();
            files.addAll(collectedFiles);
        } catch (IOException e) {
            return Map.of();
        }

        Map<String, List<Path>> matchedFiles = new LinkedHashMap<>();
        Set<Path> visited = new HashSet<>();

        for (Path file : files) {
            if (visited.contains(file)) {
                continue;
            }

            String seedName = file.getFileName().toString();
            Pattern pattern = Pattern.compile(createDynamicRegexFromFilenames(Set.of(seedName)));

            List<Path> group = new ArrayList<>();
            for (Path candidate : files) {
                if (!visited.contains(candidate)
                        && pattern.matcher(candidate.getFileName().toString()).matches()) {
                    group.add(candidate);
                }
            }

            group.sort(Path::compareTo);
            matchedFiles.put(seedName, group);
            visited.addAll(group);
        }

        return matchedFiles;
    }

    public static String createDynamicRegexFromFilenames(Set<String> filenames) {
        if (filenames == null || filenames.isEmpty()) {
            return ".*";
        }

        String firstFilename = filenames.iterator().next();
        StringBuilder regex = new StringBuilder(firstFilename.length() * 4);

        int i = 0;
        while (i < firstFilename.length()) {
            char currentChar = firstFilename.charAt(i);
            CharType currentType = getCharType(currentChar);
            int count = 1;

            while (i + count < firstFilename.length()
                    && getCharType(firstFilename.charAt(i + count)) == currentType) {
                count++;
            }

            switch (currentType) {
                case DIGIT:
                    regex.append("\\d{").append(count).append("}");
                    break;
                case LETTER:
                    regex.append("[A-Za-z]{").append(count).append("}");
                    break;
                case SPECIAL:
                    for (int j = 0; j < count; j++) {
                        regex.append(Pattern.quote(String.valueOf(firstFilename.charAt(i + j))));
                    }
                    break;
                default:
                    break;
            }

            i += count;
        }

        return regex.toString();
    }

    private static CharType getCharType(char c) {
        if (Character.isDigit(c)) {
            return CharType.DIGIT;
        } else if (Character.isLetter(c)) {
            return CharType.LETTER;
        } else {
            return CharType.SPECIAL;
        }
    }

    private enum CharType {
        DIGIT, LETTER, SPECIAL
    }
}