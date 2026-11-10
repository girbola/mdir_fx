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
        Path picturesPath = CommonUserFolders.resolve().get(CommonUserFolders.Kind.PICTURES).resolve("SampleFolder");
        TableType result = TableUtils.resolveTableTypeByPath(picturesPath);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnAsItIsForDownloadsPath() {
        Path downloadsPath = CommonUserFolders.resolve().get(CommonUserFolders.Kind.DOWNLOADS).resolve("SampleFolder");
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
    void resolveTableTypeByPath_shouldReturnSortItForReissuPattern() {
        Path path = Paths.get("Ruotsin Reissu");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTED, result);
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
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForDatePatternWithHyphens() {
        Path path = Paths.get("2023-10-05");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForDatePatternWithDots() {
        Path path = Paths.get("2023.10.05");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldReturnSortItForDatePatternWithUnderscores() {
        Path path = Paths.get("2023_10_05");
        TableType result = TableUtils.resolveTableTypeByPath(path);
        assertEquals(TableType.SORTIT, result);
    }

    @Test
    void resolveTableTypeByPath_shouldClassifyCameraAndDateNamesAsSortIt() {
        for (String folder : new String[] {
                "123Canon", "100NIKON", "100_1234", "dcim", "20231005",
                "2024-02-29", "2024_02_29", "2024.02.29",
                "20240229", "2023-02-29 2024-02-29"
        }) {
            assertEquals(TableType.SORTIT, TableUtils.resolveTableTypeByPath(Paths.get(folder)), folder);
        }
    }

    @Test
    void resolveTableTypeByPath_shouldClassifyDescriptiveNamesAsSorted() {
        for (String folder : new String[] {
                "Mazda CX-3", "O'layreys pub 2013", "Helsinki 2024",
                "Kes\u00e4loma (perhe)", "2024 Holiday", "Trip 2023-02-29",
                "Trip 2023-10_05", "Photos from 202310051",
                "Trip 2024-02-29", "Trip_2024_02_29", "Trip 2024.02.29",
                "Trip 20240229", "2024-02-29 Trip", "Trip 2023-02-29 2024-02-29",
                "!Trip 2024-02-29"
        }) {
            assertEquals(TableType.SORTED, TableUtils.resolveTableTypeByPath(Paths.get(folder)), folder);
        }
    }

    @Test
    void resolveTableTypeByPath_shouldKeepUnrecognizedNamesInSortIt() {
        for (String folder : new String[] {"!@#SpecialFolder", "...", "12345"}) {
            assertEquals(TableType.SORTIT, TableUtils.resolveTableTypeByPath(Paths.get(folder)), folder);
        }
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
    void calculateDateDifferenceRatio_shouldCalculateDifferenceForNonSequentialDates2() {
        TreeMap<LocalDate, Integer> map = new TreeMap<>();
        map.put(LocalDate.of(2023, 1, 1), 1);
        map.put(LocalDate.of(2023, 1, 5), 1);
        map.put(LocalDate.of(2023, 1, 7), 1);
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
        assertEquals(1096, result);
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
            LocalDate localDate = LocalDate.of(year1, 1, 2).plusDays(i);
            System.out.println("1 - LocalDate: " + localDate);
            map.put(localDate, 1);
        }

        for (int i = 0; i < 3; i++) {
            LocalDate localDate = LocalDate.of(year2, 1, 2).plusDays(i);
            System.out.println("2 - LocalDate: " + localDate);
            map.put(localDate, 1);
        }

        for (int i = 0; i < 3; i++) {
            LocalDate localDate = LocalDate.of(year3, 1, 2).plusDays(i);
            System.out.println("3 - LocalDate: " + localDate);
            map.put(localDate, 1);
        }
        map.keySet().forEach(System.out::println);

        double result = TableUtils.calculateDateDifferenceRatio(map);
        assertEquals(733, result);
    }

    @Test
    void calculateDateDifferenceRatio_shouldHandleDatesWithLargeAmountOfDatesWithGapsLeapYearIncluded() {
        int year1 = 2020;
        int year2 = 2021;
        int year3 = 2022;

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
            LocalDate localDate = LocalDate.of(year1, 1, 2).plusDays(i);
            System.out.println("1 - LocalDate: " + localDate);
            map.put(localDate, 1);
        }

        for (int i = 0; i < 3; i++) {
            LocalDate localDate = LocalDate.of(year2, 1, 2).plusDays(i);
            System.out.println("2 - LocalDate: " + localDate);
            map.put(localDate, 1);
        }

        for (int i = 0; i < 3; i++) {
            LocalDate localDate = LocalDate.of(year3, 1, 2).plusDays(i);
            System.out.println("3 - LocalDate: " + localDate);
            map.put(localDate, 1);
        }
        map.keySet().forEach(System.out::println);

        double result = TableUtils.calculateDateDifferenceRatio(map);
        assertEquals(734, result);
    }


}
