/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */

package com.girbola.controllers.datefixer;

import static com.girbola.messages.Messages.sprintf;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

/**
 *
 * @author Marko Lokka
 */

/**
 *
 * @author Marko Lokka
 */
public class LocalTimeDifference {

	private final String ERROR = LocalTimeDifference.class.getSimpleName();

	private boolean isOverDay;
	private LocalDateTime start;
	private LocalDateTime end;

	/**
	 * arrangeListEvenly arrange List<Integer> list example 1, 1, 1, 0, 0 will be 1,
	 * 1, 0, 0, 1 end or begin of list won't ever be 0
	 *
	 * @param list
	 * @return
	 */
	public int arrangeListEvenly(List<Integer> list) {
		boolean found = false;
		for (int i = 0; i < list.size(); i--) {
			if (list.get(i) == 0) {
				sprintf("arrangeListEvenly were 0");
				found = true;
			}
			if (found) {
				if (list.get(i) != 0) {
					return i;
				}
			}
		}
		return 0;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public LocalTimeDifference(LocalDateTime start, LocalDateTime end) {
		this.start = start;
		this.end = end;
	}

	public boolean isIsOverDay() {
		return isOverDay;
	}

	public long getTimeInSeconds(LocalTime start_lt, LocalTime end_lt) {
		// Duration d = Duration.between(start_lt, end_lt);
		long start_tm = convertTimeToMillis(start_lt);
		long end_tm = convertTimeToMillis(end_lt);
		sprintf("getTimeInSeconds start: " + start_tm + " end: " + end_tm);
		if (start_tm > end_tm) {
			isOverDay = true;
			end_tm = (end_tm + (24 * 60 * 60));
			sprintf("start > end: " + end_tm + "-" + start_tm + " is: " + (end_tm - start_tm));
			return ((end_tm / 1000) - (start_tm / 1000));
		} else {
			isOverDay = false;
			sprintf("start < end is not over day");
			return ((end_tm / 1000) - (start_tm / 1000));
		}
	}

	public int getDays(LocalDate startDate, LocalDate endDate) {
		int daysTotal;
		Period day = Period.between(startDate, endDate);
		daysTotal = day.getDays();
		if (daysTotal < 0) {
			sprintf("Error with daysTotal: " + daysTotal);
			System.exit(0);
		}
		return daysTotal;
	}

	private long convertTimeToMillis(LocalTime startTime) {
		long h = (startTime.getHour() * 60 * 60);
		long m = (startTime.getMinute() * 60);
		long s = (startTime.getSecond());
		return (h + m + s) * 1000;
	}

	public ArrayList<LocalDateTime> createDateList_logic(int files, LocalDateTime start, LocalDateTime end) {
		sprintf("createDateList files= " + files + " strt :" + start + " end: " + end);
		if (files <= 0) {
			Messages.warningText(Main.bundle.getString("noSelectedFiles"));
			sprintf("createDateList_logic there were no files");
			return null;
		}

		ArrayList<LocalDateTime> ld = new ArrayList<>();

		int days = getDays(start.toLocalDate(), end.toLocalDate());
		int seconds = Math.round(getTimeInSeconds(start.toLocalTime(), end.toLocalTime()));
//		int seconds = Math.round((getTimeInSeconds(getStart().toLocalTime(), getEnd().toLocalTime())));
		sprintf("days: " + days + " seconds: " + seconds);
		if (seconds > Integer.MAX_VALUE) {
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}

		if (days == 0) {
			sprintf("days == 0");
			ArrayList<LocalDateTime> list = createDateList_NODAYS(files, start, end);
			for (LocalDateTime l : list) {
				sprintf("2LocalDateTime: " + l);
			}
			return list;
			// listlc(list, 1);
		} else {

			sprintf("---> Days more than zero: Files: " + files + " Days: " + days);
			// days 2 files 5
			if (days < files) {
				sprintf("days < files");
				ArrayList<LocalDateTime> list = createDateList_DAYS(files, start, end);
				listlc(list, 2);
				return list;
			} else {
				sprintf("days > files");
				ArrayList<LocalDateTime> list = createDateList_DAYS(files, start, end);
				listlc(list, 3);
				return list;
			}
		}
		// return ld;
	}

	public ArrayList<LocalDateTime> createDateList_DAYS(int files, LocalDateTime start, LocalDateTime end) {
		sprintf("createDateList_DAYS files= " + files);
		ArrayList<LocalDateTime> ld = new ArrayList<>();

		LocalDateTime runc = null;

		int days = 0;
		if (start != null && end != null) {
			days = getDays(start.toLocalDate(), end.toLocalDate());
			days += 1;
		} else {
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}
		sprintf("days: " + days);

		double filesPerDay = 0;

		double current_day_splitter = 0;
		long seconds = Math.round((getTimeInSeconds(getStart().toLocalTime(), getEnd().toLocalTime())));

		sprintf("filesPerDay: " + filesPerDay);
		sprintf("time_duration (SEC): " + seconds);
		runc = start;

		List<Integer> list = null;
		list = splitIntoParts(files, days);

		arrangeList(list);

		List<Integer> current_time_splitter = null;
		LocalDateTime startingDateTime = runc;
		for (int i = 0; i < days; i++) {
			sprintf("day-->= " + i);
			if (list.get(i) != 0) {
				current_day_splitter = list.get(i);
				current_time_splitter = splitIntoParts((int) seconds, (int) current_day_splitter);
//TODO What is going on here?! Fix!
				int counter1 = 0;
				long counter2 = 0;
				if (current_day_splitter > 0) {
					for (int time : current_time_splitter) {
						counter2 = (long) Math.round((double) time / (double) current_time_splitter.size());
						Math.abs(counter2);
						sprintf("counter2= " + counter2);
						if (counter1 == 0) {
							ld.add(runc);
						} else {
							runc = runc.plusSeconds(time + counter2);
							ld.add(runc);
						}
						counter1++;
					}
				}
				if (getDays(startingDateTime.toLocalDate(), runc.toLocalDate()) >= (days + 1)) {
					runc = LocalDateTime.of(runc.getYear(), runc.getMonth(), runc.getDayOfMonth(), getStart().getHour(),
							getStart().getMinute(), getStart().getSecond());
				} else {

					runc = runc.plusDays(1);
					runc = runc.of(runc.getYear(), runc.getMonth(), runc.getDayOfMonth(), getStart().getHour(),
							getStart().getMinute(), getStart().getSecond());
				}

			} else {
				runc = runc.plusDays(1);
				runc = runc.of(runc.getYear(), runc.getMonth(), runc.getDayOfMonth(), getStart().getHour(),
						getStart().getMinute(), getStart().getSecond());

			}
		}
		if (ld.isEmpty()) {
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}
		return ld;
	}

	public ArrayList<LocalDateTime> createDateList_NODAYS(int files, LocalDateTime start, LocalDateTime end) {
		sprintf("createDateList_NODAYS - files= " + files);
		ArrayList<LocalDateTime> ld = new ArrayList<>();

		LocalDateTime runc = null;

		int days = 0;
		if (start != null && end != null) {
			days = getDays(start.toLocalDate(), end.toLocalDate());
		} else {
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}
		sprintf("days: " + days);

		int seconds = Math.round((getTimeInSeconds(start.toLocalTime(), end.toLocalTime())));

		sprintf("time_duration (seconds): " + seconds);
		runc = getStart();

		List<Integer> current_time_splitter = null;
		current_time_splitter = splitIntoParts(seconds, files);
		LocalDateTime startingDateTime = runc;

		sprintf("current_time_splitter: " + current_time_splitter.size());
		int c = 0;
		for (int tt : current_time_splitter) {
			// long cc = (long) Math.round((double) tt / (double)
			// current_time_splitter.size());
			sprintf(c + " no_days new time is; " + (tt));
			if (c == 0) {
				ld.add(runc);
			} else {
				runc = runc.plusSeconds(tt);
				ld.add(runc);
			}
			c++;
		}
		if (ld.isEmpty()) {
			Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
		}
		for (LocalDateTime l : ld) {
			sprintf("LocalDateTime: " + l);
		}
		return ld;
	}

	private static void arrangeList(List<Integer> list) {

		int zeros = getZeros(list);
		int numbers = getNumbers(list);

		if (numbers <= 0) {
			sprintf("arrangeList - numbers <= 0 - There were no files!");
			return;
		}
		if (numbers > 2 && zeros > 1) {
			int sqr = (int) Math.sqrt(zeros);
			sprintf("sqr: " + sqr);
		}
		sprintf("Zeros were: " + zeros + " numbers were: " + numbers);

		int first = list.get(0);
		int last = (list.get(list.size() - 1));
		sprintf("ooo: " + last);
		if (numbers > 1) {
			if (first == 0) {
				sprintf(" arrangeList - first was zero!");
			} else {
				sprintf("arrangeList - first was not zero= " + list.get(0));
			}
			if (last == 0) {
				sprintf("arrangeList - last == 0");
				int closest_idx = getClosest(list);
				if (closest_idx < 0) {
					sprintf("error with closest_idx");
				} else {
					sprintf("getclosest_idx = " + closest_idx);
					Collections.swap(list, closest_idx, (list.size() - 1));

					sprintf("list size()= " + list.size());
					sprintf("get lisy value= " + list.get(list.size() - 1));
					for (int test : list) {
						sprintf("arrangeList - final = " + test);
					}
				}
			}
		}
	}

	private static int getClosest(List<Integer> list) {
		ArrayList<Integer> list2 = new ArrayList<Integer>(list);
		// Collections.reverse(list2);
		int line = 0;
		for (int a : list2) {
			if (a == 0) {
				sprintf("zero found: " + line);
				return line - 1;
			} else {
				sprintf("getClosest aaa: " + a);
				line++;
			}
		}
		return 0;
	}

	private static int getNumbers(List<Integer> list) {
		int counter = 0;
		for (int i : list) {
			if (i != 0) {
				counter++;
			}
		}
		return counter;
	}

	private static int getZeros(List<Integer> list) {
		int counter = 0;
		for (int i : list) {
			if (i == 0) {
				counter++;
			}
		}
		return counter;
	}

	private void listlc(ArrayList<LocalDateTime> list, int i) {
		int c = 0;
		for (LocalDateTime lt : list) {
			sprintf(c + " * " + i + " localdt: " + lt);
			c++;
		}
	}

	protected List<Integer> splitIntoParts(int whole, int parts) {
		List<Integer> list = new ArrayList<>();
		int[] arr = new int[parts];
		int remain = whole;
		int partsLeft = parts;
		for (int i = 0; partsLeft > 0; i++) {
			int size = (remain + partsLeft - 1) / partsLeft; // rounded up, aka ceiling
			arr[i] = size;
			remain -= size;
			partsLeft--;
		}
		for (int a : arr) {
			list.add(a);
		}
		arr = null;
		return list;
	}

}
