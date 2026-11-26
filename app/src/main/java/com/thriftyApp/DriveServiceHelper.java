package com.thriftyApp;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.identity.AuthorizationClient;
import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.ClearTokenRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
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

    /** The only Google scope the app asks for: files it created itself in the user's Drive. */
    static final String DRIVE_FILE_SCOPE_URL = "https://www.googleapis.com/auth/drive.file";
    static final Scope DRIVE_FILE_SCOPE = new Scope(DRIVE_FILE_SCOPE_URL);
    private static final String GOOGLE_AUTH_UTIL_SCOPE = "oauth2:" + DRIVE_FILE_SCOPE_URL;
    /** Upper bound for the authorization calls made from the backup worker thread. */
    private static final long AUTHORIZATION_TIMEOUT_SECONDS = 30;

    private final Executor executor = Executors.newSingleThreadExecutor();
    // Certificate pinning for the Google APIs endpoints this helper talks to.
    // Pinned to the Google Trust Services root (GTS Root R1) plus the current
    // GTS intermediate, so a forged/MITM certificate is rejected while normal
    // Google certificate rotation (short-lived leaves) keeps working.
    private static final CertificatePinner CERTIFICATE_PINNER = new CertificatePinner.Builder()
            .add("*.googleapis.com", "sha256/hxqRlPTu1bMS/0DITB1SSu0vd4u/8l8TjPgfaAp63Gc=") // GTS Root R1
            .add("*.googleapis.com", "sha256/YPtHaftLw6/0vnc2BnNKGF54xiCA28WFcccjkA4ypCM=") // GTS WR2
            .build();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .certificatePinner(CERTIFICATE_PINNER)
            .build();
    private final Gson gson = new Gson();
    private final Context context;
    // The Google account the user signed in with. The removed GoogleSignInAccount only
    // ever served as the carrier of this Android Account.
    private final Account account;
    private final AuthorizationClient authorizationClient;

    // cached – refreshed lazily if Google returns 401
    private volatile String accessToken;

    public DriveServiceHelper(Account account, Context context) {
        this.context = context.getApplicationContext();
        this.account = account;
        this.authorizationClient = Identity.getAuthorizationClient(this.context);
    }

    /* ---------------- Token helpers ---------------- */

    /**
     * The drive.file grant is given when the user consents at sign-in (AuthorizationClient, the
     * replacement for GoogleSignInOptions.requestScopes). GoogleAuthUtil has always been the token
     * source here, so it stays first. A grant that only the authorization client can see is handled
     * by asking it directly: without UI (no consent sheet can be shown from this worker thread) and
     * with a hard timeout, so a stalled client cannot wedge the backup executor.
     */
    private String fetchAccessToken() throws Exception {
        try {
            return GoogleAuthUtil.getToken(context, account, GOOGLE_AUTH_UTIL_SCOPE);
        } catch (GoogleAuthException | IOException e) {
            String token = authorizeSilently();
            if (token != null) {
                return token;
            }
            throw e;
        }
    }

    private String authorizeSilently() {
        AuthorizationRequest request = new AuthorizationRequest.Builder()
                .setAccount(account)
                .setRequestedScopes(Collections.singletonList(DRIVE_FILE_SCOPE))
                .build();
        try {
            AuthorizationResult result = Tasks.await(authorizationClient.authorize(request),
                    AUTHORIZATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            // A pending intent means the user still has to consent, which cannot happen from here.
            return result.hasResolution() ? null : result.getAccessToken();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            Log.w(TAG, "Silent Drive authorization did not yield a token", e);
            return null;
        }
    }

    private synchronized String getAccessToken() throws Exception {
        if (accessToken == null) {
            accessToken = fetchAccessToken();
        }
        return accessToken;
    }

    @SuppressLint("MissingPermission")
    private synchronized void invalidateToken() {
        if (accessToken != null) {
            GoogleAuthUtil.invalidateToken(context, accessToken);
            // The authorization client keeps a token cache of its own; drop the stale token there too.
            try {
                Tasks.await(authorizationClient.clearToken(
                                new ClearTokenRequest.Builder().setToken(accessToken).build()),
                        AUTHORIZATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Log.w(TAG, "Could not clear the authorization client's token", e);
            }
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