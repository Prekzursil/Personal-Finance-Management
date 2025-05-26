package com.thriftyApp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "reminders_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        // Retrieve the alert_id passed from the activity, default to -1 if not found
        long alertId = intent.getLongExtra("alert_id", -1L);

        NotificationManager notifManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel on Oreo+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for reminder notifications");
            notifManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Reminder")
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Use the unique alertId (cast to int) as the notification ID
        // If alertId was -1 (not found), use a timestamp as a fallback, though this shouldn't happen with the updated activity code.
        int notificationId = (alertId != -1L) ? (int) alertId : (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        notifManager.notify(notificationId, builder.build());
    }
}
