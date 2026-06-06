package com.thriftyApp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link Utils#isValidEmail(String)}.
 *
 * <p>These run on the JVM under {@code testDebugUnitTest} (no Android
 * framework dependency), so they are exercised by JaCoCo coverage.</p>
 */
public class UtilsTest {

    @Test
    public void acceptsTypicalAddresses() {
        assertTrue(Utils.isValidEmail("test@example.com"));
        assertTrue(Utils.isValidEmail("a.b+c@sub.domain.co"));
        assertTrue(Utils.isValidEmail("user_name@my-host.org"));
        assertTrue(Utils.isValidEmail("first.last%tag@a.b.info"));
    }

    @Test
    public void rejectsInvalidAddresses() {
        assertFalse(Utils.isValidEmail(""));
        assertFalse(Utils.isValidEmail("notanemail"));
        assertFalse(Utils.isValidEmail("a@@b.com"));
        assertFalse(Utils.isValidEmail("missing-domain@"));
        assertFalse(Utils.isValidEmail("@no-local.com"));
        assertFalse(Utils.isValidEmail("no-tld@example"));
        // Anchoring: a value that merely contains an address is not accepted.
        assertFalse(Utils.isValidEmail("   test@example.com   "));
        assertFalse(Utils.isValidEmail("prefix test@example.com suffix"));
    }

    @Test
    public void nullIsInvalid() {
        assertFalse(Utils.isValidEmail(null));
    }

    /**
     * Regression guard for CodeQL java/polynomial-redos.
     *
     * <p>The previous pattern backtracked quadratically on a long run of
     * characters that could begin a match but never complete one. With the
     * dot-delimited, non-overlapping pattern this completes in linear time.
     * A generous wall-clock bound (well under a second) would still be blown
     * away by the old super-linear behaviour, so a regression would fail the
     * build rather than merely run slowly.</p>
     */
    @Test(timeout = 2000)
    public void rejectsPathologicalInputInLinearTime() {
        StringBuilder attack = new StringBuilder();
        for (int i = 0; i < 100_000; i++) {
            attack.append('b');
        }
        attack.append('!');

        long start = System.nanoTime();
        boolean result = Utils.isValidEmail(attack.toString());
        long elapsedMs = (System.nanoTime() - start) / 1_000_000L;

        assertFalse(result);
        assertTrue("Email validation should be linear-time; took " + elapsedMs + "ms",
                elapsedMs < 1000);
    }
}
