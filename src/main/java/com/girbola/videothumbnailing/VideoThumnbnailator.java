package com.girbola.videothumbnailing;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class VideoThumnbnailator {

    private final FileInfo fileInfo;
    private final ImageView imageView;
    private final double width;

    public VideoThumnbnailator(FileInfo fileInfo, ImageView imageView, double width) {
        this.fileInfo = fileInfo;
        this.imageView = imageView;
        this.width = width;
    }

    public static Task<List<BufferedImage>> getVideoThumbnails(FileInfo fileInfo, ImageView imageView, double width) {

//        Task<List<BufferedImage>> task2 = null;
//        Task<List<BufferedImage>> task = new Task<>() {
//
//        };
//
//        task2 = new Task<>() {
//
//            @Override
//            protected List<BufferedImage> call() throws Exception {
//                Messages.sprintf("VideoThumnbnailator.getVideoThumbnails TASK222222!");
//                List<BufferedImage> list = null;
//                try {
//                    list = get();
//                    if(list == null) {
//                        Messages.sprintf("Bufferereporgpaoerg:  + ");
//                        cancelled();
//                    }
//                    Messages.sprintf("List buffered image: " + list.size());
//                    for(BufferedImage bufferedImage : list) {
//                        Messages.sprintf("=============BufferedImage: " + bufferedImage.getWidth());
//                    }
//                } catch (Exception e) {
//                    super.cancel();
//                    System.err.println("buffered failed: " + e);
//                }
//                return null;
//            }
//        };
//
//        Task<List<BufferedImage>> finalTask = task2;
//        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//            @Override
//            public void handle(WorkerStateEvent workerStateEvent) {
//                try {
//                    List<BufferedImage> bufferedImageList = task.get();
//                    Messages.sprintf("buffered image: " + bufferedImageList.size());
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                } catch (ExecutionException e) {
//                    throw new RuntimeException(e);
//                }
//                Thread th = new Thread(finalTask, "TASKI");
//                th.start();
//            }
//        });
//
//        task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
//            @Override
//            public void handle(WorkerStateEvent workerStateEvent) {
//
//                Messages.sprintf("CAncelleeeeed");
//
//            }
//        });
//
//
//        Thread thMain = new Thread(task, "Main tasks");
//        thMain.start();
//        return null;
        return null;

    }

}
