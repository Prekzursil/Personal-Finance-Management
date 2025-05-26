package com.thriftyApp;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class ThriftyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply saved theme
        String themePref = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("pref_theme", "system");
        ThemeUtils.applyTheme(themePref);

        // Apply saved language
        String lang = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("pref_language", "system");
        // LocaleUtils.initSystemLanguage(this); // This line is removed
        LocaleUtils.applyLocale(this, lang);
    }
}
