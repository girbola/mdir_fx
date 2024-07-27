package com.girbola.controllers.datefixer;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.Conversion;
import common.utils.FileUtils;
import common.utils.date.DateUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class DestinationResolver {
	/**
	 * 
	 * @param fileInfo
	 * @return Path for example 2018-08-29 12.34.55.jpg. if duplicated filename
	 *         found but filesize were not equals file will be renamed as 2018-08-29
	 *         12.34.55_2.jpg
	 */
	public static Path getDestinationFileName(FileInfo fileInfo) {
		String location_str = "";
		String event_str = "";

		if (fileInfo.getEvent().isEmpty() && !fileInfo.getLocation().isEmpty()) {
			location_str = " - " + fileInfo.getLocation();
		} else if (!fileInfo.getEvent().isEmpty() && fileInfo.getLocation().isEmpty()) {
			event_str = " - " + fileInfo.getEvent();
		} else {
			location_str = " - " + fileInfo.getLocation();
			event_str = " - " + fileInfo.getEvent();
		}

		LocalDate ld = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();
		Messages.sprintf("location were= '" + location_str + "'");
		Messages.sprintf("evemt were= '" + event_str + "'");
		// I:\\2017\\2017-06-23 Merikarvia - Kalassa �ijien kanssa
		// I:\\2017\\2017-06-24 Merikarvia - Kalassa �ijien kanssa
		String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate()).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
		Path destPath = Paths.get(Main.conf.getWorkDir() + File.separator + ld.getYear() + File.separator + ld + location_str + event_str
				+ File.separator + fileName + "." + FileUtils.getFileExtension(Paths.get(fileInfo.getOrgPath())));

		return destPath;
	}

	/**
	 * Returns the destination file name for miscellaneous files.
	 *
	 * @param source The source file path.
	 * @param fileInfo The information about the file.
	 * @return The destination file path for the miscellaneous file.
	 */
	public static Path getDestinationFileNameMisc(Path source, FileInfo fileInfo) {
//		if (Main.conf.getWorkDir().equals("null")) {
//			Messages.warningText(Main.bundle.getString("destinationDirNotSet"));
//			return null;
//		}
		LocalDate ld = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();
		// I:\\2017\\2017-06-23 Merikarvia - Kalassa äijien kanssa
		// I:\\2017\\2017-06-24 Merikarvia - Kalassa äijien kanssa
		String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate()).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());

		Path destPath = Paths.get(File.separator + ld.getYear() + File.separator
				+ Conversion.stringTwoDigits(ld.getMonthValue()) + File.separator + fileName + "." + FileUtils.getFileExtension(source));
		return destPath;
	}

	public static Path getDestinationFileNameAsItIs(Path source, FileInfo fileInfo) {
//		if (Main.conf.getWorkDir().equals("null")) {
//			Messages.warningText(Main.bundle.getString("destinationDirNotSet"));
//			return null;
//		}
		LocalDate ld = DateUtils.longToLocalDateTime(fileInfo.getDate()).toLocalDate();
		// I:\\2017\\2017-06-23 Merikarvia - Kalassa äijien kanssa
		// I:\\2017\\2017-06-24 Merikarvia - Kalassa äijien kanssa
		String fileName = DateUtils.longToLocalDateTime(fileInfo.getDate()).format(Main.simpleDates.getDtf_ymd_hms_minusDots_default());
		
		Path destPath = Paths.get( File.separator + fileName + "." + FileUtils.getFileExtension(source));
		return destPath;
	}

}
