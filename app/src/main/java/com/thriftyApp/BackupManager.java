package com.thriftyApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupManager {
    private static final String TAG = "BackupManager";
    private static final String PREFS_NAME = "BackupPrefs";
    private static final String LAST_SYNC_KEY = "lastSyncTimestamp";
    
    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final DriveServiceHelper driveServiceHelper;
    private final SharedPreferences prefs;

    public BackupManager(Context context, GoogleSignInAccount account) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.driveServiceHelper = new DriveServiceHelper(account, context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public Task<Void> performSync(boolean forceRestore) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        
        driveServiceHelper.readBackup()
            .addOnSuccessListener(backupJson -> {
                try {
                    if (backupJson != null && forceRestore) {
                        // Restore from backup
                        JsonObject backup = JsonParser.parseString(backupJson).getAsJsonObject();
                        String backupTimestamp = backup.get("timestamp").getAsString();
                        String lastSyncTimestamp = prefs.getString(LAST_SYNC_KEY, null);

                        if (shouldRestoreFromBackup(backupTimestamp, lastSyncTimestamp)) {
                            databaseHelper.importBackup(backupJson);
                            updateLastSyncTimestamp(backupTimestamp);
                            taskCompletionSource.setResult(null);
                        } else {
                            createNewBackup(taskCompletionSource);
                        }
                    } else {
                        // Create new backup
                        createNewBackup(taskCompletionSource);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error during sync", e);
                    taskCompletionSource.setException(e);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error reading backup", e);
                taskCompletionSource.setException(e);
            });
            
        return taskCompletionSource.getTask();
    }

    private void createNewBackup(TaskCompletionSource<Void> taskCompletionSource) {
        try {
            String timestamp = getCurrentTimestamp();
            String backupContent = createBackupWithTimestamp(timestamp);
            
            driveServiceHelper.createOrUpdateBackup(backupContent)
                .addOnSuccessListener(fileId -> {
                    updateLastSyncTimestamp(timestamp);
                    Log.d(TAG, "Backup created with ID: " + fileId);
                    taskCompletionSource.setResult(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create backup", e);
                    taskCompletionSource.setException(e);
                });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing backup", e);
            taskCompletionSource.setException(e);
        }
    }

    private String createBackupWithTimestamp(String timestamp) {
        try {
            String baseBackup = databaseHelper.exportBackup();
            JsonObject backupJson = JsonParser.parseString(baseBackup).getAsJsonObject();
            backupJson.addProperty("timestamp", timestamp);
            return backupJson.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error creating backup with timestamp", e);
            throw e;
        }
    }

    private boolean shouldRestoreFromBackup(String backupTimestamp, String lastSyncTimestamp) {
        if (lastSyncTimestamp == null) {
            return true;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date backupDate = sdf.parse(backupTimestamp);
            Date lastSyncDate = sdf.parse(lastSyncTimestamp);
            return backupDate.after(lastSyncDate);
        } catch (Exception e) {
            Log.e(TAG, "Error comparing timestamps", e);
            return false;
        }
    }

    private void updateLastSyncTimestamp(String timestamp) {
        prefs.edit().putString(LAST_SYNC_KEY, timestamp).apply();
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
