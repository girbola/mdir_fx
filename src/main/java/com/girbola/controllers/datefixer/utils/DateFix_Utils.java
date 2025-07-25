
package com.girbola.controllers.datefixer.utils;

import com.girbola.controllers.datefixer.table.EXIF_Data_Selector;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

import static com.girbola.Main.simpleDates;
import static com.girbola.messages.Messages.sprintf;


public class DateFix_Utils {

	private ObservableList<EXIF_Data_Selector> cameras_obs = FXCollections.observableArrayList();
	private ObservableList<EXIF_Data_Selector> date_obs = FXCollections.observableArrayList();
	private ObservableList<EXIF_Data_Selector> events_obs = FXCollections.observableArrayList();
	private ObservableList<EXIF_Data_Selector> locations_obs = FXCollections.observableArrayList();

	public ObservableList<EXIF_Data_Selector> getDate_obs() {
		return date_obs;
	}

	public ObservableList<EXIF_Data_Selector> getCameras_obs() {
		return cameras_obs;
	}

	public void updateCamera_list(List<FileInfo> fileInfo_list) {
		List<Map<String, Integer>> list_map = new ArrayList<>();
		List<String> camera_list = new ArrayList<>();

		for (FileInfo fi : fileInfo_list) {
			if (fi.getCamera_model() == null) {
				if (!hasValue(MetadataField.UNKNOWN.getType(), camera_list)) {
					camera_list.add(MetadataField.UNKNOWN.getType());
				}
			} else {
				if (!hasValue(fi.getCamera_model(), camera_list)) {
					camera_list.add(fi.getCamera_model());
				}
			}
		}
		Collections.sort(camera_list);

		for (String camera : camera_list) {
			sprintf("Camera model:" + camera + ":");
			Map<String, Integer> map = findHashMap(camera, list_map);
			for (FileInfo fi : fileInfo_list) {
				if (fi.getCamera_model() == null) {
					if (MetadataField.UNKNOWN.getType().equals(camera)) {
						int count = map.getOrDefault(camera, 0);
						map.put(camera, count + 1);
					}
				} else {
					if (fi.getCamera_model().equals(camera)) {
						int count = map.getOrDefault(camera, 0);
						map.put(camera, count + 1);
					}
				}
			}
		}
		for (Map<String, Integer> entry : list_map) {
			for (Map.Entry<String, Integer> entry2 : entry.entrySet()) {
				cameras_obs.add(new EXIF_Data_Selector(true, entry2.getKey(), entry2.getValue()));
			}
		}
	}

	public void createCamera_list(List<FileInfo> fileInfo_list) {
		List<Map<String, Integer>> list_map = new ArrayList<>();
		List<String> camera_list = new ArrayList<>();
		for (FileInfo fi : fileInfo_list) {
			if (fi.getCamera_model() == null) {
				fi.setCamera_model(MetadataField.UNKNOWN.getType());
			}
			if (fi.getCamera_model().length() == 0 || fi.getCamera_model().isEmpty()) {
				fi.setCamera_model(MetadataField.UNKNOWN.getType());
			}
			if (!hasValue(fi.getCamera_model(), camera_list)) {
				sprintf("3Adding camera: " + fi.getCamera_model());
				camera_list.add(fi.getCamera_model());
			}

		}
		Collections.sort(camera_list);
		for (String camera : camera_list) {
			Map<String, Integer> map = findHashMap(camera, list_map);
			for (FileInfo fi : fileInfo_list) {
				if (fi.getCamera_model() == null) {
					if (MetadataField.UNKNOWN.getType().equals(camera)) {
						int count = map.getOrDefault(camera, 0);
						map.put(camera, count + 1);
					}
				} else {
					if (fi.getCamera_model().equals(camera)) {
						int count = map.getOrDefault(camera, 0);
						map.put(camera, count + 1);
					}
				}
			}
		}
		for (Map<String, Integer> entry : list_map) {
			for (Map.Entry<String, Integer> entry2 : entry.entrySet()) {
				cameras_obs.add(new EXIF_Data_Selector(false, entry2.getKey(), entry2.getValue()));
			}
		}
	}

