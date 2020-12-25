package com.girbola.controllers.datefixer;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.girbola.Main;
import com.girbola.messages.Messages;

import common.utils.date.SimpleDates;

public class DateTester {

	public static String dateTester(String dateString) {
		// 2020-08-13
		// 2020/08/13
		// 2020.08.13
		try {
			Date date = Main.simpleDates.getSdf_ymd_minus().parse(dateString);
			return date.toString();
		} catch (ParseException e) {
			Messages.sprintf("DAte wasn't separated with minus");
			e.printStackTrace();
		}
		try {
			Date date = Main.simpleDates.getSdf_ymd_minus().parse(dateString);
			return date.toString();
		} catch (ParseException e) {
			Messages.sprintf("DAte wasn't separated with minus");
			e.printStackTrace();
		}

		return null;

	}

	private void pattern() {
		Pattern pattern = Pattern.compile("w3schools", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher("Visit W3Schools!");
		boolean matchFound = matcher.find();
		if (matchFound) {
			System.out.println("Match found");
		} else {
			System.out.println("Match not found");
		}
	}
}
