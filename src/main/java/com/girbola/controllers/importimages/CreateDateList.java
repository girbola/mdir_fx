
package com.girbola.controllers.importimages;

import com.girbola.fileinfo.FileInfo;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.girbola.Main.simpleDates;

/**
 *	Returns a map 2020-08-29
 */
public class CreateDateList extends Task<Map<String, List<FileInfo>>> {

	private Path path;
	private List<FileInfo> list;
	private Map<String, List<FileInfo>> map = new HashMap<>();

	public CreateDateList(List<FileInfo> aList) {
		this.list = aList;
	}

	@Override
	protected Map<String, List<FileInfo>> call() throws Exception {
		List<String> dateMap = new ArrayList<>();

		for (FileInfo fi : list) {
			if (!dateMap.contains(simpleDates.getSdf_ymd_minus().format(fi.getDate()))) {
				dateMap.add(simpleDates.getSdf_ymd_minus().format(fi.getDate()));
			}
		}
		for (String d : dateMap) {
			List<FileInfo> list = findList(d, this.list);
			map.put(d, list);
		}
		return map;
	}

	private List<FileInfo> findList(String d, List<FileInfo> list) {
		List<FileInfo> theList = new ArrayList<>();
		for (FileInfo fi : list) {
			if (simpleDates.getSdf_ymd_minus().format(fi.getDate()).equals(d)) {
				theList.add(fi);
			}
		}
		return theList;
	}

}
