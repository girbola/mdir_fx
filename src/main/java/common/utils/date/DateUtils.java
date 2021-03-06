/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.utils.date;

import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.sprintf;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author Marko Lokka
 */
public class DateUtils {

	public static LocalDateTime parseLocalDateTimeFromString(String string) {
		// 2018-09-03 13:14:15
		LocalDateTime ldt = null;
		if (string.length() > 20) {
			string = string.substring(0, 20);
		}
		try {
			ldt = LocalDateTime.parse(string, simpleDates.getDtf_ymd_hms_minusDots_default());
		} catch (Exception e) {
			sprintf("parseLocalDateTimeFromString error: " + e.getMessage());
			return null;
		}
		return ldt;
	}

	/**
	 * Converts String date to LocalDate example 2000-12-03 21:00:00 to 2000 12
	 * 03
	 *
	 * @param string
	 * @return
	 */
	public static LocalDate parseLocalDateFromString(String string) {
		if (string.length() > 10) {
			string = string.substring(0, 10);
		}
		LocalDate ld = null;
		try {
			ld = LocalDate.parse(string, simpleDates.getDtf_ymd_minus());
		} catch (Exception e) {
			sprintf("parseLocalDateFromString error: " + e.getMessage());
			return null;
		}
		return ld;
	}

	public static LocalTime parseLocalTimeFromString(String string) {
		if (string.length() > 11) {
			string = string.substring(11, 19);
			sprintf("parseLocalTimeFromString substring time: " + string);
		}
		LocalTime lt = LocalTime.parse(string, simpleDates.getDtf_hms_dots());
		return lt;
	}

	public static LocalDateTime longToLocalDateTime(long millis) {

		LocalDateTime triggerTime = null;
		try {
			// triggerTime =
			// Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
			// triggerTime =
			// LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), TimeZone
			// .getDefault().toZoneId());
			triggerTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);

		} catch (Exception ex) {
			return null;
		}
		return triggerTime;

	}

	/**
	 * dateTime has to be formatted using yyyy-MM-dd HH.mm.ss
	 *
	 * @param dateTime
	 * @return
	 */
	public static LocalDateTime stringDateToLocalDateTime(String dateTime) {
		try {
			LocalDateTime ld = LocalDateTime.parse(dateTime, simpleDates.getDtf_ymd_hms_minusDots_default());
			return ld;
		} catch (Exception ex) {
			return null;
		}
	}

	public static long calculateMonthsOfLocalDates(LocalDate minDate, LocalDate maxDate) {
		if (minDate != null || maxDate != null) {
			return ChronoUnit.MONTHS.between(minDate, maxDate);
		}
		return 0;
	}
	public static long calculateYearsOfLocalDates(LocalDate minDate, LocalDate maxDate) {
		if (minDate != null || maxDate != null) {
			return ChronoUnit.YEARS.between(minDate, maxDate);
		}
		return 0;
	}

}
