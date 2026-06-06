package com.thriftyApp;

import java.util.regex.Pattern;

public class Utils {

	// Email validation pattern.
	//
	// The previous expression used "[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}" for the
	// domain. Because the character class "[A-Za-z0-9.-]" itself contains a
	// dot and was immediately followed by a literal "\\." the two overlapped,
	// which let the regex engine backtrack quadratically on attacker-controlled
	// input (CodeQL java/polynomial-redos). The domain is now expressed as a
	// sequence of dot-delimited labels whose label class excludes the dot, so
	// there is no overlapping ambiguity and matching runs in linear time.
	// The pattern is anchored (matched with Matcher#matches) so the whole input
	// must be a valid address rather than merely containing one.
	public static final String regEx =
			"^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}$";

	private static final Pattern EMAIL_PATTERN = Pattern.compile(regEx);

	private Utils() {
		// Utility holder; not meant to be instantiated.
	}

	/**
	 * Validates an email address against {@link #regEx}.
	 *
	 * <p>Centralising the check means both the sign-up and login screens share
	 * a single, ReDoS-safe implementation. A {@code null} input is treated as
	 * invalid.</p>
	 *
	 * @param email the candidate email address (may be {@code null})
	 * @return {@code true} if the value is a syntactically valid email address
	 */
	public static boolean isValidEmail(String email) {
		return email != null && EMAIL_PATTERN.matcher(email).matches();
	}

	//Fragments Tags
	public static final String Login_Fragment = "Login_Fragment";
	public static final String SignUp_Fragment = "SignUp_Fragment";
	public static String userId = "0";
	public static String budget = "0";
	public static String userName = "";
	public static int expense = 0;
	public static int income= 0;
	public static int pdfNumber = 1;
}
