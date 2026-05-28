package com.thriftyApp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
import java.util.Locale;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Covers the locale-resolution branches in {@link LocaleUtils}. */
@RunWith(RobolectricTestRunner.class)
@Config(application = ThriftyApp.class)
public class LocaleUtilsTest {

    private final Context ctx = ApplicationProvider.getApplicationContext();

    @After
    public void restoreDefaultLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void explicitRomanianIsApplied() {
        LocaleUtils.applyLocale(ctx, "ro");
        assertEquals("ro", Locale.getDefault().getLanguage());
    }

    @Test
    public void explicitEnglishIsApplied() {
        LocaleUtils.applyLocale(ctx, "en");
        assertEquals("en", Locale.getDefault().getLanguage());
    }

    @Test
    public void invalidPreferenceFallsBackToEnglish() {
        LocaleUtils.applyLocale(ctx, "xx");
        assertEquals("en", Locale.getDefault().getLanguage());
    }

    @Test
    public void systemPreferenceResolvesToSupportedLanguage() {
        LocaleUtils.applyLocale(ctx, "system");
        // System default in Robolectric is en-US, which is not Romanian, so it
        // falls back to English.
        assertEquals("en", Locale.getDefault().getLanguage());
    }

    @Test
    public void getResolvedLocaleReadsPreferenceRomanian() {
        PreferenceManager.getDefaultSharedPreferences(ctx)
                .edit().putString("pref_language", "ro").commit();
        assertEquals("ro", LocaleUtils.getResolvedLocale(ctx).getLanguage());
    }

    @Test
    public void getResolvedLocaleReadsPreferenceEnglish() {
        PreferenceManager.getDefaultSharedPreferences(ctx)
                .edit().putString("pref_language", "en").commit();
        assertEquals("en", LocaleUtils.getResolvedLocale(ctx).getLanguage());
    }

    @Test
    public void getResolvedLocaleSystemFallsBack() {
        PreferenceManager.getDefaultSharedPreferences(ctx)
                .edit().putString("pref_language", "system").commit();
        assertEquals("en", LocaleUtils.getResolvedLocale(ctx).getLanguage());
    }

    @Test
    public void getResolvedLocaleInvalidFallsBack() {
        PreferenceManager.getDefaultSharedPreferences(ctx)
                .edit().putString("pref_language", "zz").commit();
        assertEquals("en", LocaleUtils.getResolvedLocale(ctx).getLanguage());
    }

    @Test
    public void localeUtilsIsInstantiable() {
        assertNotNull(new LocaleUtils());
    }
}