	public void createDates_list(List<FileInfo> fileInfo_list) {
		//TODO createDates_list - Something is wrong here
		List<Map<String, Integer>> list_map = new ArrayList<>();
		List<String> date_list = new ArrayList<>();
		for (FileInfo fi : fileInfo_list) {
			if (!hasValue(simpleDates.getSdf_ymd_minus().format(fi.getDate()), date_list)) {
				date_list.add(simpleDates.getSdf_ymd_minus().format(fi.getDate()));
			}
			fileInfoField(date_list, fi, MetadataField.DATE.getType());
		}
		Collections.sort(date_list);
		for (String dates : date_list) {
			Map<String, Integer> map = findHashMap(dates, list_map);
			for (FileInfo fi : fileInfo_list) {
				if (simpleDates.getSdf_ymd_minus().format(fi.getDate()).equals(dates)) {
					int count = map.getOrDefault(dates, 0);
					map.put(dates, count + 1);
				}
			}
		}
		if (!date_obs.isEmpty()) {
			date_obs.clear();
		}
		for (Map<String, Integer> entry : list_map) {
			for (Map.Entry<String, Integer> entry2 : entry.entrySet()) {
				date_obs.add(new EXIF_Data_Selector(false, entry2.getKey(), entry2.getValue()));
			}
		}
	}

	private Map<String, Integer> findHashMap(String dt, List<Map<String, Integer>> list_map) {
		if (!list_map.isEmpty()) {
			for (Map<String, Integer> entry : list_map) {
				for (Map.Entry<String, Integer> entry2 : entry.entrySet()) {
					if (entry2.getKey().equals(dt)) {
						return entry;
					}
				}
			}
		}

		Map<String, Integer> map = new HashMap<>();
		list_map.add(map);
		return map;

	}

