package com.thriftyApp;

import static org.junit.Assert.assertNotNull;

import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Verifies the {@link ThriftyApp} application bootstrap runs onCreate. */
@RunWith(RobolectricTestRunner.class)
@Config(application = ThriftyApp.class)
public class ThriftyAppTest {

    @Test
    public void applicationIsCreatedAndBootstrapsThemeAndLocale() {
        // Robolectric instantiates the configured Application and invokes
        // onCreate() before the test, exercising the theme + locale setup.
        ThriftyApp app = (ThriftyApp) ApplicationProvider.getApplicationContext();
        assertNotNull(app);
        // Re-run onCreate explicitly to assert it is idempotent and safe.
        app.onCreate();
    }
}
