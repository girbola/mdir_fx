/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.utils;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.misc.Misc;

/**
 *
 * @author Marko Lokka
 */
public class ArrayUtils {

	private final static String ERROR = ArrayUtils.class.getSimpleName();

	public static boolean duplicateCheck(List<Path> selectedScanFolders_data_tmp2, Path f) {
		for (Path fc : selectedScanFolders_data_tmp2) {
			if (fc.compareTo(f) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reads file to List
	 *
	 * @param path
	 * @return
	 */
	public static List<Path> readFileToArray(Path path) {

		if (!Files.exists(path)) {
			sprintf("Cannot read file: " + path);
			return new ArrayList<>();
		}
		List<Path> arrayList = new ArrayList<>();

		try {
			if (Files.exists(path) && Files.size(path) != 0) {
				readFile(path, arrayList);
			}
		} catch (IOException ex) {
			Logger.getLogger(ArrayUtils.class.getName()).log(Level.SEVERE, null, ex);
			errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}
		return arrayList;
	}

	private static void readFile(Path file, List<Path> arrayList) {
		BufferedReader br = null;

		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(file.toFile()));

			while ((sCurrentLine = br.readLine()) != null) {
				arrayList.add(Paths.get(sCurrentLine));
			}
		} catch (IOException ex) {
			Logger.getLogger(ArrayUtils.class.getName()).log(Level.SEVERE, null, ex);
			errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				Logger.getLogger(ArrayUtils.class.getName()).log(Level.SEVERE, null, ex);
				errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
			}
		}
	}

	public static void saveList(List<Path> list, Path path) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileOutputStream(path.toFile()));
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ArrayUtils.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			for (Path str : list) {
				if (str != null) {
					pw.println(str.toString());
				}
			}
			if (pw != null) {
				pw.close();
			}

		}
	}

}
