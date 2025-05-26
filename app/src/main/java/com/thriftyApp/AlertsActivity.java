package com.thriftyApp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
// Remove unused imports like AdapterView, ArrayAdapter, ListView
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Import RecyclerView and LinearLayoutManager
import androidx.core.content.ContextCompat; // Added for getColor
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List; // Import List

// Implement the OnReminderActionListener interface
public class AlertsActivity extends BaseActivity implements ReminderAdapter.OnReminderActionListener {

    // boolean flagFloatingButton; // No longer needed
    FloatingActionButton floatingActionButton;
    // Button reminderAdd, budgetAdd; // No longer needed
    // Replace ListView with RecyclerView
    RecyclerView alertsRecyclerView;
    ReminderAdapter reminderAdapter; // Add adapter instance
    DatabaseHelper databaseHelper;
    // TextView budgetDetails; // No longer needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_alerts);
        databaseHelper = new DatabaseHelper (this);
        // flagFloatingButton = false; // No longer needed
        floatingActionButton =  findViewById (R.id.floatingActionButtonA);
        // budgetDetails =  findViewById (R.id.budgetDetailsTextView); // No longer needed
        // budgetDetails.setText ("Current Budget : "+ Utils.budget); // No longer needed

        // reminderAdd = findViewById (R.id.remAddButton); // No longer needed
        // budgetAdd = findViewById (R.id.budgetAddButton); // No longer needed

        View topNavBar = findViewById(R.id.top_navigation_bar);
        TextView home = topNavBar.findViewById (R.id.nav_home_text);
        TextView options = topNavBar.findViewById(R.id.nav_options_text);
        TextView alertsTab = topNavBar.findViewById(R.id.nav_alerts_text);

        // Set text colors for active/inactive tabs
        // Active tab (Alerts)
        alertsTab.setTextColor(ContextCompat.getColor(this, R.color.secondary));
        // Inactive tabs
        home.setTextColor(ContextCompat.getColor(this, R.color.text_primary)); 
        options.setTextColor(ContextCompat.getColor(this, R.color.text_primary));


        TextView thrifty =  findViewById (R.id.thriftyTitleA);

        // Initialize RecyclerView
        alertsRecyclerView = findViewById(R.id.alertsRecyclerView);
        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter with an empty list initially and pass 'this' as the listener
        reminderAdapter = new ReminderAdapter(this, new ArrayList<>(), this);
        alertsRecyclerView.setAdapter(reminderAdapter);

        setList (); // Load data into adapter
        // reminderAdd.setVisibility (View.INVISIBLE); // No longer needed
        // budgetAdd.setVisibility (View.INVISIBLE); // No longer needed

        // FAB now directly launches AddReminderActivity
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddReminderActivity.class);
                startActivity(intent);
                // Do not finish AlertsActivity, so user returns here
            }
        });

        // budgetAdd.setOnClickListener (...) // To be moved to Settings

        home.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext (), Dashboard.class);
                startActivity (intent);
                finish ();
            }
        });

        thrifty.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (getApplicationContext (), Dashboard.class);
                startActivity (intent);
                finish ();
            }
        });
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
        alertsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AlertsActivity.class);
                startActivity(intent);
                finish();
            }
        });



/*
        final ArrayList<String> myFriends = new ArrayList<String>(asList("Varsha","Samyuktha","Tejaswini","Sivakami","Ashu","Atsh", "Dhak", "Bhava","Prash","Kavin"));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myFriends);

        myListView.setAdapter(arrayAdapter);
        */
    }

    // Remove FloatingButtonToggle method as it's no longer used
    /*
    public void FloatingButtonToggle(View view) {
        if (flagFloatingButton) {
            flagFloatingButton = false;
            // reminderAdd.setVisibility (View.INVISIBLE);
            // budgetAdd.setVisibility (View.INVISIBLE);
        }
        else {
            flagFloatingButton = true;
            // reminderAdd.setVisibility (View.VISIBLE);
            // budgetAdd.setVisibility (View.VISIBLE);
        }
    }
    */

    @Override
    public void onBackPressed() {
        Intent intent = new Intent (getApplicationContext (),Dashboard.class);
        startActivity (intent);
        finish ();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Apply theme and locale first
        String theme = androidx.preference.PreferenceManager
            .getDefaultSharedPreferences(this)
            .getString("pref_theme", "system");
        ThemeUtils.applyTheme(theme);
        String lang = androidx.preference.PreferenceManager
            .getDefaultSharedPreferences(this)
            .getString("pref_language", "system");
        LocaleUtils.applyLocale(this, lang);

        // Then update tab styles, as colors might depend on the theme
        updateTabStyles();
        // Also refresh the list data
        setList();
    }

    private void updateTabStyles() {
        View topNavBar = findViewById(R.id.top_navigation_bar);
        if (topNavBar != null) {
            TextView home = topNavBar.findViewById(R.id.nav_home_text);
            TextView options = topNavBar.findViewById(R.id.nav_options_text);
            TextView alertsTab = topNavBar.findViewById(R.id.nav_alerts_text);

            if (alertsTab != null) alertsTab.setTextColor(ContextCompat.getColor(this, R.color.secondary));
            if (home != null) home.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            if (options != null) options.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
    }

    public void setList() {
        // Fetch List<AlertsTable> instead of List<String>
        List<AlertsTable> reminders = databaseHelper.getReminders();
        if (reminders.isEmpty()) {
            Toast.makeText(AlertsActivity.this, "No reminders yet.", Toast.LENGTH_LONG).show();
            // Optionally, show a placeholder view for no reminders
        }
        // Update the adapter's data
        reminderAdapter.setReminders(reminders);
        // Remove old ListView item click listener logic
    }

    // Implementation of OnReminderActionListener interface methods
    @Override
    public void onEditReminder(int reminderId) {
        Intent intent = new Intent(getApplicationContext(), AddReminderActivity.class);
        intent.putExtra("reminder_id", reminderId); // Pass the ID to AddReminderActivity
        startActivity(intent);
        // Consider if you need to refresh the list in onResume of AlertsActivity
        // if changes are made in AddReminderActivity
    }

    @Override
    public void onDeleteReminder(int reminderId, int position) {
        // Cancel the alarm associated with this reminder
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        // Use the same reminderId (which was used as requestCode) to cancel
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            reminderId,
            intent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel(); // Also cancel the PendingIntent itself
        }

        // Delete from database
        databaseHelper.deleteReminder(reminderId);
        // Remove from adapter's list and notify
        reminderAdapter.removeItem(position);
        Toast.makeText(this, "Reminder deleted.", Toast.LENGTH_SHORT).show();

        // If the list becomes empty, show a message
        if (reminderAdapter.getItemCount() == 0) {
            Toast.makeText(AlertsActivity.this, "No reminders yet.", Toast.LENGTH_LONG).show();
        }
    }
}
