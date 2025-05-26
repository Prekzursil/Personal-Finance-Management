package com.thriftyApp;

import android.content.Context;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import androidx.preference.PreferenceManager; // Import PreferenceManager
// import android.text.TextUtils; // Not strictly needed with new logic
import java.util.Locale;

public class LocaleUtils {
    // private static String originalSystemLang; // No longer needed with this approach

    /**
     * Initialize originalSystemLang from device settings.
     * Should be called once at application startup.
     * This method is no longer strictly necessary if "system" means current system default.
     */
    /*
    public static void initSystemLanguage(Context ctx) {
        if (originalSystemLang == null) {
            Locale sys = ctx.getResources()
                .getConfiguration()
                .getLocales()
                .get(0);
            originalSystemLang = sys.getLanguage();
        }
    }
    */

    public static void applyLocale(Context context, String langPref) {
        Locale newLocale;

        if ("system".equals(langPref)) {
            // For "system", use the current system's default locale.
            // If it's not Romanian, default the app to English.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
            } else {
                newLocale = Resources.getSystem().getConfiguration().locale;
            }
            // Fallback to English if system is not Romanian (as we only support en/ro)
            if (!"ro".equals(newLocale.getLanguage())) {
                newLocale = new Locale("en");
            }
        } else if ("en".equals(langPref) || "ro".equals(langPref)) {
            // Directly apply "en" or "ro" if explicitly chosen
            newLocale = new Locale(langPref);
        } else {
            // Fallback to English for any other invalid preference
            newLocale = new Locale("en");
        }

        Locale.setDefault(newLocale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(newLocale);
        res.updateConfiguration(config, res.getDisplayMetrics());

        // Update application context as well for broader effect,
        // especially if some resources are fetched from application context.
        Context appContext = context.getApplicationContext();
        if (appContext != context) { // Avoid re-updating if context is already appContext
            Resources appRes = appContext.getResources();
            Configuration appConfig = new Configuration(appRes.getConfiguration());
            appConfig.setLocale(newLocale);
            appRes.updateConfiguration(appConfig, appRes.getDisplayMetrics());
        }
    }

    public static Locale getResolvedLocale(Context context) {
        String langPref = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("pref_language", "system"); // Assuming "pref_language" is the key
        Locale newLocale;
        if ("system".equals(langPref)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
            } else {
                newLocale = Resources.getSystem().getConfiguration().locale;
            }
            // Fallback to English if system is not Romanian (as we only support en/ro)
            if (!"ro".equals(newLocale.getLanguage())) {
                newLocale = new Locale("en");
            }
        } else if ("en".equals(langPref) || "ro".equals(langPref)) {
            newLocale = new Locale(langPref);
        } else {
            newLocale = new Locale("en"); // Default fallback
        }
        return newLocale;
    }
}
