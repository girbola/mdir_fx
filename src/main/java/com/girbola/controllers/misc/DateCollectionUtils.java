package com.girbola.controllers.misc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.girbola.Main;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;

import java.util.TreeMap;

import common.utils.date.DateUtils;

public class DateCollectionUtils {

	private static LocalDateTime last = null;

	public static Map<String, List<FileInfo>> getByDay(List<FileInfo> list) {
		Map<String, List<FileInfo>> map = new TreeMap<>();
		for (FileInfo fi : list) {
			LocalDateTime ldt = DateUtils.longToLocalDateTime(fi.getDate());
			String date = ldt.format(Main.simpleDates.getDtf_ymd_minus());
			Messages.sprintf("Date: " + date);
			if (map.containsKey(date)) {
				add(map, date, fi, Main.simpleDates.getDtf_ymd_minus());
			} else {
				List<FileInfo> date_list = new ArrayList<>();
				date_list.add(fi);
				map.put(date, date_list);
			}
		}
		sortLists(map);
		return map;
	}

	public static Map<String, List<FileInfo>> getByHour(List<FileInfo> list) {
		Map<String, List<FileInfo>> map = new TreeMap<>();

		for (FileInfo fi : list) {
			LocalDateTime ldt = DateUtils.longToLocalDateTime(fi.getDate());
			String date = ldt.format(Main.simpleDates.getDtf_ymd_h_minus());
			Messages.sprintf("Date: " + date);
			if (map.containsKey(date)) {
				add(map, date, fi, Main.simpleDates.getDtf_ymd_h_minus());
			} else {
				List<FileInfo> date_list = new ArrayList<>();
				date_list.add(fi);
				map.put(date, date_list);
			}
		}
		sortLists(map);
		return map;
	}

	public static Map<String, List<FileInfo>> getByMin(List<FileInfo> list) {
		// LocalDateTime prev = null;
		Map<String, List<FileInfo>> map = new TreeMap<>();
		
		for (FileInfo fi : list) {
			LocalDateTime ldt = DateUtils.longToLocalDateTime(fi.getDate());
			String date = ldt.format(Main.simpleDates.getDtf_ymd_hm_minus());
			Messages.sprintf("Date: " + date);
			if (map.containsKey(date)) {
				add(map, date, fi, Main.simpleDates.getDtf_ymd_h_minus());
			} else {
				List<FileInfo> date_list = new ArrayList<>();
				date_list.add(fi);
				map.put(date, date_list);
			}
		}
		sortLists(map);
		return map;
	}

	public static Map<String, List<FileInfo>> getBySeconds(List<FileInfo> list) {
		Map<String, List<FileInfo>> map = new TreeMap<>();
		for (FileInfo fi : list) {
			LocalDateTime ldt = DateUtils.longToLocalDateTime(fi.getDate());
			String date = ldt.format(Main.simpleDates.getDtf_ymd_hms_minus());
			Messages.sprintf("Date: " + date);
			if (map.containsKey(date)) {
				add(map, date, fi, Main.simpleDates.getDtf_ymd_hms_minus());
			} else {
				List<FileInfo> date_list = new ArrayList<>();
				date_list.add(fi);
				map.put(date, date_list);
			}
		}
		sortLists(map);
		return map;
	}

	private static void add(Map<String, List<FileInfo>> map, String dateToSearch, FileInfo fi, DateTimeFormatter dateTimeFormatter) {
		for (Entry<String, List<FileInfo>> entry : map.entrySet()) {
			LocalDateTime ldt = DateUtils.longToLocalDateTime(fi.getDate());
			String date = ldt.format(dateTimeFormatter);
			if (date.contains(dateToSearch)) {
				entry.getValue().add(fi);
			}
		}

	}

	private static void sortLists(Map<String, List<FileInfo>> map) {
		for (Entry<String, List<FileInfo>> entry : map.entrySet()) {
			List<FileInfo> list = entry.getValue();
			Collections.sort(list, new Comparator<FileInfo>() {

				@Override
				public int compare(FileInfo o1, FileInfo o2) {
					if (o1.getDate() < o2.getDate()) {
						return -1;
					} else if (o1.getDate() > o2.getDate()) {
						return 1;
					}
					return 0;
				}
			});
		}

	}

}