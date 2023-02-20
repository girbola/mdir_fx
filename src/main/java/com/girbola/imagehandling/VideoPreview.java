package com.girbola.imagehandling;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VideoPreview {
	private List<BufferedImage> buff_list;
	private ImageView iv;
	private AtomicInteger index = new AtomicInteger(0);

	public VideoPreview(List<BufferedImage> buff_list, ImageView iv) {
		this.buff_list = buff_list;
		this.iv = iv;
		for (int i = 0; i < buff_list.size(); i++) {
			BufferedImage buff = buff_list.get(i);
		}
	}

	public BufferedImage showNextImage() {
		index.getAndIncrement();
		if (index.get() > (buff_list.size() - 1)) {
			index.set(0);
		}
		BufferedImage buff = buff_list.get(index.get());
		return buff;
	}

	public Image getImage(int index) {
		Image image = SwingFXUtils.toFXImage(buff_list.get(index), null);
		return image;
	}

}
