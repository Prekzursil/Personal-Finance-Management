package com.thriftyApp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests for the pure {@link ReminderInput} checks. */
public class ReminderInputTest {

    @Test
    public void allPresentWhenEveryValueIsNonEmpty() {
        assertTrue(ReminderInput.allPresent("message", "01/01/2030", "09:00"));
    }

    @Test
    public void notPresentWhenAnyValueIsEmpty() {
        assertFalse(ReminderInput.allPresent("message", "", "09:00"));
    }

    @Test
    public void notPresentWhenAnyValueIsNull() {
        assertFalse(ReminderInput.allPresent("message", "01/01/2030", null));
    }

    @Test
    public void allPresentWithNoArgumentsIsTrue() {
        assertTrue(ReminderInput.allPresent());
    }
}
