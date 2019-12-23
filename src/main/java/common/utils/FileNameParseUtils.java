/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.utils;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko
 */
public class FileNameParseUtils {

	private final static String ERROR = FileNameParseUtils.class.getSimpleName();

	// private final static String DATE_WITH_DELIM =
	// "\\d{4}\\W\\d{2}\\W\\d{2}\\s\\d{2}\\W\\d{2}\\W\\d{2}";
	//2004
	/*
	 * .	Any character (may or may not match line terminators)
	* \d	A digit: [0-9]
	* \D	A non-digit: [^0-9]
	* \s	A whitespace character: [ \t\n\x0B\f\r]
	* \S	A non-whitespace character: [^\s]
	* \w	A word character: [a-zA-Z_0-9]
	* \W	A non-word character: [^\w]
	 */
	// 2004
//	erG;
	private final static String DATE_WITH_SEPARATOR = "\\d{4}\\W\\d{2}\\W\\d{2}[\\w|\\s]{1}\\d{2}\\W\\d{2}\\W\\d{2}"; // 2000-12-13 12.13.14 & 2000-12-13_12.13.14
	private final static String DATE_WITH_YMDHMS = "\\d{4}\\d{2}\\d{2}\\d{2}\\d{2}\\d{2}"; // 20001213121314
	// private final static String DATE_WITH_YMD_HMS =
	// "\\d{4}\\d{2}\\d{2}\\d{2}\\d{2}\\d{2}"; //20001213121314

	private final static String DATE_WITH_YMD_HMS = "\\d{4}\\d{2}\\d{2}[\\w|\\s]{1}\\d{2}\\d{2}\\d{2}"; // 20001213 121314
	private static final List<String> regex_list = Arrays.asList(DATE_WITH_SEPARATOR, DATE_WITH_YMDHMS, DATE_WITH_YMD_HMS);

	/* Filename parse utils START */
	/**
	 * Split file name & path & extension
	 *
	 */
	public static String[] imageFileNamePrefix = { "CIMG", "DSC_", "DSCF", "DSCN", "DSC_", "IMG_", "MVI_", "SAM_" };

	/**
	 * Parse known imagename running number IMG_1234 return 1234
	 *
	 * @param str
	 * @return Known imagenane running number IMG_1234 return 1234
	 */
	@Deprecated
	public static Integer getFileNameRunningNumber(String str) {
		int value = 0;

		String number = "";
		for (int i = 0; i < str.length(); i++) {
			Character character = str.charAt(i);
			if (Character.isDigit(character)) {
				number += character;
			}
		}
		if (number.length() > 1) {
			value = Integer.parseInt(number);
		}
		return value;
	}

	/**
	 * Check if known image name format
	 *
	 * @param path
	 * @return knownPrefix
	 */
	@Deprecated
	public static String knownImageName(Path path) {
		// 20070909_DCS0197.jpg
		// 20070909_IMG_0198.jpg

		for (String str : imageFileNamePrefix) {
			if (path.getFileName().toString().toLowerCase().contains(str.toLowerCase())) {
				return str;
			}
		}
		return null;
	}

	/**
	 * parse Beginning Of fileName
	 *
	 * @param path
	 * @return Begin of fileName stripping from delims till end
	 */
	@Deprecated
	public static String parseBOFfileName(Path path) {
		String delims = knownImageName(path);
		if (delims.length() > 1) {
			return path.getFileName().toString().substring(0, path.getFileName().toString().indexOf(delims));
		}
		// 1233456_IMG_800.jpg to 123456
		return null;
	}

