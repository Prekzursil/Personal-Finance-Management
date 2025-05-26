package com.thriftyApp;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import android.content.Context;

public class ThemeUtils {

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    public static void applyTheme(String pref) {
        switch (pref) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static void applyTheme(Context ctx) {
        String pref = PreferenceManager.getDefaultSharedPreferences(ctx)
                .getString("pref_theme", THEME_SYSTEM);
        applyTheme(pref);
    }
}