package com.thriftyApp;

public class Utils {

	// Email validation pattern.
	// Anchored and delimiter-separated so each quantified group consumes a
	// distinct character class (no overlap between the domain labels and the
	// '.' separators). This avoids the polynomial-backtracking (ReDoS) risk of
	// the previous unanchored "[A-Za-z0-9.-]+\.[A-Za-z]{2,4}" pattern.
	public static final String regEx = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$";

	//Fragments Tags
	public static final String Login_Fragment = "Login_Fragment";
	public static final String SignUp_Fragment = "SignUp_Fragment";
	public static String userId = "0";
	public static String budget = "0";
	public static String userName = "";
	public static int expense = 0;
	public static int income= 0;
	public static int pdfNumber = 1;

	/**
	 * Parse an {@code int} without throwing on malformed input. Returns
	 * {@code defaultValue} when {@code value} is null or not a valid number,
	 * so corrupted persisted data degrades gracefully instead of crashing.
	 */
	public static int safeParseInt(String value, int defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/** Parse a {@code long} without throwing on malformed input. */
	public static long safeParseLong(String value, long defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
}
