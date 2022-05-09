package common.utils.date;

import static com.girbola.Main.simpleDates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.fileinfo.FileInfo;

public class CreateDateList {

	public static void addToDateMap(Map<String, List<FileInfo>> map, List<FileInfo> fileInfoList, TableType tableType) {
		List<String> dateMap = new ArrayList<>();
		for (FileInfo fi : fileInfoList) {
			if (!dateMap.contains(simpleDates.getSdf_ymd_minus().format(fi.getDate()))) {
				dateMap.add(simpleDates.getSdf_ymd_minus().format(fi.getDate()));
			}
		}
		for (String d : dateMap) {
			List<FileInfo> list = findList(d, fileInfoList);
			map.put(d, list);
		}
	}

	private static List<FileInfo> findList(String d, List<FileInfo> list) {
		List<FileInfo> theList = new ArrayList<>();
		for (FileInfo fi : list) {
			if (simpleDates.getSdf_ymd_minus().format(fi.getDate()).equals(d)) {
				theList.add(fi);
			}
		}
		return theList;
	}
}
