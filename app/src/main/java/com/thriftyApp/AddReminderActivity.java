 package com.thriftyApp;

import android.app.AlarmManager;
import android.app.AlertDialog; // Import AlertDialog
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.Manifest; // For permission string
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager; // For permission check
import android.net.Uri; // For settings intent
import android.os.Build; // For version checks
import android.os.Bundle;
import android.provider.Settings; // For settings intent
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView; // Keep for thriftyTitle
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity; // Keep BaseActivity
import androidx.core.content.ContextCompat; // For permission check

import java.text.ParseException; // Import ParseException
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date; // Import java.util.Date
import java.util.Locale;

public class AddReminderActivity extends BaseActivity {

    private EditText describeReminderEditText;
    // Revert to TextView for picker dialogs
    private TextView dateRemEditText, timeRemEditText;
    private ImageView closeButton;
    private TextView thriftyTitle;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fab;
    private DatabaseHelper databaseHelper;
    private Calendar calendar; // This will be used by pickers
    private int currentReminderId = -1; // To store ID in edit mode
    private AlertsTable currentReminder = null; // To store the reminder being edited

    // Launcher for notification permission
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        // Initialize the ActivityResultLauncher for notification permission
        requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. We can now proceed to save and schedule.
                    // Potentially re-trigger saveAndSchedule or enable a button.
                    // For now, we'll assume the user clicks save again if needed.
                    Toast.makeText(this, "Notification permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    // Explain to the user that the feature is unavailable
                    Toast.makeText(this, "Notification permission denied. Reminders may not be shown.", Toast.LENGTH_LONG).show();
                }
            });

        databaseHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();

        describeReminderEditText = findViewById(R.id.describeReminderEditText);
        dateRemEditText = findViewById(R.id.dateRemEditText);
        timeRemEditText = findViewById(R.id.timeRemEditText);
        closeButton = findViewById(R.id.close_addrem);
        thriftyTitle = findViewById(R.id.thriftyTitleAddRem);
        fab = findViewById(R.id.floatingActionButtonAddRem);

        // Check for edit mode
        if (getIntent().hasExtra("reminder_id")) {
            currentReminderId = getIntent().getIntExtra("reminder_id", -1);
            if (currentReminderId != -1) {
                loadReminderData();
                // Optionally change FAB icon or title to "Update Reminder"
            }
        }

        // Remove date picker listener
        // dateRemEditText.setOnClickListener(...)

        // Remove time picker listener
        // timeRemEditText.setOnClickListener(...)

        // Re-add date picker listener
        dateRemEditText.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(
                AddReminderActivity.this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dateRemEditText.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show();
        });

        // Re-add time picker listener
        timeRemEditText.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(
                AddReminderActivity.this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    timeRemEditText.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // 24 hour view
            );
            tpd.show();
        });

        closeButton.setOnClickListener(v -> onBackPressed());

        thriftyTitle.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Dashboard.class));
            finish();
        });

        fab.setOnClickListener(v -> checkPermissionsAndSave());
    }

    private void checkPermissionsAndSave() {
        boolean canScheduleExact = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                canScheduleExact = false;
                new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("To schedule reminders, this app needs permission to schedule exact alarms. Please grant this permission in settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        // Optionally, provide your app's package URI:
                        // intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        }

        if (!canScheduleExact) {
            return; // Stop if exact alarm permission is not available/granted
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API 33
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted, proceed to save
                saveAndScheduleInternal();
            } else {
                // Request notification permission
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                // saveAndScheduleInternal will be called from the launcher's callback if granted
            }
        } else {
            // For older versions, no runtime permission needed for notifications
            saveAndScheduleInternal();
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), AlertsActivity.class));
        finish();
    }

    // Renamed original saveAndSchedule to saveAndScheduleInternal
    private void saveAndScheduleInternal() {
        String message = describeReminderEditText.getText().toString().trim();
        String dateText = dateRemEditText.getText().toString().trim();
        String timeText = timeRemEditText.getText().toString().trim();

        if (message.isEmpty() || dateText.isEmpty() || timeText.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_reminder_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            inputFormat.setLenient(false);
            java.util.Date reminderDateTime = inputFormat.parse(dateText + " " + timeText);
            long triggerAtMillis = reminderDateTime.getTime();

            if (triggerAtMillis <= System.currentTimeMillis() && currentReminderId == -1) { // Only check for new reminders if not editing past one
                Toast.makeText(this, R.string.error_reminder_in_past, Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String alertAt = dbFormat.format(reminderDateTime);

            AlertsTable alert = new AlertsTable();
            try {
                alert.setUid(Integer.parseInt(Utils.userId));
            } catch (NumberFormatException nfe) {
                Toast.makeText(this, R.string.error_invalid_user_id, Toast.LENGTH_SHORT).show();
                nfe.printStackTrace();
                return;
            }
            alert.setMessage(message);
            alert.setalert_at(alertAt);

            if (currentReminderId != -1) { // Edit mode
                alert.setAid(currentReminderId);
                int rowsAffected = databaseHelper.updateReminder(alert);
                if (rowsAffected > 0) {
                    cancelAlarm(currentReminderId); // Cancel old alarm
                    scheduleAlarm(message, triggerAtMillis, currentReminderId); // Schedule new/updated alarm
                    Toast.makeText(this, "Reminder updated.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error updating reminder.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else { // Add mode
                long newAlertId = databaseHelper.insertRemainder(alert);
                if (newAlertId == -1) {
                    Toast.makeText(this, R.string.error_saving_reminder, Toast.LENGTH_SHORT).show();
                    return;
                }
                scheduleAlarm(message, triggerAtMillis, (int) newAlertId);
                Toast.makeText(this, R.string.reminder_set_success, Toast.LENGTH_SHORT).show();
            }

            startActivity(new Intent(getApplicationContext(), AlertsActivity.class));
            finish();

        } catch (ParseException e) {
            Toast.makeText(this, R.string.error_invalid_datetime_format, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_scheduling_reminder, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void loadReminderData() {
        currentReminder = databaseHelper.getReminderById(currentReminderId);
        if (currentReminder != null) {
            describeReminderEditText.setText(currentReminder.getMessage());
            // Parse and set date and time from currentReminder.getalert_at()
            // The format in DB is "yyyy-MM-dd HH:mm:ss"
            // The format for EditText is "dd/MM/yyyy" and "HH:mm"
            try {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date reminderDate = dbFormat.parse(currentReminder.getalert_at());
                if (reminderDate != null) {
                    SimpleDateFormat dateFormatForField = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat timeFormatForField = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    dateRemEditText.setText(dateFormatForField.format(reminderDate));
                    timeRemEditText.setText(timeFormatForField.format(reminderDate));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading reminder date/time.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error loading reminder details.", Toast.LENGTH_SHORT).show();
            finish(); // Or handle error appropriately
        }
    }

    private void scheduleAlarm(String message, long triggerAtMillis, int requestCode) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("message", message);
        intent.putExtra("alert_id", (long) requestCode); // Pass as long, consistent with receiver

        PendingIntent pi = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } else {
            Toast.makeText(this, R.string.error_alarm_service_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAlarm(int requestCode) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
