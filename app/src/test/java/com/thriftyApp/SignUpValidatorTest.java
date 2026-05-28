package com.thriftyApp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Unit tests for the pure {@link SignUpValidator} rules. */
public class SignUpValidatorTest {

    private static final String NAME = "Ada";
    private static final String EMAIL = "ada@example.com";
    private static final String MOBILE = "5550000";
    private static final String BUDGET = "1000";
    private static final String PASSWORD = "secret";

    @Test
    public void validWhenAllFieldsAreCorrect() {
        assertEquals(SignUpValidator.Result.VALID,
                SignUpValidator.validate(NAME, EMAIL, MOBILE, BUDGET, PASSWORD, PASSWORD, true));
    }

    @Test
    public void missingFieldsWhenAnyValueIsBlank() {
        assertEquals(SignUpValidator.Result.MISSING_FIELDS,
                SignUpValidator.validate("", EMAIL, MOBILE, BUDGET, PASSWORD, PASSWORD, true));
        assertEquals(SignUpValidator.Result.MISSING_FIELDS,
                SignUpValidator.validate(NAME, EMAIL, MOBILE, BUDGET, PASSWORD, null, true));
    }

    @Test
    public void invalidEmailIsRejected() {
        assertEquals(SignUpValidator.Result.INVALID_EMAIL,
                SignUpValidator.validate(NAME, "not-an-email", MOBILE, BUDGET, PASSWORD, PASSWORD, true));
    }

    @Test
    public void passwordMismatchIsRejected() {
        assertEquals(SignUpValidator.Result.PASSWORD_MISMATCH,
                SignUpValidator.validate(NAME, EMAIL, MOBILE, BUDGET, PASSWORD, "different", true));
    }

    @Test
    public void termsMustBeAccepted() {
        assertEquals(SignUpValidator.Result.TERMS_NOT_ACCEPTED,
                SignUpValidator.validate(NAME, EMAIL, MOBILE, BUDGET, PASSWORD, PASSWORD, false));
    }

    @Test
    public void enumValuesAreStable() {
        // Exercises the synthetic values()/valueOf() methods for coverage.
        assertEquals(SignUpValidator.Result.VALID,
                SignUpValidator.Result.valueOf("VALID"));
        assertEquals(5, SignUpValidator.Result.values().length);
    }
}
