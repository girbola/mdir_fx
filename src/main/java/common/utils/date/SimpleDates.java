/*
 @(#)Copyright:  Copyright (c) 2012-2024 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.utils.date;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author Marko Lokka
 */
public class SimpleDates {

	private final String YMD_HMS_NOSPACES = "yyyyMMddHHmmss";

	private final String YMD_HMS_SPACES = "yyyy MM dd HH mm ss";
	private final String YMD_HMS_SPACE = "yyyyMMdd HHmmss";

	private final String YMD_HMS_SLASHDOTS = "yyyy/MM/dd HH.mm.ss";
	private final String YMD_HMS_MINUSDOTS_DEFAULT = "yyyy-MM-dd HH.mm.ss";
	private final String YMD_HMS_SLASHCOLON = "yyyy/MM/dd HH:mm:ss";
	private final String YMD_HMS_MINUSCOLON = "yyyy-MM-dd HH:mm:ss";
	private final String YMD_SLASH = "yyyy/MM/dd";
	private final String YMD_MINUS = "yyyy-MM-dd";

	private final static String YMD_H_MINUS = "yyyy-MM-dd HH";
	private final static String YMD_HM_MINUS = "yyyy-MM-dd HH.mm";
	private final static String YMD_HMS_MINUS = "yyyy-MM-dd HH.mm.ss";

	private final static String YMD_H_SLASH = "yyyy/MM/dd HH";
	private final static String HMS_COLON = "HH:mm:ss";
	private final static String HMS_DOTS = "HH.mm.ss";

	private final DateTimeFormatter dtf_ymd_hms_minusDots_default = DateTimeFormatter.ofPattern(YMD_HMS_MINUSDOTS_DEFAULT);
	private final DateTimeFormatter dtf_ymd_minus = DateTimeFormatter.ofPattern(YMD_MINUS);
	private final DateTimeFormatter dtf_hms_dots = DateTimeFormatter.ofPattern(HMS_DOTS);
	private final DateTimeFormatter dtf_ymd_slash = DateTimeFormatter.ofPattern(YMD_SLASH);
	private DateTimeFormatter dtf_ymd_hms_nospaces = DateTimeFormatter.ofPattern(YMD_HMS_NOSPACES);

	private SimpleDateFormat sdf_ymd_hms_minusColon = new SimpleDateFormat(YMD_HMS_MINUSCOLON);
	private SimpleDateFormat sdf_ymd_hms_minusDots_default = new SimpleDateFormat(YMD_HMS_MINUSDOTS_DEFAULT);
	private SimpleDateFormat sdf_ymd_hms_nospaces = new SimpleDateFormat(YMD_HMS_NOSPACES);
	private SimpleDateFormat sdf_ymd_hms_slashColon = new SimpleDateFormat(YMD_HMS_SLASHCOLON);
	private SimpleDateFormat sdf_ymd_hms_slashDots = new SimpleDateFormat(YMD_HMS_SLASHDOTS);
	private SimpleDateFormat sdf_ymd_hms_space = new SimpleDateFormat(YMD_HMS_SPACE);
	private SimpleDateFormat sdf_ymd_hms_spaces = new SimpleDateFormat(YMD_HMS_SPACES);
	private SimpleDateFormat sdf_ymd_minus = new SimpleDateFormat(YMD_MINUS);
	private SimpleDateFormat sdf_ymd_slash = new SimpleDateFormat(YMD_SLASH);

	private List<SimpleDateFormat> sdf_list = Arrays.asList(sdf_ymd_hms_minusDots_default, sdf_ymd_hms_slashDots);

	private DateTimeFormatter dtf_ymd_h_minus = DateTimeFormatter.ofPattern(YMD_H_MINUS);
	private DateTimeFormatter dtf_ymd_hm_minus = DateTimeFormatter.ofPattern(YMD_HM_MINUS);

	public final DateTimeFormatter getDtf_ymd_hm_minus() {
		return dtf_ymd_hm_minus;
	}

	public final DateTimeFormatter getDtf_ymd_hms_minus() {
		return dtf_ymd_hms_minus;
	}

	private DateTimeFormatter dtf_ymd_hms_minus = DateTimeFormatter.ofPattern(YMD_HMS_MINUS);

	public final DateTimeFormatter getDtf_ymd_h_minus() {
		return dtf_ymd_h_minus;
	}

	private SimpleDateFormat sdf_ymd_h_slash = new SimpleDateFormat(YMD_H_SLASH);

	private SimpleDateFormat sdf_hms_colon = new SimpleDateFormat(HMS_COLON);
	private SimpleDateFormat sdf_hms_dots = new SimpleDateFormat(HMS_DOTS);
	private SimpleDateFormat sdf_Year = new SimpleDateFormat("yyyy");
	private SimpleDateFormat sdf_Month = new SimpleDateFormat("MM");
	private SimpleDateFormat sdf_Day = new SimpleDateFormat("dd");
	private SimpleDateFormat sdf_Hour = new SimpleDateFormat("HH");
	private SimpleDateFormat sdf_Min = new SimpleDateFormat("mm");
	private SimpleDateFormat sdf_Sec = new SimpleDateFormat("ss");

