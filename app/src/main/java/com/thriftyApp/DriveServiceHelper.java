package com.thriftyApp;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Uses raw Drive v3 REST calls with a fresh OAuth2 access‑token obtained from GoogleAuthUtil.
 * Scope required: https://www.googleapis.com/auth/drive.file
 */
public class DriveServiceHelper {

    private static final String TAG = "DriveServiceHelper";
    private static final String DRIVE_API_BASE_URL = "https://www.googleapis.com/drive/v3";
    private static final String BACKUP_FILENAME = "thrifty_backup.json";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Context context;
    private final GoogleSignInAccount account;

    // cached – refreshed lazily if Google returns 401
    private volatile String accessToken;

    public DriveServiceHelper(GoogleSignInAccount account, Context context) {
        this.context = context.getApplicationContext();
        this.account = account;
    }

    /* ---------------- Token helpers ---------------- */

    private String fetchAccessToken() throws Exception {
        String scope = "oauth2:https://www.googleapis.com/auth/drive.file";
        return GoogleAuthUtil.getToken(context, account.getAccount(), scope);
    }

    private synchronized String getAccessToken() throws Exception {
        if (accessToken == null) {
            accessToken = fetchAccessToken();
        }
        return accessToken;
    }

    private synchronized void invalidateToken() {
        if (accessToken != null) {
            GoogleAuthUtil.invalidateToken(context, accessToken);
            accessToken = null;
        }
    }

    /* ---------------- Public  API ---------------- */

    public Task<String> readBackup() {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        executor.execute(() -> {
            try {
                String fileId = findBackupFile();
                if (fileId == null) {
                    tcs.setResult(null);
                    return;
                }
                String content = downloadFile(fileId);
                tcs.setResult(content);
            } catch (Exception e) {
                tcs.setException(e);
            }
        });
        return tcs.getTask();
    }

    public Task<String> createOrUpdateBackup(String backupContent) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        executor.execute(() -> {
            try {
                String fileId = findBackupFile();
                if (fileId == null) {
                    fileId = createNewFile(backupContent);
                } else {
                    updateFile(fileId, backupContent);
                }
                tcs.setResult(fileId);
            } catch (Exception e) {
                tcs.setException(e);
            }
        });
        return tcs.getTask();
    }

    /* ---------------- Drive helpers ---------------- */

    private String findBackupFile() throws Exception {
        String query = URLEncoder.encode("name = '" + BACKUP_FILENAME + "' and trashed = false", "UTF-8");
        String url = DRIVE_API_BASE_URL + "/files?q=" + query + "&spaces=drive&fields=files(id,name)";
        Request request = authorizedRequestBuilder(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 401) { // token expired – retry once
                invalidateToken();
                return findBackupFile();
            }
            if (!response.isSuccessful()) throw new IOException("Search files failed " + response.code());

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            JsonArray files = json.getAsJsonArray("files");
            if (files != null && files.size() > 0) {
                return files.get(0).getAsJsonObject().get("id").getAsString();
            }
            return null;
        }
    }

    private String downloadFile(String fileId) throws Exception {
        String url = "https://www.googleapis.com/drive/v3/files/" + fileId + "?alt=media";
        Request request = authorizedRequestBuilder(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 401) {
                invalidateToken();
                return downloadFile(fileId);
            }
            if (!response.isSuccessful()) throw new IOException("Download failed " + response.code());
            return response.body().string();
        }
    }

    private String createNewFile(String content) throws Exception {
        String boundary = "BackupBoundary" + System.currentTimeMillis();
        MediaType multipart = MediaType.get("multipart/related; boundary=" + boundary);

        JsonObject metadata = new JsonObject();
        metadata.addProperty("name", BACKUP_FILENAME);
        metadata.addProperty("mimeType", "application/json");

        String body = "--" + boundary + "\n" +
                      "Content-Type: application/json; charset=UTF-8\n\n" +
                      gson.toJson(metadata) + "\n\n" +
                      "--" + boundary + "\n" +
                      "Content-Type: application/json; charset=UTF-8\n\n" +
                      content + "\n" +
                      "--" + boundary + "--";

        RequestBody requestBody = RequestBody.create(body, multipart);

        Request request = authorizedRequestBuilder("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                .post(requestBody)
                .addHeader("Content-Type", "multipart/related; boundary=" + boundary)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 401) {
                invalidateToken();
                return createNewFile(content);
            }
            if (!response.isSuccessful()) throw new IOException("Create file failed " + response.code());

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            return json.get("id").getAsString();
        }
    }

    private void updateFile(String fileId, String content) throws Exception {
        String url = "https://www.googleapis.com/upload/drive/v3/files/" + fileId + "?uploadType=media";
        RequestBody body = RequestBody.create(content, JSON);

        Request request = authorizedRequestBuilder(url).patch(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 401) {
                invalidateToken();
                updateFile(fileId, content);
                return;
            }
            if (!response.isSuccessful()) throw new IOException("Update failed " + response.code());
        }
    }

    /* ---------------- Utils ---------------- */

    private Request.Builder authorizedRequestBuilder(String url) throws Exception {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + getAccessToken());
    }
}