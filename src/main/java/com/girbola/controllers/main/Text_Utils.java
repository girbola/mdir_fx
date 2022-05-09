package com.girbola.controllers.main;

import java.util.ArrayList;
import java.util.List;

public class Text_Utils {
	// /  : + * ?  < > |
	private static List<String> notValidFileNameCharaters = new ArrayList<>() {
		{
			add("/");
			add(":");
			add("+");
			add("*");
			add("?");
			add("<");
			add(">");
		}
	};

	/**
	 * Check if string is valid file&foldername
	 * @param value
	 * @return false if not valid otherwise true if is valid
	 */
	public static boolean isValidFileOrFolderName(String value) {

		for(String notValid : notValidFileNameCharaters) {
			if(value.equals(notValid)) {
				return false;
			}
		}
		return true;
	}
}