	private boolean hasValue(String format, List<String> list) {
		for (String str : list) {
			if (format.equals(str)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param fileInfo_list
	 * @param obs
	 * @param fileInfoField
	 */
	public void createTableEXIF_Data_Selector_list(List<FileInfo> fileInfo_list, ObservableList<EXIF_Data_Selector> obs, String fileInfoField) {
		List<Map<String, Integer>> list_map = new ArrayList<>();
		List<String> exifInfo_list = new ArrayList<>();
		for (FileInfo fileInfo : fileInfo_list) {
			fileInfoField(exifInfo_list, fileInfo, fileInfoField);
		}
		if (exifInfo_list.isEmpty()) {
			Messages.sprintf("ExifInfo was empty");
			return;
		}
		Collections.sort(exifInfo_list);
		for (String value : exifInfo_list) {
			Map<String, Integer> map = findHashMap(value, list_map);
			for (FileInfo fileInfo : fileInfo_list) {
				checkEquals(map, fileInfo, value, fileInfoField);
			}
		}
		Messages.sprintf("checkEquals done!");
		for (Map<String, Integer> entry : list_map) {
			for (Map.Entry<String, Integer> entry2 : entry.entrySet()) {

				Messages.sprintf("Adding to obs: " + entry2.getKey() + " : " + entry2.getValue());
				obs.add(new EXIF_Data_Selector(false, entry2.getKey(), entry2.getValue()));
			}
		}

	}

	private void checkEquals(Map<String, Integer> map, FileInfo fileInfo, String info, String fileInfoField) {
		if (fileInfoField.equals(MetadataField.EVENT.getType())) {
			if (fileInfo.getEvent().equals(info)) {
				int count = map.containsKey(info) ? map.get(info) : 0;
				map.put(info, count + 1);
			}
		} else if (fileInfoField.equals(MetadataField.LOCATION.getType())) {
			if (fileInfo.getLocation().equals(info)) {
				int count = map.containsKey(info) ? map.get(info) : 0;
				map.put(info, count + 1);
			}
		} else if (fileInfoField.equals(MetadataField.CAMERA.getType())) {
			if (fileInfo.getCamera_model().equals(info)) {
				int count = map.containsKey(info) ? map.get(info) : 0;
				map.put(info, count + 1);
			}
		} else if (fileInfoField.equals(MetadataField.DATE.getType())) {
			if (simpleDates.getSdf_ymd_minus().format(fileInfo.getDate()).equals(info)) {
				int count = map.containsKey(info) ? map.get(info) : 0;
				map.put(info, count + 1);
			}

		}

	}

	/**
	 * 
	 * @param exifInfo_list
	 * @param fi
	 * @param fileInfoFieldexifInfo_list
	 */
	private void fileInfoField(List<String> exifInfo_list, FileInfo fi, String fileInfoFieldexifInfo_list) {
		if (fileInfoFieldexifInfo_list.equals(MetadataField.EVENT.getType())) {
			if (!fi.getEvent().isEmpty()) {
				if (!hasValue(fi.getEvent(), exifInfo_list)) {
					exifInfo_list.add(fi.getEvent());
				}
			}
		} else if (fileInfoFieldexifInfo_list.equals(MetadataField.LOCATION.getType())) {
			if (!fi.getLocation().isEmpty()) {
				if (!hasValue(fi.getLocation(), exifInfo_list)) {
					exifInfo_list.add(fi.getLocation());
				}
			}
		} else if (fileInfoFieldexifInfo_list.equals(MetadataField.CAMERA.getType())) {
			if (fi.getCamera_model() == null) {
				fi.setCamera_model(MetadataField.UNKNOWN.getType());
			}
			if (fi.getCamera_model().length() == 0 || fi.getCamera_model().isEmpty()) {
				fi.setCamera_model(MetadataField.UNKNOWN.getType());
			}
			if (!hasValue(fi.getCamera_model(), exifInfo_list)) {
				exifInfo_list.add(fi.getCamera_model());
			}
		} else if (fileInfoFieldexifInfo_list.equals(MetadataField.DATE.getType())) {
			if (!hasValue(simpleDates.getSdf_ymd_minus().format(fi.getDate()), exifInfo_list)) {
				exifInfo_list.add(simpleDates.getSdf_ymd_minus().format(fi.getDate()));
			}
		}
	}

	public void createEvent_list(List<FileInfo> fileInfo_list) {
		List<Map<String, Integer>> list_map = new ArrayList<>();

		List<String> events_list = new ArrayList<>();
		for (FileInfo fi : fileInfo_list) {
			if (!fi.getEvent().isEmpty()) {
				if (!hasValue(fi.getEvent(), events_list)) {
					events_list.add(fi.getEvent());
				}
			}
		}
		if (events_list.isEmpty()) {
			return;
		}
		Collections.sort(events_list);
		for (String events : events_list) {
			Map<String, Integer> map = findHashMap(events, list_map);
			for (FileInfo fi : fileInfo_list) {
				if (fi.getEvent().equals(events)) {
					int count = map.containsKey(events) ? map.get(events) : 0;
					map.put(events, count + 1);
				}
			}
		}

		for (Map<String, Integer> entry : list_map) {
			for (Map.Entry<String, Integer> entry2 : entry.entrySet()) {
				events_obs.add(new EXIF_Data_Selector(false, entry2.getKey(), entry2.getValue()));
			}
		}

	}

	public void createLocation_list(List<FileInfo> fileInfo_list) {
		List<Map<String, Integer>> list_map = new ArrayList<>();
		List<String> locations_list = new ArrayList<>();
		for (FileInfo fi : fileInfo_list) {
			if (!fi.getLocation().isEmpty()) {
				if (!hasValue(fi.getLocation(), locations_list)) {
					locations_list.add(fi.getLocation());
				}
			}
		}
		if (locations_list.isEmpty()) {
			return;
		}
		Collections.sort(locations_list);
		for (String locations : locations_list) {
			Map<String, Integer> map = findHashMap(locations, list_map);
			for (FileInfo fi : fileInfo_list) {
				if (fi.getLocation().equals(locations)) {
					int count = map.containsKey(locations) ? map.get(locations) : 0;
					map.put(locations, count + 1);
				}
			}
		}

		for (Map<String, Integer> entry : list_map) {
			sprintf("Listing map: " + entry.size());
			for (Map.Entry<String, Integer> entry2 : entry.entrySet()) {
				locations_obs.add(new EXIF_Data_Selector(false, entry2.getKey(), entry2.getValue()));
			}
		}
	}

	/**
	 * @return the events_obs
	 */
	public final ObservableList<EXIF_Data_Selector> getEvents_obs() {
		return events_obs;
	}


	/**
	 * @return the locations_obs
	 */
	public final ObservableList<EXIF_Data_Selector> getLocations_obs() {
		return locations_obs;
	}


}
