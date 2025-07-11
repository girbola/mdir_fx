
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.filelisting.SubFolders;
import com.girbola.filelisting.ValidatePathUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import common.utils.FileUtils;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.girbola.messages.Messages.sprintf;


public class SubList extends Task<List<Path>> {
	private final String ERROR = SubList.class.getSimpleName();

	private static List<Path> list = new ArrayList<>();
	private List<Path> selectedFolderScanner_list;

	public SubList(List<Path> selectedFolderScanner_list) {
		this.selectedFolderScanner_list = selectedFolderScanner_list;
	}

	private static void calculate(Path p) throws IOException {
		if(Files.isReadable(p)) {
			sprintf("IS Readable. SubList - calculate: " + p);
		}
		SubFolders subFolders = new SubFolders();
		List<Path> list = SubFolders.subFolders(p);

//		DirectoryStream<Path> ds = FileUtils.createDirectoryStream(p, FileUtils.filter_directories);
//		if(ds == null) {
//			Messages.sprintfError("Calculate has failed. Cannot read folder: " + p);
//		}
		for (Path path : list) {
			if(Main.getProcessCancelled()) {
				break;
			}
			if (ValidatePathUtils.validFolder(path)) {
				sprintf("----calculating: " + path);
				if (!SubList.list.contains(path)) {
					SubList.list.add(path);
					calculate(path);
				}
			}
		}
	}

	@Override
	protected List<Path> call() throws Exception {
		for (Path p : selectedFolderScanner_list) {
			Messages.sprintf("PATHHHTHTH: " + p.toString());
			if(Main.getProcessCancelled()) {
				break;
			}
			if (ValidatePathUtils.hasMediaFilesInFolder(p)) {
				list.add(p);
			}
			try {
				calculate(p);
			} catch (IOException ex) {
				Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
		}
		return list;
	}
}
