/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.utils;

import static com.girbola.messages.Messages.sprintf;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.girbola.messages.Messages;

/**
 *
 * @author Marko
 */
public class Conversion {

	/**
	 * doubleTwoDesimal converts double two desimal
	 *
	 * @param arvo
	 * @return
	 */
	public static double doubleTwoDesimal(double arvo) {
		// debugText("Convertsize value is: " + arvo);
		double value = 0;
		DecimalFormat dcf = new DecimalFormat("0.00");
		String str = dcf.format(arvo);
		// sprintf("arvo VALUE is: " + arvo);
		// sprintf("ConvertDoublesize STR VALUE is: " + str);
		value = Double.parseDouble(str.replace(",", "."));
		// debugText("Convertsize STRING value is: " + value + "\nARVO CS: " + arvo);
		// sprintf("ConvertFLOATsize VALUE is: " + value);
		return value;
	}

	/**
	 * timeInDays converts value to time in days
	 *
	 * @param timediff
	 * @return
	 */
	public static int timeInDays(long timediff) {
		// sprintf("timediff = " + timediff);
		long diffDays = timediff / (24 * 60 * 60 * 1000);
		int value = (int) diffDays;
		if (diffDays <= 0) {
			value = 0;
		}
		// sprintf("timediffindays: " + diffDays);
		return value;
	}

	/**
	 * timeInSec converts value to time in secs
	 *
	 * @param time
	 * @return
	 */
	public static int timeInSec(long time) {
		// sprintf("timediff = " + time);
		long diffSeconds = time / 1000 % 60;
		return (int) diffSeconds;
	}

	/**
	 * stringDateToLong converts string date to long
	 *
	 * @param value
	 * @param smd
	 * @return long
	 */
	@Deprecated
	public static Long stringDateToLong_(String value, SimpleDateFormat smd) {
		long dateLong = 0;
		// 2013 03 08 y m d
		// sprintf("PARSING STRING TO DATE LONG: " + value);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(smd.toPattern());
		LocalDateTime dateTime = LocalDateTime.parse(value, formatter);

		if (dateTime != null) {
			try {
				dateLong = dateTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli();
			} catch (NullPointerException np) {
				dateLong = 0;
				sprintf("Returning null from stringDateToLong");
			}
		} else {
			dateLong = 0;
		}
		return (dateLong);
		// - (timeZone.getRawOffset()));
	}

	/**
	 * stringDateToLong converts string date to long
	 *
	 * @param value
	 * @param smd
	 * @return long
	 */
	public static long stringDateToLong(String value, SimpleDateFormat smd) {
		long dateLong = 0;
		// 2013 03 08 y m d
//		sprintf("PARSING STRING TO DATE LONG: " + value);
		// smd.toPattern();
		Date date = null;

		try {
			date = smd.parse(value);
		} catch (ParseException ex) {
			sprintf("parse exception: " + ex.getMessage() + " value were: " + value);
			return 0;
		}
		if (date != null) {
			try {
				dateLong = date.getTime();
				// sprintf("Conversion stringdatotlong: " + smd.format(dateLong));
			} catch (NullPointerException np) {
				dateLong = 0;
				// sprintf("Returning null from stringDateToLong");
			}
		} else {
			dateLong = 0;
		}
		// sprintf("stringDateToLong is: " + smd.format(dateLong));
		return (dateLong);
	}

	/**
	 * Returns String number in two digits e.g 0 = 00, 1 = 01 etc
	 *
	 * @param number
	 * @return
	 */
	public static String stringTwoDigits(int number) {

		if (number >= 0 && number <= 9) {
			return "0" + number;
		} else {
			return "" + number;
		}

	}

	public static String convertToSmallerConversion(long bytes) {
		float kilobyte = 1024;
		float megabyte = kilobyte * 1024;
		float gigabyte = megabyte * 1024;
		float terabyte = gigabyte * 1024;

		DecimalFormat df = new DecimalFormat("#.##");

		if ((bytes >= 0) && (bytes < kilobyte)) {
			return df.format(bytes) + " B";

		} else if ((bytes >= kilobyte) && (bytes < megabyte)) {
			return df.format(bytes / kilobyte) + " KB";

		} else if ((bytes >= megabyte) && (bytes < gigabyte)) {
			return df.format(bytes / megabyte) + " MB";

		} else if ((bytes >= gigabyte) && (bytes < terabyte)) {
			return df.format(bytes / gigabyte) + " GB";

		} else if (bytes >= terabyte) {
			return df.format(bytes / terabyte) + " TB";

		} else {
			return df.format(bytes) + " Bytes";
		}
	}

	/**
	 * 
	 * @param value
	 * @param digits
	 * @return
	 */
	public static String stringWithDigits(int value, int digits) {
		// 1 digits == 2 tulos 02
		if (digits < value) {
			Messages.sprintfError("Value were higher than digits. Digit: " + digits + " value: " + Conversion.stringTwoDigits(value));
			return Conversion.stringTwoDigits(value);
		}
		if (String.valueOf(value).length() < digits) {
			StringBuilder number = new StringBuilder();
			number.append(value);
			for (int i = 1; i < digits; i++) {
				if (i == digits) {
					System.out.println("number is: " + number);
					return number.toString();
				} else {
					number.insert(0, "0");
					System.out.println("inserting number: " + number);
				}
			}
			return number.toString();
		}
		return "" + value;
	}

}
