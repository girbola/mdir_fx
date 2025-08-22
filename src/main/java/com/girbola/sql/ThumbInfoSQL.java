package com.girbola.sql;

import com.girbola.controllers.main.SQLTableEnums;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.thumbinfo.ThumbInfo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// implements SQLInterface
public class ThumbInfoSQL  {

    final private static String ERROR = ThumbInfoSQL.class.getSimpleName();

    final static String thumbInfoInsert =
            "INSERT OR REPLACE INTO " +
                    SQLTableEnums.THUMBINFO.getType() +
                    " ('id',"+
                    "'filepath', " +
                    "'thumb_width', " +
                    "'thumb_height', " +
                    "'thumb_fast_width', " +
                    "'thumb_fast_height', " +
                    "'orientation', " +
                    "'image_0', " +
                    "'image_1', " +
                    "'image_2', " +
                    "'image_3', " +
                    "'image_4') VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

    private static boolean addToThumbInfoDB(Connection connection, PreparedStatement pstmt, ThumbInfo thumbInfo) {
        try {
            pstmt.setInt(1, thumbInfo.getId());
            pstmt.setString(2, thumbInfo.getFileName());
            pstmt.setDouble(3, thumbInfo.getThumb_width());
            pstmt.setDouble(4, thumbInfo.getThumb_height());
            pstmt.setDouble(5, thumbInfo.getThumb_fast_width());
            pstmt.setDouble(6, thumbInfo.getThumb_fast_height());
            pstmt.setDouble(7, thumbInfo.getOrientation());

            final int tsize = thumbInfo.getThumbs().size();
            int pstmtCounter = 8;
            for (int i = 0; i < (tsize); i++) {
                pstmt.setBytes((i + 8), thumbInfo.getThumbs().get(i));
                pstmtCounter++;
            }
            for (int i = pstmtCounter; i < (13); i++) { // TODO FIX THIS 13 number!!!
                pstmt.setBytes((i), null);
            }

            pstmt.addBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                pstmt.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return false;
        }
    }
    // @formatter:off
    public static boolean createThumbInfoTable(Connection connection) {
        boolean dbConnected = SQL_Utils.isDbConnected(connection);
        if (!dbConnected) {
            Messages.sprintf("Not connected to database!");
            return false;
        }
        try {
            Statement stmt = connection.createStatement();
            connection.setAutoCommit(false);
            String sql = "CREATE TABLE IF NOT EXISTS "
                    + SQLTableEnums.THUMBINFO.getType()
                    + " (id INTEGER PRIMARY KEY,"
                    + " filepath STRING UNIQUE NOT NULL,"
                    + " thumb_width  DOUBLE,"
                    + " thumb_height DOUBLE,"
                    + " thumb_fast_width  DOUBLE,"
                    + " thumb_fast_height DOUBLE,"
                    + " orientation INTEGER,"
                    + " image_0 BLOB NULL,"
                    + " image_1  BLOB NULL,"
                    + " image_2 BLOB NULL,"
                    + " image_3  BLOB NULL,"
                    + " image_4  BLOB NULL)";

            stmt.execute(sql);
            connection.commit();

            stmt.close();
            return true;
        } catch (Exception ex) {
            Messages.sprintfError("Not working: " + ex.getMessage());
            return false;
        }
    }

