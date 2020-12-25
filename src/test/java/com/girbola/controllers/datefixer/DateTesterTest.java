package com.girbola.controllers.datefixer;

import static org.junit.jupiter.api.Assertions.fail;

import java.text.SimpleDateFormat;

import org.junit.jupiter.api.Test;

import common.utils.date.SimpleDates;

class DateTesterTest {

	@Test
	void test() {
		SimpleDates sd = new SimpleDates();
		String date = "2000-01-12 12.13.11";
		SimpleDateFormat checkIfStringHasDate = sd.checkIfStringHasDate(date);
		if (checkIfStringHasDate == null) {
			fail("Not yet implemented");
		} else {
			System.out.println("date is valid: " + date);
		}
	}

}
