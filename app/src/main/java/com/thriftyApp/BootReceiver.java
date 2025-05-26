package com.thriftyApp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Boot completed, re-scheduling reminders.");
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            ArrayList<AlertsTable> reminders = databaseHelper.getReminders(); // Assuming getReminders fetches future reminders

            if (reminders.isEmpty()) {
                Log.d(TAG, "No reminders to re-schedule.");
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null, cannot re-schedule reminders.");
                return;
            }
            
            // Check for SCHEDULE_EXACT_ALARM permission on Android 12+
            // Note: This check in a BroadcastReceiver is tricky as we can't easily prompt user.
            // The app should ensure this permission is granted during normal operation.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact alarms. Permission not granted. Reminders will not be re-scheduled.");
                    // Optionally, create a notification to inform the user to open the app and grant permission.
                    return;
                }
            }

            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            for (AlertsTable reminder : reminders) {
                try {
                    Date reminderDateTime = dbFormat.parse(reminder.getalert_at());
                    if (reminderDateTime != null && reminderDateTime.getTime() > System.currentTimeMillis()) {
                        long triggerAtMillis = reminderDateTime.getTime();
                        int requestCode = reminder.getAid(); // Use the unique reminder ID

                        Intent reminderIntent = new Intent(context, ReminderReceiver.class);
                        reminderIntent.putExtra("message", reminder.getMessage());
                        reminderIntent.putExtra("alert_id", (long) requestCode);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                context,
                                requestCode,
                                reminderIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );

                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                        Log.d(TAG, "Re-scheduled reminder ID: " + requestCode + " for " + reminder.getalert_at());
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date for reminder ID: " + reminder.getAid(), e);
                } catch (Exception e) {
                    Log.e(TAG, "Error re-scheduling reminder ID: " + reminder.getAid(), e);
                }
            }
            Log.d(TAG, "Finished re-scheduling reminders.");
        }
    }
}
