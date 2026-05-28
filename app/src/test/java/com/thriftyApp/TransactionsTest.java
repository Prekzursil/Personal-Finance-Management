package com.thriftyApp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Unit tests for the {@link Transactions} data holder. */
public class TransactionsTest {

    @Test
    public void gettersReturnValuesSetBySetters() {
        Transactions t = new Transactions();
        t.setTid(7);
        t.setUid(42);
        t.setExin(1);
        t.setAmount(1500L);
        t.setTag("Food");
        t.setCreated_at("2026-05-28");

        assertEquals(7, t.getTid());
        assertEquals(42, t.getUid());
        assertEquals(1, t.getExin());
        assertEquals(1500L, t.getAmount());
        assertEquals("Food", t.getTag());
        assertEquals("2026-05-28", t.getCreated_at());
    }

    @Test
    public void defaultsAreZeroAndNull() {
        Transactions t = new Transactions();
        assertEquals(0, t.getTid());
        assertEquals(0, t.getUid());
        assertEquals(0, t.getExin());
        assertEquals(0L, t.getAmount());
        org.junit.Assert.assertNull(t.getTag());
        org.junit.Assert.assertNull(t.getCreated_at());
    }
}