	public DateTimeFormatter getDtf_ymd_slash() {
		return dtf_ymd_slash;
	}

	public DateTimeFormatter getDtf_hms_dots() {
		return dtf_hms_dots;
	}

	public DateTimeFormatter getDtf_ymd_minus() {
		return dtf_ymd_minus;
	}

	public DateTimeFormatter getDtf_ymd_hms_minusDots_default() {
		return dtf_ymd_hms_minusDots_default;
	}

	public DateTimeFormatter getDtf_ymd_hms_nospaces() {
		return dtf_ymd_hms_nospaces;
	}

	public void setDtf_ymd_hms_nospaces(DateTimeFormatter dtf_ymd_hms_nospaces) {
		this.dtf_ymd_hms_nospaces = dtf_ymd_hms_nospaces;
	}

	public SimpleDateFormat getSdf_ymd_hms_space() {
		return sdf_ymd_hms_space;
	}

	public void setSdf_ymd_hms_space(SimpleDateFormat sdf_ymd_hms_space) {
		this.sdf_ymd_hms_space = sdf_ymd_hms_space;
	}

	{
		getSdf_ymd_hms_minusColon().setTimeZone(TimeZone.getTimeZone("UTC"));
		getSdf_ymd_hms_minusDots_default().setTimeZone(TimeZone.getTimeZone("UTC"));
		getSdf_ymd_hms_slashColon().setTimeZone(TimeZone.getTimeZone("UTC"));
		getSdf_ymd_hms_slashDots().setTimeZone(TimeZone.getTimeZone("UTC"));
		getSdf_ymd_hms_spaces().setTimeZone(TimeZone.getTimeZone("UTC"));
		getSdf_ymd_hms_nospaces().setTimeZone(TimeZone.getTimeZone("UTC"));
		getSdf_ymd_minus().setTimeZone(TimeZone.getTimeZone("UTC"));
		// getSdf_ymd_hms_minusColon().setTimeZone(TimeZone.getDefault());
		// getSdf_ymd_hms_minusDots().setTimeZone(TimeZone.getDefault());
		// getSdf_ymd_hms_slashColon().setTimeZone(TimeZone.getDefault());
		// getSdf_ymd_hms_slashDots().setTimeZone(TimeZone.getDefault());
		// getSdf_ymd_hms_spaces().setTimeZone(TimeZone.getDefault());
		// getSdf_ymd_minus().setTimeZone(TimeZone.getDefault());
	}

	public SimpleDateFormat getSdf_ymd_hms_nospaces() {
		return sdf_ymd_hms_nospaces;
	}

	public SimpleDateFormat getSdf_ymd_hms_spaces() {
		return sdf_ymd_hms_spaces;
	}

	public SimpleDateFormat getSdf_ymd_minus() {
		return sdf_ymd_minus;
	}

	public SimpleDateFormat getSimpleDateFormatByString(String value) {
		for (SimpleDateFormat sdf : sdf_list) {
			Date date = null;
			try {
				date = sdf.parse(value);
			} catch (Exception e) {
			}

			return sdf;
		}
		return null;
	}

	public SimpleDateFormat getSdf_ymd_hms_slashDots() {
		return sdf_ymd_hms_slashDots;
	}

	/**
	 * Introdcution
	 *
	 * <dl>
	 * <dt><span class="strong">Heading 1</span></dt>
	 * <dd>There is a line
	 * break.</dd>
	 * <dt><span class="strong">Heading 2</span></dt>
	 * <dd>There is a line
	 * break.</dd>
	 * </dl>
	 *
	 * @return
	 */
	public SimpleDateFormat getSdf_ymd_hms_minusDots_default() {
		return sdf_ymd_hms_minusDots_default;
	}

	public SimpleDateFormat getSdf_ymd_hms_slashColon() {
		return sdf_ymd_hms_slashColon;
	}

	public SimpleDateFormat getSdf_ymd_hms_minusColon() {
		return sdf_ymd_hms_minusColon;
	}

	public SimpleDateFormat getSdf_ymd_slash() {
		return sdf_ymd_slash;
	}

	public SimpleDateFormat getSdf_ymd_h_slash() {
		return sdf_ymd_h_slash;
	}

	public SimpleDateFormat getSdf_hms_colon() {
		return sdf_hms_colon;
	}

	public SimpleDateFormat getSdf_hms_dots() {
		return sdf_hms_dots;
	}

	public SimpleDateFormat getSdf_Year() {
		return sdf_Year;
	}

	public SimpleDateFormat getSdf_Month() {
		return sdf_Month;
	}

	public SimpleDateFormat getSdf_Day() {
		return sdf_Day;
	}

	public SimpleDateFormat getSdf_Hour() {
		return sdf_Hour;
	}

	public SimpleDateFormat getSdf_Min() {
		return sdf_Min;
	}

	public SimpleDateFormat getSdf_Sec() {
		return sdf_Sec;
	}

}