	/**
	 * parse bold filename 2013 09 09 IMG_1234.jpg to known image name format
	 * IMG_1234.jpg
	 *
	 * @param path
	 * @return foobarIMG_1234.jpg into IMG_1234.jpg
	 */
	@Deprecated
	public static String parseEOFfilename(Path path) {
		String delims = knownImageName(path);
		if (delims.length() != 0) {
			if (delims.length() > 1) {
				return path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf(delims));
			}
		}
		return null;
	}

	/**
	 * Parse file name extension example: C:/Temp/IMG_1234.jpg will be IMG_1234
	 *
	 * @param path
	 * @return
	 */
	public static String parseFileExtentension(Path path) {
		return path.getFileName().toString().substring(0, path.getFileName().toString().lastIndexOf("."));
	}

	public static long hasFileNameDate(Path path) {
		//dfb;

		long ymd_hms_separator = tryFileNameDate(path, DATE_WITH_SEPARATOR);
		if (ymd_hms_separator >= 1) {
			return ymd_hms_separator;
		}
//		long ymd_hms_separator = tryFileNameDate(path, DATE_WITH_SEPARATOR);
//		if (ymd_hms_separator >= 1) {
//			return ymd_hms_separator;
//		}
		long ymdhms = tryFileNameDate(path, DATE_WITH_YMDHMS);
		if (ymdhms >= 1) {
			return ymdhms;
		}
		long ymd_hms = tryFileNameDate(path, DATE_WITH_YMD_HMS);
		if (ymd_hms >= 1) {
			return ymd_hms;
		}
		return 0;
	}

	protected static long tryFileNameDateKnownFormat(Path path) {

		for (String regex : regex_list) {
			String result = "";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(parseFileExtentension(path));

			if (m.find()) {
				result = m.group();
				//                sprintf("match found; " + result);
				SimpleDateFormat simpleDateFormat = simpleDates.getSimpleDateFormatByString(result);
				if (simpleDateFormat != null) {
					sprintf("simpleDateFormat: " + simpleDateFormat.toPattern());
				}
			}

			if (!result.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				result = m.group(); // 2016-10-29 14:31:44
				for (char s : result.toCharArray()) {
					if (s >= '0' && s <= '9') {
						sb.append(s);
					} else {
						sb.append("");
					}
				}
				// sprintf("SB RESULT: " + sb);
				// String d = hasDate(sb
				if (!sb.toString().isEmpty()) {
					long date = 0;
					try {
						date = Conversion.stringDateToLong(sb.toString(), simpleDates.getSdf_ymd_hms_nospaces());
						return date;
					} catch (Exception ex) {
						sprintf("Exception d = hasDate(sb); " + result + " exception is: " + ex.getMessage());
						return 0;
					}
				}
			}
			result = null;

		}
		return 0;
	}

	public static long findDateFromFilename(Path path, String dateDelim) {
		String result = "";
		Pattern p = Pattern.compile(dateDelim);
		Matcher m = p.matcher(parseFileExtentension(path));

		if (m.find()) {
			result = m.group();
		}

		if (!result.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			result = m.group(); // 2016-10-29 14:31:44
			for (char s : result.toCharArray()) {
				if (s >= '0' && s <= '9') {
					sb.append(s);
				} else {
					sb.append(" ");
				}
			}
			//            sprintf("SB RESULT: " + sb);
			// String d = hasDate(sb);
			if (!sb.toString().isEmpty()) {
				long date = 0;
				try {
					date = Conversion.stringDateToLong(sb.toString(), simpleDates.getSdf_ymd_hms_spaces());
					//                    sprintf("stringDateToLong is: " + date);
					return date;
				} catch (Exception ex) {
					sprintf("Exception d = hasDate(sb); " + result + " exception is: " + ex.getMessage());
					return 0;
				}
			}
		}
		return 0;
	}

	protected static long tryFileNameDate(Path path, String dateDelim) {
		String result = "";
		Pattern p = Pattern.compile(dateDelim);
		Matcher m = p.matcher(parseFileExtentension(path));

		if (m.find()) {
			result = m.group();
		}

		if (!result.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			result = m.group(); // 2016-10-29 14:31:44
			for (char s : result.toCharArray()) {
				if (s >= '0' && s <= '9') {
					sb.append(s);
				}
				// else {
				// sb.append(" ");
				// }
			}
			//            sprintf("SB RESULT: " + sb);
			//            System.out.println("SB RESULT date is: " + (sb.toString()));
			if (isValidDate(sb.toString())) {
				if (!sb.toString().isEmpty()) {
					long date = 0;
					// date = stringDateToLong(sb.toString());
					try {
						date = Conversion.stringDateToLong(sb.toString(), simpleDates.getSdf_ymd_hms_nospaces());
						//                        sprintf("stringDateToLong is: " + date);
						return date;
					} catch (Exception ex) {
						sprintf("Exception d = hasDate(sb); " + result + " exception is: " + ex.getMessage());
						return 0;
					}
				}
			}
		}
		return 0;
	}

	public static boolean isValidDate(String inDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(inDate.trim());
		} catch (ParseException pe) {
			return false;
		}
		return true;
	}
}
