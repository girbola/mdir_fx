package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import com.girbola.sql.ThumbInfoSQL;
import com.girbola.thumbinfo.ThumbInfo;
import com.girbola.utils.ThumbInfo_Utils;
import common.utils.FileUtils;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SaveThumbInfos extends Task<List<ThumbInfo>> {

    private Connection connection;
    private Path currentFolderPath;
    private TilePane tilePane;

    public SaveThumbInfos(Connection connection, Path currentFolderPath, TilePane tilePane) {
        this.connection = connection;
        this.currentFolderPath =currentFolderPath;
        this.tilePane = tilePane;
    }

    @Override
    protected List<ThumbInfo> call() throws Exception {
        return saveThumbs();
    }

    public List<ThumbInfo> saveThumbs() {
        if (!Main.conf.isSavingThumb()) {
            Messages.sprintf("isSavingThumb() were turned off");
            return null;
        }

        List<ThumbInfo> thumbInfo_list = new ArrayList<>();
        for (Node n : tilePane.getChildren()) {
            if (n instanceof VBox) {
                for (Node vbox : ((VBox) n).getChildren()) {
                    if (vbox instanceof StackPane) {
                        ImageView iv = (ImageView) vbox.lookup("#imageView");
                        if (iv.getImage() != null) {
                            FileInfo fileInfo = (FileInfo) n.getUserData();

                            if (FileUtils.supportedVideo(Paths.get(fileInfo.getOrgPath()))) {
                                if (iv.getUserData() instanceof List<?>) {
                                    Messages.sprintf("iv.getUserData() was instanceof List<?>");
                                    ThumbInfo thumbInfo = ThumbInfo_Utils.findThumbInfo(fileInfo, thumbInfo_list,
                                            fileInfo.getFileInfo_id());

                                    List<BufferedImage> buffList = (List<BufferedImage>) iv.getUserData();
                                    Messages.sprintf("buffList size is: " + buffList.size());

                                    int counter = 0;
                                    for (BufferedImage bufImage : buffList) {
                                        if (bufImage == null) {
                                            continue;
                                        }

                                        try {
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            ImageIO.write(bufImage, "jpg", baos);
                                            baos.flush();
                                            byte[] imageInByte = baos.toByteArray();
                                            baos.close();
                                            thumbInfo.setThumb_width(bufImage.getWidth());
                                            thumbInfo.setThumb_height(bufImage.getHeight());
                                            thumbInfo.getThumbs().add(counter, imageInByte);
                                        } catch (Exception e) {
                                            Messages.sprintfError("Something went wrong with converting Bufferedimage to byte array: " + e.getMessage());
                                        }
                                    }
                                    thumbInfo_list.add(thumbInfo);
                                }
                            } else {
                                Messages.sprintf("Picture: " + fileInfo.getOrgPath());
                                ThumbInfo thumbInfo = ThumbInfo_Utils.findThumbInfo(fileInfo, thumbInfo_list,
                                        fileInfo.getFileInfo_id());
                                if (!thumbInfo.getThumbs().isEmpty()) {
                                    WritableImage writableImage = iv.snapshot(new SnapshotParameters(), null);
                                    thumbInfo.setThumb_fast_width(writableImage.getWidth());
                                    thumbInfo.setThumb_fast_height(writableImage.getHeight());
                                    ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();

                                    try {
                                        ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png",
                                                byteArrayOS);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    byte[] imageByteArray = byteArrayOS.toByteArray();
                                    try {
                                        byteArrayOS.close();
                                    } catch (IOException ex) {
                                        Logger.getLogger(ModelDatefix.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    thumbInfo.getThumbs().add(imageByteArray);
                                    thumbInfo_list.add(thumbInfo);
                                }
                            }
                        }
                    }
                }
            }
        }
        return thumbInfo_list;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        List<ThumbInfo> thumbInfos = null;
        try {
            thumbInfos = get();
        } catch (InterruptedException | ExecutionException e) {
            Messages.sprintfError("Cannot handle thumbinfos");
            throw new RuntimeException(e);
        }

        Messages.sprintf("Thumbinfo list size is: " + thumbInfos.size());
        connection = SqliteConnection.connector(currentFolderPath, Main.conf.getMdir_db_fileName());
        ThumbInfoSQL.insertThumbInfoListToDatabase(connection, thumbInfos);
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SQL_Utils.closeConnection(connection);
        }
        Messages.sprintf("savingThumb were succeeded");
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        Messages.sprintf("savingThumb were cancelled");
    }

    @Override
    protected void failed() {
        super.failed();
        Messages.sprintf("savingThumb were FAILED");
    }
}
