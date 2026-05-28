package com.thriftyApp;

import java.util.regex.Pattern;

/**
 * Pure, framework-free validation for the sign-up form.
 *
 * <p>Extracting the rules out of {@code SignUp_Fragment} keeps the fragment's
 * {@code checkValidation} method at a low cyclomatic complexity and makes the
 * rules unit-testable without an Android context.
 */
public final class SignUpValidator {

    /** Outcome of validating the sign-up form. */
    public enum Result {
        VALID,
        MISSING_FIELDS,
        INVALID_EMAIL,
        PASSWORD_MISMATCH,
        TERMS_NOT_ACCEPTED
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(Utils.regEx);

    private SignUpValidator() {
        // Utility class: no instances.
    }

    private static boolean anyBlank(String... values) {
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates the sign-up inputs in priority order.
     *
     * @return the first failing {@link Result}, or {@link Result#VALID}
     */
    public static Result validate(String fullName,
                                  String email,
                                  String mobileNumber,
                                  String budget,
                                  String password,
                                  String confirmPassword,
                                  boolean termsAccepted) {
        if (anyBlank(fullName, email, mobileNumber, budget, password, confirmPassword)) {
            return Result.MISSING_FIELDS;
        }
        if (!EMAIL_PATTERN.matcher(email).find()) {
            return Result.INVALID_EMAIL;
        }
        if (!confirmPassword.equals(password)) {
            return Result.PASSWORD_MISMATCH;
        }
        if (!termsAccepted) {
            return Result.TERMS_NOT_ACCEPTED;
        }
        return Result.VALID;
    }
}
