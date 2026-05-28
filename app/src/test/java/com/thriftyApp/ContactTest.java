package com.thriftyApp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** Unit tests for the {@link Contact} data holder. */
public class ContactTest {

    @Test
    public void gettersReturnValuesSetBySetters() {
        Contact c = new Contact();
        c.setId(3);
        c.setName("Ada");
        c.setEmailId("ada@example.com");
        c.setPassword("secret");
        c.setMobile(1234567890L);
        c.setBudget(9999L);

        assertEquals(3, c.getId());
        assertEquals("Ada", c.getName());
        assertEquals("ada@example.com", c.getEmailId());
        assertEquals("secret", c.getPassword());
        assertEquals(1234567890L, c.getMobile());
        assertEquals(9999L, c.getBudget());
    }

    @Test
    public void defaultsAreZeroAndNull() {
        Contact c = new Contact();
        assertEquals(0, c.getId());
        assertEquals(0L, c.getMobile());
        assertEquals(0L, c.getBudget());
        assertNull(c.getName());
        assertNull(c.getEmailId());
        assertNull(c.getPassword());
    }
}
