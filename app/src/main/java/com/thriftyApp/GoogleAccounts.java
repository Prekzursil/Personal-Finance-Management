package com.thriftyApp;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.identity.AuthorizationClient;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.RevokeAccessRequest;

import java.util.Collections;

/**
 * Remembers which Google account the user signed in with, for the Drive backup.
 *
 * <p>play-services-auth 22.0.0 removed the legacy Google Sign-In SDK and with it
 * {@code GoogleSignIn.getLastSignedInAccount()}, the session store the app used to find
 * the account again once the sign-in screen was gone (background sync on the dashboard,
 * the backup / restore preferences). Credential Manager has no equivalent: the app is
 * expected to keep what it needs from the sign-in result itself. This class keeps the
 * account's e-mail address in private preferences and turns it back into the Android
 * {@link Account} the backup helper needs.
 *
 * <p>The store is the app's own, so it has the lifetime the old implicit session had:
 * an account is remembered only after a successful Google sign-in and is forgotten by
 * {@link #signOut(Activity)}, which also revokes the app's access on that account and
 * clears Credential Manager's state.
 */
final class GoogleAccounts {

    private static final String TAG = "GoogleAccounts";

    private static final String PREFS_NAME = "GoogleAccount";
    private static final String KEY_ACCOUNT_EMAIL = "account_email";

    private GoogleAccounts() {}

    /** Remembers {@code email} as the signed-in Google account and returns it as an {@link Account}. */
    static @Nullable Account remember(Context context, @Nullable String email) {
        Account account = fromEmail(email);
        if (account != null) {
            prefs(context).edit().putString(KEY_ACCOUNT_EMAIL, account.name).apply();
        }
        return account;
    }

    /**
     * The Google account of the current app session, or {@code null} when nobody signed in
     * with Google since the last {@link #signOut(Activity)} (replaces
     * {@code GoogleSignIn.getLastSignedInAccount}).
     */
    static @Nullable Account lastSignedInAccount(Context context) {
        return fromEmail(prefs(context).getString(KEY_ACCOUNT_EMAIL, null));
    }

    /**
     * Ends the Google session: revokes the app's access on the signed-in account, clears
     * Credential Manager's local state and forgets the account. Firebase is signed out by the
     * caller. Replaces {@code GoogleSignInClient.signOut()} followed by
     * {@code GoogleSignInClient.revokeAccess()}.
     */
    static void signOut(Activity activity) {
        Account account = lastSignedInAccount(activity);
        forget(activity);

        AuthorizationClient authorizationClient = Identity.getAuthorizationClient(activity);
        if (account != null) {
            // drive.file is the only scope the app is ever granted, so revoking it is
            // what the removed GoogleSignInClient.revokeAccess() did for this app.
            authorizationClient.revokeAccess(RevokeAccessRequest.builder()
                            .setAccount(account)
                            .setScopes(Collections.singletonList(DriveServiceHelper.DRIVE_FILE_SCOPE))
                            .build())
                    .addOnFailureListener(activity, e ->
                            Log.w(TAG, "Could not revoke Drive access for " + account.name, e));
        }

        CredentialManager credentialManager = CredentialManager.create(activity);
        credentialManager.clearCredentialStateAsync(new ClearCredentialStateRequest(), null,
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(Void result) {
                        // Nothing to report: the local credential state is gone.
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        // Not fatal: the account is already forgotten, so the app starts a fresh sign-in.
                        Log.w(TAG, "Could not clear the sign-in state", e);
                    }
                });
    }

    static void forget(Context context) {
        prefs(context).edit().remove(KEY_ACCOUNT_EMAIL).apply();
    }

    /** The Android account for a Google e-mail address, or {@code null} when there is none. */
    static @Nullable Account fromEmail(@Nullable String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return new Account(email.trim(), GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
