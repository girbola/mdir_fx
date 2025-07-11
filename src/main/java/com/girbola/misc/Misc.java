
package com.girbola.misc;

import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.messages.html.HTMLClass;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.warningText;

/**
 *
 * @author Marko
 */
public class Misc {

	private static final String ERROR = Misc.class.getSimpleName();

	public static String getCurrentOs() {
		return System.getProperty("os.name");
	}

	public static boolean checkOS() {
		String os = getCurrentOs().toLowerCase();
		Messages.sprintf("OS is: " + os);
		if (os.contains("win") || os.contains("linux") || os.contains("mac")) {
			Path configPath = Paths.get(System.getProperty("user.home") + File.separator + ".mdir");
			try {
				Files.createDirectories(configPath);
				Main.conf.setAppDataPath(Paths.get(System.getProperty("user.home") + File.separator + ".mdir"));
			} catch (IOException e) {
				Messages.warningText(Main.bundle.getString("cannotCreateConfigFile"));
				e.printStackTrace();
			}
			
			return true;
		} else {
			Main.setProcessCancelled(true);
			Messages.errorSmth(ERROR, bundle.getString("osNotSupported") + "\n\n" + bundle.getString("homePage") + ": "
					+ HTMLClass.programHomePage, null, Misc.getLineNumber(), true);
			return false;
		}
	}

	public static boolean isUnix() {
		if (System.getProperty("os.name").toLowerCase().contains("nix")
				|| System.getProperty("os.name").toLowerCase().contains("linux")) {
			return true;
		}
		return false;
	}

	public static boolean isMac() {
		if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			return true;
		}
		return false;
	}

//Tee merge & move tools joka hakee vasemmalta Sortit päivät Sortediin
	public static boolean isWindows() {
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param linenumber
	 */
	public static void showMemInfo(int linenumber) {
		long left = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long freeMemTotal = ((getFreeMem() - Runtime.getRuntime().freeMemory()) / 1024);
		warningText("Memory test: " + "\nTotalMemory: " + Runtime.getRuntime().totalMemory() + "\nFree memory: "
				+ Runtime.getRuntime().freeMemory() + "\nTotal availableProcessors: "
				+ Runtime.getRuntime().availableProcessors() + "\nMemory Left = " + left + "\nMem used: " + freeMemTotal
				+ "\n\nLine number: " + getLineNumber());
	}

	public static int getLineNumber() {
		return Thread.currentThread().getStackTrace()[2].getLineNumber();
	}

	public static long getTotalMem() {
		return Runtime.getRuntime().totalMemory();
	}

	public static long getFreeMem() {
		return Runtime.getRuntime().freeMemory();
	}

	/**
	 * Save Object to file
	 *
	 * @param obj
	 * @param folder
	 * @throws IOException
	 */
	public static void saveObject(Object obj, File folder) throws IOException {
		Messages.sprintf("foldername is: " + folder);
		try {
			FileOutputStream fileOut = new FileOutputStream(folder);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(obj);
			objectOut.close();

			Messages.sprintf("The Object  was succesfully written to a file");
		} catch (Exception ex) {
			Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
		}

	}

	/**
	 * Load from file to Object
	 *
	 * @param folder
	 * @return
	 * @throws IOException
	 */
	public static Object loadObject(File folder) throws IOException {
		Object obj = null;
		FileInputStream fis = new FileInputStream(folder);
		ObjectInputStream ois = new ObjectInputStream(fis);

		try {
			obj = ois.readObject();
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			ois.close();
		}
		
		return obj;
	}

	public  static void openFileBrowser(Path path) {
		String os = System.getProperty("os.name").toLowerCase();

		try {
			if (os.contains("win")) {
				// Windows
				new ProcessBuilder("explorer.exe", "/select,", path.toString()).start();
			} else if (os.contains("mac")) {
				// macOS
				new ProcessBuilder("open", "-R", path.toString()).start();
			} else if (os.contains("nix") || os.contains("nux")) {
				// Linux
				new ProcessBuilder("xdg-open", path.toString()).start();
			} else {
				Messages.sprintfError("Unsupported operating system.");
			}
		} catch (IOException e) {
			Messages.sprintfError("Cannot open current file in operating file browser. " + path);
		}
	}

	public static Rectangle2D getScreenBounds() {
		Messages.sprintf("getScreenBounds started");
		ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(
				Main.scene_Switcher.getScene_main().getX(), Main.scene_Switcher.getScene_main().getY(),
				Main.scene_Switcher.getScene_main().getWidth(), Main.scene_Switcher.getScene_main().getHeight());
		return screensForRectangle.get(0).getBounds();
//			return Screen.getPrimary().getBounds();
	}
}
