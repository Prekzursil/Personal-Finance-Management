package com.thriftyApp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ThemeUtils} covering every theme branch. */
@RunWith(RobolectricTestRunner.class)
@Config(application = ThriftyApp.class)
public class ThemeUtilsTest {

    @Test
    public void lightThemeSetsNightModeNo() {
        ThemeUtils.applyTheme(ThemeUtils.THEME_LIGHT);
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode());
    }

    @Test
    public void darkThemeSetsNightModeYes() {
        ThemeUtils.applyTheme(ThemeUtils.THEME_DARK);
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode());
    }

    @Test
    public void systemThemeFollowsSystem() {
        ThemeUtils.applyTheme(ThemeUtils.THEME_SYSTEM);
        assertEquals(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.getDefaultNightMode());
    }

    @Test
    public void unknownThemeFallsThroughToSystemDefault() {
        ThemeUtils.applyTheme("unknown-value");
        assertEquals(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.getDefaultNightMode());
    }

    @Test
    public void contextOverloadReadsPreferenceAndApplies() {
        ThemeUtils.applyTheme(ApplicationProvider.getApplicationContext());
        // Default preference is THEME_SYSTEM, so night mode follows the system.
        assertEquals(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.getDefaultNightMode());
    }

    @Test
    public void themeUtilsIsInstantiable() {
        assertNotNull(new ThemeUtils());
    }
}
