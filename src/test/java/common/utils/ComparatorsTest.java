package common.utils;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComparatorsTest {

    @Test
    void testCompareInt() {
        List<Path> paths = Arrays.asList(
                Path.of("fileA.txt"),
                Path.of("longerFileName.txt"),
                Path.of("abc.txt")
        );

        List<Path> expected = Arrays.asList(
                Path.of("abc.txt"),
                Path.of("fileA.txt"),
                Path.of("longerFileName.txt")
        );

        Comparators.compareInt(paths);

        paths.forEach(System.out::println);

        assertEquals(expected, paths);
    }

    @Test
    void testSortStringsWithNumbers() {
        List<String> strings = Arrays.asList("a10", "a1", "a2", "a20", "a11");
        List<String> expected = Arrays.asList("a1", "a2", "a10", "a11", "a20");

        Comparators.sortStringsWithNumbers(strings);

        assertEquals(expected, strings);
    }
}