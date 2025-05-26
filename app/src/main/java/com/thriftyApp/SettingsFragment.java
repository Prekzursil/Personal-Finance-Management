package com.thriftyApp;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import android.content.Intent;

public class SettingsFragment extends PreferenceFragmentCompat {

    private BackupManager backupManager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        backupManager = new BackupManager(requireContext(),
                GoogleSignIn.getLastSignedInAccount(requireContext()));

        updateCurrentBudgetSummary(); // Initial update

        Preference backupPref = findPreference("pref_backup");
        Preference restorePref = findPreference("pref_restore");
        Preference changeBudgetPref = findPreference("pref_change_budget");

        if (backupPref != null) {
            backupPref.setOnPreferenceClickListener(preference -> {
                backupManager.performSync(false);
                return true;
            });
        }

        if (restorePref != null) {
            restorePref.setOnPreferenceClickListener(preference -> {
                backupManager.performSync(true);
                return true;
            });
        }

        if (changeBudgetPref != null) {
            changeBudgetPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), AddBudgetActivity.class);
                startActivity(intent);
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCurrentBudgetSummary(); // Update budget display when returning to settings
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .registerOnSharedPreferenceChangeListener(listener);
    }

    private void updateCurrentBudgetSummary() {
        Preference budgetDisplayPref = findPreference("pref_display_current_budget");
        if (budgetDisplayPref != null) {
            String currentBudget = Utils.budget != null ? Utils.budget : "0"; // Default to "0" if null
            budgetDisplayPref.setSummary(getString(R.string.pref_summary_current_budget_value, currentBudget));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

    private final android.content.SharedPreferences.OnSharedPreferenceChangeListener listener =
            (sharedPreferences, key) -> {
                if ("pref_theme".equals(key)) {
                    ThemeUtils.applyTheme(sharedPreferences.getString(key, "system"));
                    requireActivity().recreate();
                } else if ("pref_language".equals(key)) {
                    LocaleUtils.applyLocale(requireContext(), sharedPreferences.getString(key, "system"));
                    Intent intent = new Intent(requireContext(), SplashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    requireActivity().startActivity(intent);
                }
            };
}
