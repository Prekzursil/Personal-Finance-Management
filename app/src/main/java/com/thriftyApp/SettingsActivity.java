package com.thriftyApp;

import android.os.Bundle;
import com.thriftyApp.BaseActivity;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.core.app.NavUtils;
import androidx.preference.PreferenceManager;
import com.thriftyApp.LocaleUtils;

public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        // apply language locale before inflation
        String lang = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("pref_language", "system");
        LocaleUtils.applyLocale(this, lang);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        }
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // ensure localized title
        getSupportActionBar().setTitle(R.string.options);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }
}
