package com.girbola.controllers.datefixer;

import com.girbola.fileinfo.FileInfo;
import com.girbola.imagehandling.JCodecVideoThumb;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

public class HandleVideoFile extends Task<List<BufferedImage>>{

    private Path file;
    private FileInfo fileInfo;
    private ImageView imageView;
    private double thumbWidth;

    public HandleVideoFile(Path file, FileInfo fileInfo, ImageView imageView, double thumbWidth) {
        this.file = file;
        this.fileInfo = fileInfo;
        this.imageView = imageView;
        this.thumbWidth = thumbWidth;
    }

    @Override
    protected List<BufferedImage> call() throws Exception {
        List<BufferedImage> convertVideo_task = JCodecVideoThumb.findThumbnails(file);
        if (convertVideo_task != null) {
            return convertVideo_task;
        }

        System.out.println("HandleVideoFile were null");
        return null;
    }
}
