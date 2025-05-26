package com.thriftyApp;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply selected or system language before context is used
        String lang = PreferenceManager.getDefaultSharedPreferences(newBase)
                .getString("pref_language", "system");
        LocaleUtils.applyLocale(newBase, lang);
        super.attachBaseContext(newBase);
    }
}
