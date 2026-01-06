package com.girbola.controllers.main.tables;

import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.utils.CommonUserFolders;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TableUtilsTest {

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForPicturesPath() {
        Path picturesPath = Paths.get(System.getProperty("user.home"), "Pictures", "SampleFolder");
        TableType result = TableUtils.resolveTableTypeByPath(picturesPath);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnAsItIsForDownloadsPath() {
        Path downloadsPath = Paths.get(System.getProperty("user.home"), "Downloads", "SampleFolder");
        TableType result = TableUtils.resolveTableTypeByPath(downloadsPath);
        assertEquals(TableType.ASITIS, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForNumericFolderName() {
        Path numericPath = Paths.get("123Canon");
        TableType result = TableUtils.resolveTableTypeByPath(numericPath);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortedForMixedContentFolderName() {
        Path mixedContentPath = Paths.get("O'layreys pub 2013");
        TableType result = TableUtils.resolveTableTypeByPath(mixedContentPath);
        assertEquals(TableType.SORTED, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortedForUnknownFolderName() {
        Path unknownPath = Paths.get("UnknownFolder");
        TableType result = TableUtils.resolveTableTypeByPath(unknownPath);
        assertEquals(TableType.SORTED, result);
    }

    @Test
    void resolveTableTypeByPath_shouldHandleEmptyPathGracefully() {
        Path emptyPath = Paths.get("");
        TableType result = TableUtils.resolveTableTypeByPath(emptyPath);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForCommonUserFolder() {
        Map<CommonUserFolders.Kind, Path> commonFolders = CommonUserFolders.resolve();
        Path picturesPath = commonFolders.get(CommonUserFolders.Kind.PICTURES);
        assertNotNull(picturesPath);
        TableType result = TableUtils.resolveTableTypeByPath(picturesPath);
        assertEquals(TableType.SORTIT, result);
    }


    @Test
    void resolveTableTypeByPath_shouldReturnSortItForDcimPattern() {
        Path path = Paths.get("DCIM");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }


    @Test
    void resolveTableTypeByPath_shouldReturnSortItForLongNumberPattern() {
        Path path = Paths.get("20231005123456");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTED, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortedForDatePatternWithHyphens() {
        Path path = Paths.get("2023-10-05");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTED, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortedForDatePatternWithDots() {
        Path path = Paths.get("2023.10.05");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTED, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortedForDatePatternWithUnderscores() {
        Path path = Paths.get("2023_10_05");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTED, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForFolderNameWithOnlyNumbers() {
        Path path = Paths.get("12345");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortedForFolderNameWithOnlyLettersAndSpaces() {
        Path path = Paths.get("Holiday Pictures");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTED, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForUnmatchedPattern() {
        Path path = Paths.get("!@#SpecialFolder");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForKnownCameraFolderNameDcim() {
        Path path = Paths.get("DCIM");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForKnownCameraFolderNameCanonMsc() {
        Path path = Paths.get("CANONMSC");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForKnownCameraFolderNamePrivate() {
        Path path = Paths.get("PRIVATE");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForKnownCameraFolderNameScreenshots() {
        Path path = Paths.get("Screenshots");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForKnownCameraFolderNameDji() {
        Path path = Paths.get("DJI");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }


    @Test
    void calculateDateDifferenceRatio_shouldReturnZeroForEmptyMap() {
        TreeMap<LocalDate, Integer> map = new TreeMap<>();
        double result = TableUtils.calculateDateDifferenceRatio(map);
        assertEquals(0, result);
    }

    @Test
    void calculateDateDifferenceRatio_shouldReturnZeroForSingleEntryMap() {
        TreeMap<LocalDate, Integer> map = new TreeMap<>();
        map.put(LocalDate.of(2023, 1, 1), 1);
        double result = TableUtils.calculateDateDifferenceRatio(map);
        assertEquals(0, result);
    }

    @Test
    void calculateDateDifferenceRatio_shouldCalculateDifferenceForSequentialDates() {
        TreeMap<LocalDate, Integer> map = new TreeMap<>();
        map.put(LocalDate.of(2023, 1, 1), 1);
        map.put(LocalDate.of(2023, 1, 2), 1);
        map.put(LocalDate.of(2023, 1, 3), 1);
        double result = TableUtils.calculateDateDifferenceRatio(map);
        assertEquals(2, result);
    }

    @Test
    void calculateDateDifferenceRatio_shouldCalculateDifferenceForNonSequentialDates() {
        TreeMap<LocalDate, Integer> map = new TreeMap<>();
        map.put(LocalDate.of(2023, 1, 1), 1);
        map.put(LocalDate.of(2023, 1, 5), 1);
        map.put(LocalDate.of(2023, 1, 10), 1);
        double result = TableUtils.calculateDateDifferenceRatio(map);
        assertEquals(9, result);
    }

    @Test
    void calculateDateDifferenceRatio_shouldHandleDatesWithLargeGaps() {
        TreeMap<LocalDate, Integer> map = new TreeMap<>();
        map.put(LocalDate.of(2020, 1, 1), 1);
        map.put(LocalDate.of(2023, 1, 1), 1);
        double result = TableUtils.calculateDateDifferenceRatio(map);
        assertEquals(1095, result);
    }

    @Test
    void calculateDateDifferenceRatio_shouldHandleDatesWithLargeAmountOfDatesWithGaps() {
        int year1 = 2022;
        int year2 = 2023;
        int year3 = 2024;

        TreeMap<LocalDate, Integer> map = new TreeMap<>();
        map.put(LocalDate.of(year1, 1, 1), 1);
        map.put(LocalDate.of(year1, 1, 1), 1);
        map.put(LocalDate.of(year1, 1, 1), 1);

        map.put(LocalDate.of(year2, 1, 1), 1);
        map.put(LocalDate.of(year2, 1, 1), 1);
        map.put(LocalDate.of(year2, 1, 1), 1);

        map.put(LocalDate.of(year3, 1, 1), 1);
        map.put(LocalDate.of(year3, 1, 1), 1);
        map.put(LocalDate.of(year3, 1, 1), 1);

// Add 100 more LocalDate entries with increasing dates
        for (int i = 0; i < 3; i++) {
            map.put(LocalDate.of(year1, 1, 2).plusDays(i), 1);
        }
        for (int i = 0; i < 3; i++) {
            map.put(LocalDate.of(year2
                    , 1, 2).plusDays(i), 1);
        }

        for (int i = 0; i < 3; i++) {
            map.put(LocalDate.of(year3
                    , 5, 2).plusDays(i), 1);
        }
        double result = TableUtils.calculateDateDifferenceRatio(map);
        assertEquals(362, result);
    }


}