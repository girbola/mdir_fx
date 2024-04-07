package com.girbola.videothumbnailing;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.util.List;

public class SingleVideoThumbnail extends Task<List<BufferedImage>> {

    private FileInfo fileInfo;
    private ImageView imageView;
    private double width;

    public SingleVideoThumbnail(FileInfo fileInfo, ImageView imageView, double width) {
        this.fileInfo = fileInfo;
        this.imageView = imageView;
        this.width = width;
    }

    @Override
    protected List<BufferedImage> call() throws Exception {
        Messages.sprintf("VideoThumnbnailator.getVideoThumbnails TASK!");
        return JCodecVideoThumbUtils.getList(fileInfo.getOrgPath());
    }
}
