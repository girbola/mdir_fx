package com.girbola.controllers.main.tasks;

import com.girbola.Main;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.concurrent.Task;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import static com.girbola.Main.simpleDates;

public class UpdateTableFolderInfo extends Task<Integer> {
	private final String ERROR = UpdateTableFolderInfo.class.getName();

	private int bad = 0;
	private int good = 0;
	private int image = 0;
	private int raw = 0;
	private int video = 0;
	private int confirmed = 0;
	private long size = 0;
	private int copied = 0;
	private FolderInfo folderInfo;

	public UpdateTableFolderInfo(FolderInfo folderInfo) {
		this.folderInfo = folderInfo;
	}

	@Override
	protected Integer call() throws Exception {
		Messages.sprintfError("uptd updateFolderInfos_FileInfo: " + folderInfo.getFolderPath());
		TreeMap<LocalDate, Integer> map = new TreeMap<>();
		List<Long> dateCounter_list = new ArrayList<>();
		if (folderInfo.getFileInfoList() == null) {
			Messages.sprintf("Somehow fileInfo list were null!!!");

			Main.setProcessCancelled(true);
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
			cancel();
		}
		for (FileInfo fi : folderInfo.getFileInfoList()) {
			if (isCancelled()) {
				Main.setProcessCancelled(true);
				return null;
			}
			if (Main.getProcessCancelled()) {
				cancel();
				break;
			}
			if (fi.isIgnored() || fi.isTableDuplicated()) {
				Messages.sprintfError("Were ignored or duplicated");
			} else {
				fi.getOrgPath();

				size += fi.getSize();

				if (fi.isCopied()) {
					copied++;
				}
				if (fi.isBad()) {
					bad++;
				}
				if (fi.isConfirmed()) {
					confirmed++;
				}
				if (fi.isGood()) {
					good++;
				}
				if (fi.isIgnored()) {
					Messages.sprintfError("isignored!");
				}
				if (fi.isTableDuplicated()) {
					Messages.sprintfError("isTABLRignored!");
				}
				if (fi.isRaw()) {
					raw++;
				}

				if (fi.isImage()) {
					image++;
				}
				if (fi.isVideo()) {
					video++;
				}
				if (fi.getDate() != 0) {
					dateCounter_list.add(fi.getDate());
				} else {
					fi.getDate();
				}

				LocalDate localDate = null;
				try {
					localDate = LocalDate.of(Integer.parseInt(simpleDates.getSdf_Year().format(fi.getDate())),
							Integer.parseInt(simpleDates.getSdf_Month().format(fi.getDate())),
							Integer.parseInt(simpleDates.getSdf_Day().format(fi.getDate())));

				} catch (Exception ex) {
					Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
				}

				map.put(localDate, 0);
			}
		}
		// folderInfo.setFolderFiles((image + raw + video) - ignored);

		folderInfo.setBadFiles(bad);
		// folderInfo.setConfirmed(confirmed);
		folderInfo.setFolderRawFiles(raw);
		folderInfo.setFolderVideoFiles(video);
		folderInfo.setGoodFiles(good);
		folderInfo.setCopied(copied);
		folderInfo.setFolderImageFiles(image);
		folderInfo.setFolderSize(size);
		long min = 0;
		long max = 0;

		if (!dateCounter_list.isEmpty()) {
			Collections.sort(dateCounter_list);
			try {
				min = Collections.min(dateCounter_list);
				max = Collections.max(dateCounter_list);

			} catch (Exception ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
		}

		folderInfo.setMinDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(min));
		folderInfo.setMaxDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(max));

		double dateDifferenceRatio = calculateDateDifferenceRatio(map);
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

		return null;
	}

	private double calculateDateDifferenceRatio(TreeMap<LocalDate, Integer> map) {
		List<Double> list = new ArrayList<>();

		double tester = 0;
		boolean pass = false;
		LocalDate localDate = null;

		for (Entry<LocalDate, Integer> entry : map.entrySet()) {
			if (!pass) {
				pass = true;
				localDate = entry.getKey();
			} else {
				LocalDate localDate2 = entry.getKey();
				Period per = Period.between(localDate, localDate2);
				double days = per.getDays();
				list.add(days - tester);
				localDate = localDate2;
			}
		}
		double sum = 0;
		for (Double db : list) {
			sum += db;
		}
		if (list.isEmpty()) {
			return 0;
		} else {
			return Collections.max(list);
		}
	}

}
