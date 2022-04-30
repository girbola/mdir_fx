/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.misc;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.warningText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.messages.html.HTMLClass;

/**
 *
 * @author Marko
 */
public class Misc {

	private static final String ERROR = Misc.class.getSimpleName();

	public static boolean checkOS() {
		String os = System.getProperty("os.name").toLowerCase();
		Messages.sprintf("OS is: " + os);
		if (os.contains("win") || os.contains("linux")) {
			// setNative_Library_Search_Path("C:\\Program Files\\");
			return true;
		} else {
			Main.setProcessCancelled(true);
			Messages.errorSmth(ERROR, bundle.getString("osNotSupported") + "\n\n" + bundle.getString("homePage") + ": "
					+ HTMLClass.programHomePage, null, Misc.getLineNumber(), true);
			return false;
		}
	}

	public static boolean isUnix() {
		if (System.getProperty("os.name").toLowerCase().contains("nix")) {
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
		System.out.println("foldername is: " + folder);
		try {
			FileOutputStream fileOut = new FileOutputStream(folder);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(obj);
			objectOut.close();

			System.out.println("The Object  was succesfully written to a file");
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
		}
		return obj;
	}
}
