package com.thriftyApp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link BootReceiver}'s pure action check. */
@RunWith(RobolectricTestRunner.class)
@Config(application = ThriftyApp.class)
public class BootReceiverTest {

    @Test
    public void recognizesBootCompletedAction() {
        assertTrue(BootReceiver.isBootCompleted(new Intent(Intent.ACTION_BOOT_COMPLETED)));
    }

    @Test
    public void rejectsOtherActions() {
        assertFalse(BootReceiver.isBootCompleted(new Intent(Intent.ACTION_VIEW)));
    }

    @Test
    public void rejectsActionlessIntent() {
        assertFalse(BootReceiver.isBootCompleted(new Intent()));
    }

    @Test
    public void rejectsNullIntent() {
        assertFalse(BootReceiver.isBootCompleted(null));
    }
}
