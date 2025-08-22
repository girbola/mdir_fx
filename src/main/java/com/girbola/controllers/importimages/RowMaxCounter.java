package com.girbola.controllers.importimages;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.ui.UI_Tools;
import javafx.scene.control.ScrollPane;

import java.util.ArrayList;
import java.util.List;

public class RowMaxCounter {

	private Model_importImages model_importImages;
	private List<FileInfo> entryList;
	private ScrollPane scrollPane;
	List<FileInfo> current_fileInfo = new ArrayList<>();
	private double gap = 0;

	public double getGap() {
		return gap;
	}

	public void setGap(double gap) {
		this.gap = gap;
	}

	public List<FileInfo> getCurrent_fileInfo() {
		return current_fileInfo;
	}

	public void setCurrent_fileInfo(List<FileInfo> current_fileInfo) {
		this.current_fileInfo = current_fileInfo;
	}

	public RowMaxCounter(Model_importImages model_importImages, List<FileInfo> images_list, ScrollPane scrollPane) {
		super();
		this.model_importImages = model_importImages;
		this.entryList = images_list;
		this.scrollPane = scrollPane;
		int imagesPerLine = 0;

		/*
		 * imagerPerLine = ((1366 - 40) / 100) = (int) Math.floor(13) gap = (1366-40 =
		 * 1326) - (100 * 13 = 1300) = 26 26 / (imagesPerLine - 2)
		 */
		double scrollBar_width = UI_Tools.getScrollBarWidth(scrollPane);
		double screenWidth = Misc.getScreenBounds().getWidth();
		double width = model_importImages.getGUIUtils().getWidth();
		Messages.sprintf("scrp: " + scrollBar_width + " screen_w: " + screenWidth + " width: " + width);
		imagesPerLine = (int) Math.floor((Misc.getScreenBounds().getWidth() - UI_Tools.getScrollBarWidth(scrollPane)) / model_importImages.getGUIUtils().getWidth());
		Messages.sprintf("possibleImages: " + imagesPerLine);
		double possibleGap = ((Misc.getScreenBounds().getWidth() - UI_Tools.getScrollBarWidth(scrollPane))) -
				((double) model_importImages.getGUIUtils().getWidth() * (double) imagesPerLine);

		Messages.sprintf("possibleGap: " + possibleGap + " CHECK: " + ((double) model_importImages.getGUIUtils().getWidth()));
		if (possibleGap < 1) {
			imagesPerLine--;
			possibleGap = (Misc.getScreenBounds().getWidth() - UI_Tools.getScrollBarWidth(scrollPane) * (imagesPerLine));
			Messages.sprintf("Possible gap were < 1 and it was changed to: " + possibleGap);
			setGap(possibleGap);
		}
		if (imagesPerLine < images_list.size()) {
			setCurrent_fileInfo(images_list);
			Messages.sprintf("imagesPerLine < images_list.size() imagesPerLine " + imagesPerLine + " images_list.size() " + images_list.size());
		} else {
			int divider = imagesPerLine;
			Messages.sprintf("divider: " + divider + " entryList size: " + images_list.size());
			current_fileInfo.add(images_list.get(0));
			for (int i = divider; i < (images_list.size() - divider); i++) {
				if (i % divider == 0) {
					current_fileInfo.add(images_list.get(i));
					Messages.sprintf("ADDING TO DIVIDER: " + i);
				}
			}
		}
	}
}
