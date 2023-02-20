/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import static com.girbola.Main.simpleDates;
import static com.girbola.controllers.main.tables.TableUtils.calculateDateDifferenceRatio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

/**
 *
 * @author Marko Lokka
 */
public final class UpdateFolderInfoContent_NT {

	private final String ERROR = UpdateFolderInfoContent_NT.class.getSimpleName();

	public final FolderInfo getFolderInfo() {
		return folderInfo;
	}

	private FolderInfo folderInfo;

	public UpdateFolderInfoContent_NT(FolderInfo folderInfo) {
		this.folderInfo = folderInfo;
	}

	private int bad = 0;
	private int copied = 0;
	private int good = 0;
	private int image = 0;
	private int raw = 0;
	private int video = 0;
	private int confirmed = 0;
	private int ignored = 0;
	// private int mformat = 0;
	private long size = 0;
	private long min = 0;
	private long max = 0;

	TreeMap<LocalDate, Integer> map = new TreeMap<>();

	List<Long> dateCounter_list = new ArrayList<>();

	public void calculate() throws Exception {
		for (FileInfo fi : folderInfo.getFileInfoList()) {
			if (Main.getProcessCancelled()) {
				break;
			}
			if (fi.isIgnored() || fi.isTableDuplicated()) {
				ignored++;
			} else {
				fi.getOrgPath();
				// fi.getDate();

				size += fi.getSize();

				if (fi.isBad()) {
					bad++;
				}
				if (fi.isConfirmed()) {
					confirmed++;
				}
				if (fi.isCopied()) {
					copied++;
				}
				if (fi.isGood()) {
					good++;
				}
				if (fi.getDate() != 0) {
					dateCounter_list.add(fi.getDate());
				} else {
					fi.getDate();
				}
				if (fi.isIgnored()) {
					ignored++;
				}
				if (fi.isImage()) {
					image++;
				}
				if (fi.isRaw()) {
					raw++;
				}
				if (fi.isVideo()) {
					video++;
				}
				LocalDate localDate = null;
				try {
					localDate = LocalDate.of(Integer.parseInt(simpleDates.getSdf_Year().format(fi.getDate())),
							Integer.parseInt(simpleDates.getSdf_Month().format(fi.getDate())),
							Integer.parseInt(simpleDates.getSdf_Day().format(fi.getDate())));

				} catch (Exception e) {
					Logger.getLogger(TableUtils.class.getName()).log(Level.SEVERE, null, e);
					Main.setProcessCancelled(true);
					Messages.errorSmth(ERROR, "", e, Misc.getLineNumber(), true);
				}

				map.put(localDate, 0);
			}
		}

		if (!dateCounter_list.isEmpty()) {
			Collections.sort(dateCounter_list);
			try {
				min = Collections.min(dateCounter_list);
				max = Collections.max(dateCounter_list);

			} catch (Exception e) {
				Logger.getLogger(TableUtils.class.getName()).log(Level.SEVERE, null, e);
			}
		}

		double dateDifferenceRatio = calculateDateDifferenceRatio(map);

		// folderInfo.setChanged(false);
		// folderInfo.setFolderFiles((image + raw + video));
		// folderInfo.setBadFiles(bad);
		// folderInfo.setFolderRawFiles(raw);
		// folderInfo.setFolderVideoFiles(video);
		// folderInfo.setGoodFiles(good);
		// folderInfo.setCopied(copied);
		// folderInfo.setFolderImageFiles(image);
		// folderInfo.setFolderSize(size);
		// folderInfo.setFolderImageFiles(image);
		// folderInfo.setFolderVideoFiles(video);
		//
		folderInfo.setChanged(false);
		folderInfo.setFolderFiles((1));
		folderInfo.setBadFiles(2);
		folderInfo.setFolderRawFiles(3);
		folderInfo.setFolderVideoFiles(4);
		folderInfo.setGoodFiles(5);
		folderInfo.setCopied(6);
		folderInfo.setFolderImageFiles(7);
		folderInfo.setFolderSize(8);
		folderInfo.setFolderImageFiles(9);
		folderInfo.setFolderVideoFiles(10);

		folderInfo.setMinDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(min));
		folderInfo.setMaxDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(max));

		folderInfo.setDateDifferenceRatio(dateDifferenceRatio);
		// sprintf("Datedifference ratio completed");
		// folderInfo.setDateDifferenceRatio(0);

		dateCounter_list.clear();
		bad = 0;
		good = 0;
		image = 0;
		raw = 0;
		video = 0;
		confirmed = 0;
	}

}