    public static boolean insertThumbInfoListToDatabase(Connection connection, List<ThumbInfo> thumbInfoList) {
        boolean thumbInfoCreated = createThumbInfoTable(connection);

        if (!thumbInfoCreated) {
            Messages.sprintf("insertThumbInfoListToDatabase NOT connected");
            return false;
        }

        try {
            connection.setAutoCommit(false);
            PreparedStatement pstmt = null;
            pstmt = connection.prepareStatement(thumbInfoInsert);
            for (ThumbInfo thumbInfo : thumbInfoList) {
                addToThumbInfoDB(connection, pstmt, thumbInfo);
            }
            pstmt.executeBatch();
            pstmt.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static List<ThumbInfo> loadThumbInfo_list(Connection connection) {
        if (connection == null) {
            return null;
        }
        List<ThumbInfo> list = new ArrayList<>();
        if (!SQL_Utils.isDbConnected(connection)) {
            return null;
        }
        try {
            String sql = "SELECT * FROM " + SQLTableEnums.THUMBINFO.getType();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Messages.sprintf("stmt starting: " + sql);
                ThumbInfo thumbInfo = thumbInfoCreation(rs);
                list.add(thumbInfo);
            }
        } catch (Exception e) {
            return null;
        }
        return list;
    }

    // @formatter:off
    public static ThumbInfo loadThumbInfo(Connection connection, int thumbInfo_ID) {
        Messages.sprintf("Loading thumbinfo SQL id= " + thumbInfo_ID);
        if (connection == null) {
            Messages.errorSmth(ERROR, "Connection was unable to establish", null, Misc.getLineNumber(), true);
            return null;
        }
        if (!SQL_Utils.isDbConnected(connection)) {
            Messages.sprintfError("No connection for thumbinfo ID: " + thumbInfo_ID);
            return null;
        }
        ThumbInfo thumbInfo = null;
        try {
            String sql = "SELECT * FROM " + SQLTableEnums.THUMBINFO.getType() + " WHERE id = ?";
//			String sql = "SELECT id, " + "filename, " + "thumb_width, " + "thumb_height, " + "thumb_fast_width, "
//					+ "thumb_fast_height, " + "orientation, " + "image_0, " + "image_1, " + "image_2, " + "image_3, "
//					+ "image_4 FROM " + SQLTableEnums.THUMBINFO.getType() + " WHERE id = ?";
            // @formatter:on
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, thumbInfo_ID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String filePath = rs.getString("filepath");
                double thumb_width = rs.getDouble("thumb_width");
                double thumb_height = rs.getDouble("thumb_height");
                double thumb_fast_width = rs.getDouble("thumb_fast_width");
                double thumb_fast_height = rs.getDouble("thumb_fast_height");
                int orientation = rs.getInt("orientation");
                byte[] image_0 = rs.getBytes("image_0");
                byte[] image_1 = rs.getBytes("image_1");
                byte[] image_2 = rs.getBytes("image_2");
                byte[] image_3 = rs.getBytes("image_3");
                byte[] image_4 = rs.getBytes("image_4");
                Messages.sprintf("ID WERE: " + id);
                thumbInfo = new ThumbInfo(id, filePath, thumb_width, thumb_height, thumb_fast_width, thumb_fast_height, orientation, new ArrayList<>(Arrays.asList(image_0, image_1, image_2, image_3, image_4)));
//				thumbInfo = thumbInfoCreation(rs);
                return thumbInfo;
            }
        } catch (Exception e) {
//			if (Main.DEBUG) {
//				e.printStackTrace();
//			}

            return null;
        }
        return thumbInfo;
    }

    // @formatter:on
//    public static boolean insertThumbInfo(Connection connection, int id, ThumbInfo thumbInfo) {
//        createThumbInfoTable(connection);
//        if (!isDbConnected(connection)) {
//            Messages.sprintf("Connection is not connected");
//            return false;
//        }
//
//        try {
//            connection.setAutoCommit(false);
//            PreparedStatement pstmt = connection.prepareStatement(thumbInfoInsert);
//            addToThumbInfoDB(connection, pstmt, thumbInfo);
//            pstmt.executeBatch();
//            connection.commit();
//            pstmt.close();
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    private static ThumbInfo thumbInfoCreation(ResultSet rs) throws SQLException {
        String fileName = rs.getString("filename");
        int id = rs.getInt("id");
        double thumb_width = rs.getDouble("thumb_width");
        double thumb_height = rs.getDouble("thumb_height");
        double thumb_fast_width = rs.getDouble("thumb_fast_width");
        double thumb_fast_height = rs.getDouble("thumb_fast_height");
        double orientation = rs.getDouble("orientation");
        byte[] byte0 = rs.getBytes("image_0");
        byte[] byte1 = rs.getBytes("image_1");
        byte[] byte2 = rs.getBytes("image_2");
        byte[] byte3 = rs.getBytes("image_3");
        byte[] byte4 = rs.getBytes("image_4");
        ArrayList<byte[]> byteList = new ArrayList<>(Arrays.asList(byte0, byte1, byte2, byte3, byte4));

        ThumbInfo thumbInfo = new ThumbInfo(id, fileName, thumb_width, thumb_height, thumb_fast_width, thumb_fast_height, orientation, byteList);
        return thumbInfo;
    }


}
