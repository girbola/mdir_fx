package com.girbola.sql;

import com.girbola.Main;
import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.FolderInfoEnum;
import com.girbola.fileinfo.FileInfoEnum;
import com.girbola.persistence.folderinfo.FolderInfoDao;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FolderInfoSchemaMigrationTest {

    @TempDir
    Path tempDir;

    @Test
   public void loadFolderInfoMigratesLegacySchemas() throws Exception {
        Path imagePath = createPlainJpeg(tempDir.resolve("legacy-image.jpg"));
        Path databasePath = tempDir.resolve(Main.conf.getMdir_db_fileName());

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath)) {
            connection.setAutoCommit(false);
            createLegacyFolderInfoTable(connection);
            createLegacyFileInfoTable(connection);
            insertLegacyFolderInfo(connection, tempDir.toString());
            insertLegacyFileInfo(connection, imagePath);
            connection.commit();
        }

        FolderInfo folderInfo = FolderInfoDao.loadFolderInfo(tempDir.toString());

        assertNotNull(folderInfo);
        assertEquals(tempDir.toString(), folderInfo.getFolderPath());
        assertEquals(1, folderInfo.getFileInfoList().size());
        assertEquals(imagePath.toString(), folderInfo.getFileInfoList().getFirst().getOrgPath());

        assertTrue(hasColumn(databasePath, SQLTableEnums.FOLDERINFO.getType(), FolderInfoEnum.WORKDIR_SERIAL_NUMBER.getColumnName()));
        assertTrue(hasColumn(databasePath, SQLTableEnums.FILEINFO.getType(), FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER.getColumnName()));
        assertTrue(hasColumn(databasePath, SQLTableEnums.FILEINFO.getType(), FileInfoEnum.SHA256_CHECKSUM.getColumnName()));
    }

    private static Path createPlainJpeg(Path target) throws Exception {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "jpg", target.toFile());
        return target;
    }

    private static void createLegacyFolderInfoTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE folderinfo ("
                + joinDefinitions(Arrays.stream(FolderInfoEnum.getValuesInBindingOrder())
                .filter(column -> column != FolderInfoEnum.WORKDIR_SERIAL_NUMBER)
                .toList())
                + ")";
        connection.createStatement().execute(sql);
    }

    private static void createLegacyFileInfoTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE fileinfo ("
                + joinDefinitions(Arrays.stream(FileInfoEnum.getValuesInBindingOrder())
                .filter(column -> column != FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER)
                .filter(column -> column != FileInfoEnum.SHA256_CHECKSUM)
                .toList())
                + ")";
        connection.createStatement().execute(sql);
    }

    private static void insertLegacyFolderInfo(Connection connection, String folderPath) throws SQLException {
        List<FolderInfoEnum> columns = Arrays.stream(FolderInfoEnum.getValuesInBindingOrder())
                .filter(column -> column != FolderInfoEnum.WORKDIR_SERIAL_NUMBER)
                .toList();

        String sql = "INSERT INTO folderinfo ("
                + joinColumnNames(columns.stream().map(FolderInfoEnum::getColumnName).toList())
                + ") VALUES ("
                + placeholders(columns.size())
                + ")";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int index = 1;
            for (FolderInfoEnum column : columns) {
                switch (column) {
                    case ID -> pstmt.setInt(index++, 1);
                    case STATUS -> pstmt.setInt(index++, 97);
                    case BAD_FILES -> pstmt.setInt(index++, 0);
                    case CHANGED -> pstmt.setBoolean(index++, true);
                    case CONFIRMED -> pstmt.setInt(index++, 0);
                    case CONNECTED -> pstmt.setBoolean(index++, true);
                    case COPIED -> pstmt.setInt(index++, 0);
                    case DATE_DIFFERENCE -> pstmt.setDouble(index++, 0.0d);
                    case FOLDER_FILES -> pstmt.setInt(index++, 1);
                    case FOLDER_IMAGE_FILES -> pstmt.setInt(index++, 1);
                    case FOLDER_PATH -> pstmt.setString(index++, folderPath);
                    case FOLDER_RAW_FILES -> pstmt.setInt(index++, 0);
                    case FOLDER_SIZE -> pstmt.setLong(index++, 0L);
                    case FOLDER_VIDEO_FILES -> pstmt.setInt(index++, 0);
                    case GOOD_FILES -> pstmt.setInt(index++, 1);
                    case IGNORED -> pstmt.setBoolean(index++, false);
                    case JUST_FOLDER_NAME -> pstmt.setString(index++, Path.of(folderPath).getFileName().toString());
                    case MAX_DATE -> pstmt.setString(index++, "2026-01-01 00.00.00");
                    case MIN_DATE -> pstmt.setString(index++, "2026-01-01 00.00.00");
                    case STATE -> pstmt.setString(index++, "");
                    case SUGGESTED -> pstmt.setInt(index++, 0);
                    case TABLE_TYPE -> pstmt.setString(index++, "SortIt");
                    case WORKDIR_SERIAL_NUMBER -> throw new IllegalStateException("Legacy folderinfo insert should not include workdirSerialNumber");
                }
            }
            pstmt.executeUpdate();
        }
    }

    private static void insertLegacyFileInfo(Connection connection, Path imagePath) throws Exception {
        List<FileInfoEnum> columns = Arrays.stream(FileInfoEnum.getValuesInBindingOrder())
                .filter(column -> column != FileInfoEnum.WORK_DIR_DRIVE_SERIAL_NUMBER)
                .filter(column -> column != FileInfoEnum.SHA256_CHECKSUM)
                .toList();

        String sql = "INSERT INTO fileinfo ("
                + joinColumnNames(columns.stream().map(FileInfoEnum::getColumnName).toList())
                + ") VALUES ("
                + placeholders(columns.size())
                + ")";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int index = 1;
            for (FileInfoEnum column : columns) {
                switch (column) {
                    case FILEINFO_ID -> pstmt.setInt(index++, 1);
                    case BAD -> pstmt.setBoolean(index++, false);
                    case CAMERA_MODEL -> pstmt.setString(index++, "legacy-camera");
                    case CONFIRMED -> pstmt.setBoolean(index++, false);
                    case DESTINATION_PATH -> pstmt.setString(index++, "");
                    case DATE -> pstmt.setLong(index++, System.currentTimeMillis());
                    case EVENT -> pstmt.setString(index++, "");
                    case FILEHISTORIES -> pstmt.setString(index++, "legacy import");
                    case GOOD -> pstmt.setBoolean(index++, true);
                    case COPIED -> pstmt.setBoolean(index++, false);
                    case GPS_COORDINATES -> pstmt.setString(index++, "");
                    case CUSTOM_GPS_COORDINATES -> pstmt.setBoolean(index++, false);
                    case IGNORED -> pstmt.setBoolean(index++, false);
                    case IMAGE -> pstmt.setBoolean(index++, true);
                    case IMAGE_DIFFERENCE_HASH -> pstmt.setString(index++, "");
                    case LOCATION -> pstmt.setString(index++, "");
                    case MODIFIED -> pstmt.setBoolean(index++, false);
                    case ORGPATH -> pstmt.setString(index++, imagePath.toString());
                    case ORGPATH_DRIVE_SERIAL_NUMBER -> pstmt.setString(index++, "");
                    case ORIENTATION -> pstmt.setInt(index++, 0);
                    case RAW -> pstmt.setBoolean(index++, false);
                    case SIZE -> pstmt.setLong(index++, Files.size(imagePath));
                    case SUGGESTED -> pstmt.setBoolean(index++, false);
                    case TABLE_DUPLICATED -> pstmt.setBoolean(index++, false);
                    case TAGS -> pstmt.setString(index++, "");
                    case THUMB_LENGTH -> pstmt.setInt(index++, 0);
                    case THUMB_OFFSET -> pstmt.setInt(index++, 0);
                    case TIME_SHIFT -> pstmt.setLong(index++, 0L);
                    case USER -> pstmt.setString(index++, "");
                    case VIDEO -> pstmt.setBoolean(index++, false);
                    case WORK_DIR -> pstmt.setString(index++, "");
                    case WORK_DIR_DRIVE_SERIAL_NUMBER, SHA256_CHECKSUM -> throw new IllegalStateException("Legacy fileinfo insert should omit migrated columns");
                }
            }
            pstmt.executeUpdate();
        }
    }

    private static boolean hasColumn(Path databasePath, String tableName, String columnName) throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
             ResultSet rs = connection.createStatement().executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }

    private static String joinDefinitions(List<? extends Enum<?>> columns) {
        return columns.stream()
                .map(column -> {
                    if (column instanceof FolderInfoEnum folderInfoEnum) {
                        return "'" + folderInfoEnum.getColumnName() + "' " + folderInfoEnum.getSqlType();
                    }
                    FileInfoEnum fileInfoEnum = (FileInfoEnum) column;
                    return fileInfoEnum.getColumnDefinition();
                })
                .collect(Collectors.joining(", "));
    }

    private static String joinColumnNames(List<String> columnNames) {
        return columnNames.stream()
                .map(columnName -> "'" + columnName + "'")
                .collect(Collectors.joining(", "));
    }

    private static String placeholders(int count) {
        return String.join(", ", java.util.Collections.nCopies(count, "?"));
    }
}

