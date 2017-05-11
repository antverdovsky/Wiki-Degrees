package com.antverdovsky.wikideg.util;

public class Logger {
	private static boolean isEnabled = false;
	
	/**
	 * Sets a boolean value indicating whether the logger is on or off.
	 * @param b True if the logger is to be turned on. False if the logger is
	 *          to be turned off.
	 */
	public static void setIsEnabled(boolean b) {
		Logger.isEnabled = b;
	}
	
	/**
	 * Returns a boolean value indicating whether the logger is on or off.
	 * @return True if the logger is turned on. False otherwise.
	 */
	public static boolean getIsEnabled() {
		return Logger.isEnabled;
	}
	
	/**
	 * Logs the specified string message to the console, if the logger is 
	 * enabled. If the logger is disabled, no change occurs. 
	 * @param s The string to be logged.
	 */
	public static void log(String s) {
		if (Logger.isEnabled) System.out.print(s);
	}
	
	/**
	 * Logs the specified string message to the console and adds a new line
	 * character, if the logger is enabled. If the logger is disabled, no 
	 * change occurs. 
	 * @param s The string to be logged.
	 */
	public static void logLine(String s) {
		if (Logger.isEnabled) System.out.println(s);
	}
}
