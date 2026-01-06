package com.girbola.controllers.main.tables;

        import com.girbola.controllers.main.tables.tabletype.TableType;
        import com.girbola.utils.CommonUserFolders;
        import org.junit.jupiter.api.Test;
        import java.nio.file.Path;
        import java.nio.file.Paths;
        import java.util.Map;

        import static org.junit.jupiter.api.Assertions.*;

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
            void resolveTableTypeByPath_shouldReturnSortedForDatePatternWithHyphens() {
                Path path = Paths.get("2023-10-05");
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
        }