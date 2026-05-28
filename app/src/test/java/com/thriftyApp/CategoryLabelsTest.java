package com.thriftyApp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for the {@link CategoryLabels} lookup table. */
@RunWith(RobolectricTestRunner.class)
@Config(application = ThriftyApp.class)
public class CategoryLabelsTest {

    @Test
    public void knownCodesResolveToNonZeroResourceIds() {
        assertEquals(R.string.cat_food, CategoryLabels.resourceIdFor("Food"));
        assertEquals(R.string.cat_transport, CategoryLabels.resourceIdFor("Transport"));
        assertEquals(R.string.cat_movie, CategoryLabels.resourceIdFor("Movie"));
        assertNotEquals(0, CategoryLabels.resourceIdFor("Healthcare"));
    }

    @Test
    public void unknownCodeResolvesToZero() {
        assertEquals(0, CategoryLabels.resourceIdFor("Nonexistent"));
        assertEquals(0, CategoryLabels.resourceIdFor(null));
    }
}
