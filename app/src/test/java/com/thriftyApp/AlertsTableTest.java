package com.thriftyApp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** Unit tests for the {@link AlertsTable} data holder. */
public class AlertsTableTest {

    @Test
    public void gettersReturnValuesSetBySetters() {
        AlertsTable a = new AlertsTable();
        a.setAid(5);
        a.setUid(11);
        a.setMessage("Pay rent");
        a.setalert_at("2026-06-01 09:00");

        assertEquals(5, a.getAid());
        assertEquals(11, a.getUid());
        assertEquals("Pay rent", a.getMessage());
        assertEquals("2026-06-01 09:00", a.getalert_at());
    }

    @Test
    public void defaultsAreZeroAndNull() {
        AlertsTable a = new AlertsTable();
        assertEquals(0, a.getAid());
        assertEquals(0, a.getUid());
        assertNull(a.getMessage());
        assertNull(a.getalert_at());
    }
}
