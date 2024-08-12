package com.girbola.imagehandling;

import com.girbola.messages.Messages;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VideoPreview {
    private List<BufferedImage> buff_list;
    private ImageView iv;
    private AtomicInteger index = new AtomicInteger(0);

    public VideoPreview(List<BufferedImage> buff_list, ImageView iv) {
        this.buff_list = buff_list;
        this.iv = iv;
        for (int i = 0; i < buff_list.size(); i++) {
            BufferedImage buff = buff_list.get(i);
            Messages.sprintf("------Buff: " + buff);
        }
    }

    public int getIndex() {
        return index.get();
    }

    public BufferedImage showNextBufferedImage() {
        index.getAndIncrement();
        Messages.sprintf("showNextBufferedImage index: " + index.get());
        if (index.get() > (buff_list.size() - 1)) {
            Messages.sprintf("INNNN is greater that buff_list.sze(): " + index.get());
            index.set(0);
        }
        BufferedImage buff = buff_list.get(index.get());
        Messages.sprintf("buff: " + buff);
        return buff_list.get(index.get());
    }

    public Image showNextImage() {
        index.getAndIncrement();
        if (index.get() > (buff_list.size() - 1)) {
            index.set(0);
        }
        return SwingFXUtils.toFXImage(buff_list.get(index.get()), null);
    }

    public Image getImage(int index) {
        return SwingFXUtils.toFXImage(buff_list.get(index), null);
    }

}
