package com.thriftyApp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;
import org.junit.Test;

/** Unit tests for {@link Utils} constants and mutable state. */
public class UtilsTest {

    @Test
    public void emailRegexAcceptsValidAndRejectsInvalid() {
        Pattern p = Pattern.compile(Utils.regEx);
        assertTrue(p.matcher("user.name+tag@example.com").find());
        assertFalse(p.matcher("not-an-email").find());
    }

    @Test
    public void fragmentTagsAreStable() {
        assertEquals("Login_Fragment", Utils.Login_Fragment);
        assertEquals("SignUp_Fragment", Utils.SignUp_Fragment);
    }

    @Test
    public void mutableStateCanBeUpdated() {
        Utils.userId = "99";
        Utils.budget = "500";
        Utils.userName = "Ada";
        Utils.expense = 120;
        Utils.income = 800;
        Utils.pdfNumber = 4;

        assertEquals("99", Utils.userId);
        assertEquals("500", Utils.budget);
        assertEquals("Ada", Utils.userName);
        assertEquals(120, Utils.expense);
        assertEquals(800, Utils.income);
        assertEquals(4, Utils.pdfNumber);
    }

    @Test
    public void utilsIsInstantiable() {
        // The class exposes only static members but has an implicit constructor.
        org.junit.Assert.assertNotNull(new Utils());
    }
}
