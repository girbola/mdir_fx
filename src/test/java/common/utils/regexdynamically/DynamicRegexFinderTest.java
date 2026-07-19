package common.utils.regexdynamically;

import com.twelvemonkeys.imageio.metadata.Entry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicRegexFinderTest {

    @Test
    void testCreateDynamicRegexFromFilename() {
        Set<String> filenames = new HashSet<>();
        filenames.add("file123.txt");

        String dynamicRegex = DynamicRegexFinder.createDynamicRegexFromFilenames(filenames);

        assertEquals("\\w{4}\\d{3}\\Q.\\E\\w{3}", dynamicRegex);
    }

    @Test
    void testCreateDynamicRegexFromFilenames() {
        Set<String> filenames = new HashSet<>();
        filenames.add("file123.txt");
        filenames.add("file123_aamu-1234.txt");

        String dynamicRegex = DynamicRegexFinder.createDynamicRegexFromFilenames(filenames);

        assertEquals("\\w{4}\\d{3}\\Q.\\E\\w{3}", dynamicRegex);
    }

    @Test
    void testCreateDynamicRegexFromFilenamesWithEmptySet() {
        Set<String> filenames = new HashSet<>();

        String dynamicRegex = DynamicRegexFinder.createDynamicRegexFromFilenames(filenames);

        assertEquals(".*", dynamicRegex);
    }

    @Test
    void testGroupFilesByFilenamePatternWithValidFiles(@TempDir Path testDirectory) throws IOException {
        Path file1 = Files.createFile(testDirectory.resolve("file123.txt"));
        Path file2 = Files.createFile(testDirectory.resolve("file124.txt"));
        Path file3 = Files.createFile(testDirectory.resolve("document1.doc"));

        Map<String, List<Path>> groupedFiles = DynamicRegexFinder.groupFilesByFilenamePattern(testDirectory);

        assertEquals(2, groupedFiles.size());
        assertEquals(List.of(file1, file2), groupedFiles.get("file123.txt"));
        assertEquals(List.of(file3), groupedFiles.get("document1.doc"));
    }

    @Test
    void testGroupFilesByFilenamePatternWithEmptyDirectory(@TempDir Path testDirectory) {
        Map<String, List<Path>> groupedFiles = DynamicRegexFinder.groupFilesByFilenamePattern(testDirectory);

        assertEquals(0, groupedFiles.size());
    }

    @Test
    void testGroupFilesByFilenamePatternHandlesIOException() {
        Path testDirectory = Path.of("Z:/path/that/should/not/exist/for/test");

        assertDoesNotThrow(() -> {
            Map<String, List<Path>> groupedFiles = DynamicRegexFinder.groupFilesByFilenamePattern(testDirectory);
            assertEquals(0, groupedFiles.size());
        });
    }

    @Test
    void testGroupFilesByFilenamePattern() throws IOException {
        Path testDirectory = Path.of("E:\\Maija\\Maijan-vaadin-2\\iCloud Photos-11");
        Map<String, List<Path>> groupedFiles = DynamicRegexFinder.groupFilesByFilenamePattern(testDirectory);

        if(groupedFiles.isEmpty()) {
            System.err.println("No files found in the directory");
        }

        List<Path> expectedFiles = Files.list(testDirectory)
                .filter(Files::isRegularFile)
                .toList();

        List<Path> actualFiles = groupedFiles.values().stream()
                .flatMap(List::stream)
                .toList();

        assertEquals(expectedFiles.size(), actualFiles.size());
        assertEquals(Set.copyOf(expectedFiles), Set.copyOf(actualFiles));

        for(Map.Entry<String, List<Path>> entry : groupedFiles.entrySet()) {
            String key = entry.getKey();
            System.out.println("KEY:::: " + key);
            List<Path> value = entry.getValue();

            for(Path p : value) {
                System.out.println("-----VALUE:::: " + p);
            }
        }
//        assertEquals(2, groupedFiles.size());
//        assertEquals(List.of(file1, file2), groupedFiles.get("file123.txt"));
//        assertEquals(List.of(file3), groupedFiles.get("document1.doc"));
    }
}