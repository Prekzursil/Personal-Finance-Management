package com.thriftyApp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isBootCompleted(intent)) {
            return;
        }
        Log.d(TAG, "Boot completed, re-scheduling reminders.");
        ArrayList<AlertsTable> reminders = new DatabaseHelper(context).getReminders();
        if (reminders.isEmpty()) {
            Log.d(TAG, "No reminders to re-schedule.");
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (!canScheduleExactAlarms(alarmManager)) {
            return;
        }
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        for (AlertsTable reminder : reminders) {
            rescheduleReminder(context, alarmManager, dbFormat, reminder);
        }
        Log.d(TAG, "Finished re-scheduling reminders.");
    }

    /** Pure check: is this the boot-completed broadcast we care about? */
    static boolean isBootCompleted(Intent intent) {
        return intent != null
                && intent.getAction() != null
                && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED);
    }

    // Verifies the AlarmManager exists and may schedule exact alarms on API 31+.
    private boolean canScheduleExactAlarms(AlarmManager alarmManager) {
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null, cannot re-schedule reminders.");
            return false;
        }
        // Check for SCHEDULE_EXACT_ALARM permission on Android 12+. This check in a
        // BroadcastReceiver is tricky as we can't easily prompt the user, so the app
        // should ensure this permission is granted during normal operation.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "Cannot schedule exact alarms. Permission not granted. Reminders will not be re-scheduled.");
            return false;
        }
        return true;
    }

    // Schedules a single future reminder, logging and swallowing failures.
    private void rescheduleReminder(Context context, AlarmManager alarmManager,
            SimpleDateFormat dbFormat, AlertsTable reminder) {
        try {
            Date reminderDateTime = dbFormat.parse(reminder.getalert_at());
            if (reminderDateTime == null || reminderDateTime.getTime() <= System.currentTimeMillis()) {
                return;
            }
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

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderDateTime.getTime(), pendingIntent);
            Log.d(TAG, "Re-scheduled reminder ID: " + requestCode + " for " + reminder.getalert_at());
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date for reminder ID: " + reminder.getAid(), e);
        } catch (Exception e) {
            Log.e(TAG, "Error re-scheduling reminder ID: " + reminder.getAid(), e);
        }
    }
}
