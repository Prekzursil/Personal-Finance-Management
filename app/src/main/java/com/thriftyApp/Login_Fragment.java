package com.thriftyApp;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login_Fragment extends Fragment implements OnClickListener {

    /* ------------------------------------------------------------------ */
    /* Safe-Toast helper so we never crash if the fragment is detached.   */
    /* ------------------------------------------------------------------ */
    private void showToastSafe(@NonNull String msg) {
        Context ctx = getContext() != null ? getContext() : getActivity();
        if (ctx != null) {
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
        } else {
            Log.w("Login_Fragment", "Toast dropped (\"" + msg + "\") – no Context");
        }
    }

    /* --------------------------- UI & member vars -------------------- */
    private static View view;
    private static EditText emailid, password;
    private static Button loginButton;
    private static TextView signUp;
    private static CheckBox show_hide_password;
    private static LinearLayout loginLayout;
    private static Animation shakeAnimation;
    private static FragmentManager fragmentManager;
    private DatabaseHelper databaseHelper;

    private FirebaseAuth mAuth;
    // Android Credential Manager: the replacement for GoogleSignInClient, removed in
    // play-services-auth 22.0.0. Its result arrives through CredentialManagerCallback, not
    // through an Activity result.
    private CredentialManager credentialManager;
    private MaterialCardView googleSignInButton;
    private final Executor executor = Executors.newSingleThreadExecutor();

    // The Google account of the sign-in in progress, while the Drive consent sheet is up.
    // Null once the sign-in is settled (the dashboard is told about the account, or the
    // sign-in was abandoned).
    private Account driveAccount;
    // The consent sheet is shown at most once per sign-in, so a grant that keeps asking for
    // it cannot turn into a loop.
    private boolean driveConsentShown;

    // Registered during fragment construction as required by the API. Google sign-in
    // itself no longer uses an Activity result; this launcher carries the Drive consent
    // sheet, the replacement for GoogleSignInOptions.requestScopes.
    private final ActivityResultLauncher<IntentSenderRequest> driveConsentLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> onDriveConsentResult(result.getResultCode()));

    public Login_Fragment() {}

    /* ------------------------------------------------------------------ */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        databaseHelper = new DatabaseHelper(getContext());
        view = inflater.inflate(R.layout.login_layout, container, false);

        initViews();
        setListeners();

        mAuth = FirebaseAuth.getInstance();
        // Ensure Firebase and Google sessions cleared on login screen
        mAuth.signOut();
        GoogleAccounts.signOut(requireActivity());
        credentialManager = CredentialManager.create(requireContext());

        googleSignInButton = view.findViewById(R.id.googleSignInBtn);
        googleSignInButton.setOnClickListener(this);

        return view;
    }

    /* ----------------------- view helpers ---------------------------- */
    private void initViews() {
        fragmentManager = requireActivity().getSupportFragmentManager();
        emailid          = view.findViewById(R.id.login_emailid);
        password         = view.findViewById(R.id.login_password);
        loginButton      = view.findViewById(R.id.loginBtn);
        signUp           = view.findViewById(R.id.createAccount);
        show_hide_password = view.findViewById(R.id.show_hide_password);
        loginLayout      = view.findViewById(R.id.login_layout);

        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);

        try {
            ColorStateList csl = AppCompatResources.getColorStateList(requireContext(), R.color.text_selector);
            if (csl == null) {
                throw new IllegalStateException("Missing text selector color state list");
            }
            show_hide_password.setTextColor(csl);
            signUp.setTextColor(csl);
        } catch (Exception e) {
            Log.i("Login", "Error loading selector: " + e.getMessage());
        }
    }

    private void setListeners() {
        loginButton.setOnClickListener(this);
        signUp.setOnClickListener(this);

        show_hide_password.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                show_hide_password.setText(R.string.hide_pwd);
                password.setInputType(InputType.TYPE_CLASS_TEXT);
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                show_hide_password.setText(R.string.show_pwd);
                password.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    /* ----------------------- clicks ---------------------------------- */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.loginBtn) {
            checkValidation();
        } else if (id == R.id.createAccount) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                    .replace(R.id.frameContainer,
                            new SignUp_Fragment(),
                            Utils.SignUp_Fragment)
                    .commit();
        } else if (id == R.id.googleSignInBtn) {
            signInWithGoogle();
        }
    }

    /* ----------------------- Google sign-in -------------------------- */
    private void signInWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                // Offer every account on the device plus "Add another account", as the
                // removed GoogleSignInOptions.DEFAULT_SIGN_IN chooser did.
                .setFilterByAuthorizedAccounts(false)
                .build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        driveAccount = null;
        driveConsentShown = false;

        credentialManager.getCredentialAsync(requireActivity(), request, null,
                ContextCompat.getMainExecutor(requireContext()),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        if (!isAdded()) {
                            return;
                        }
                        handleSignInResult(result.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        if (!isAdded()) {
                            return;
                        }
                        if (e instanceof GetCredentialCancellationException) {
                            // The user dismissed the account picker: not a failure.
                            Log.i("SignIn", "Google sign-in cancelled");
                            return;
                        }
                        showToastSafe("Google Sign-In failed");
                        Log.e("SignIn", "Google sign in failed", e);
                    }
                });
    }

    private void handleSignInResult(Credential credential) {
        if (!(credential instanceof CustomCredential)
                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(
                        credential.getType())) {
            String resultType = credential.getType();
            showToastSafe("Google Sign-In failed");
            Log.e("SignIn", "Unexpected type of sign-in result: " + resultType);
            return;
        }
        GoogleIdTokenCredential googleIdToken;
        try {
            googleIdToken = GoogleIdTokenCredential.createFrom(credential.getData());
        } catch (GoogleIdTokenParsingException | IllegalArgumentException e) {
            // createFrom rejects a malformed ID token with an unchecked exception.
            showToastSafe("Google Sign-In failed");
            Log.e("SignIn", "Received an invalid google id token response", e);
            return;
        }
        firebaseAuthWithGoogle(googleIdToken);
    }

    private void firebaseAuthWithGoogle(GoogleIdTokenCredential googleIdToken) {
        AuthCredential cred =
                GoogleAuthProvider.getCredential(googleIdToken.getIdToken(), null);

        mAuth.signInWithCredential(cred).addOnCompleteListener(requireActivity(), task -> {
            if (!task.isSuccessful()) {
                showToastSafe("Authentication failed.");
                Log.e("Auth", "Auth failed", task.getException());
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            assert user != null;

            List<String> data = databaseHelper.searchPass(user.getEmail());
            Contact userContact = databaseHelper.getUserByGoogleId(user.getUid());
            String googleId = userContact.getEmailId();

            if (data != null && !data.isEmpty() && googleId.equals(user.getEmail())) {
                Utils.userId = data.get(1);
                Utils.budget = data.get(2);

                // The removed GoogleSignInAccount was both the signed-in identity and the
                // Android account the Drive backup runs under. Its e-mail identifies that
                // account, so that is what the app remembers for the rest of the session.
                Account account =
                        GoogleAccounts.remember(requireContext(), googleIdToken.getEmail());
                requestDriveAccess(account);
            } else {
                showToastSafe("Welcome! Please complete sign-up.");
                SignUp_Fragment signUpFragment = new SignUp_Fragment();
                Bundle b = new Bundle();
                b.putString("USER_ID", user.getUid());
                b.putString("USER_EMAIL", user.getEmail());
                b.putString("USER_NAME", user.getDisplayName());
                b.putString("PHONE_NUMBER", user.getPhoneNumber());
                signUpFragment.setArguments(b);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameContainer, signUpFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    /* ----------------------- Drive access ---------------------------- */
    private void requestDriveAccess(Account account) {
        driveAccount = account;
        if (account == null) {
            // Signed in, but the ID token named no account to run the backup under.
            Log.w("SignIn", "The Google ID token carried no e-mail; Drive backup stays off");
            continueToDashboard();
            return;
        }
        authorizeDriveAccount();
    }

    /**
     * Asks for the drive.file grant the backup needs. AuthorizationClient replaces the
     * GoogleSignInOptions.requestScopes call that used to collect it with the sign-in.
     */
    private void authorizeDriveAccount() {
        AuthorizationRequest request = new AuthorizationRequest.Builder()
                .setAccount(driveAccount)
                .setRequestedScopes(Collections.singletonList(DriveServiceHelper.DRIVE_FILE_SCOPE))
                .build();
        Identity.getAuthorizationClient(requireActivity()).authorize(request)
                .addOnSuccessListener(requireActivity(), result -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (result.hasResolution()) {
                        launchDriveConsent(result);
                    } else {
                        continueToDashboard();
                    }
                })
                .addOnFailureListener(requireActivity(), e -> {
                    if (!isAdded()) {
                        return;
                    }
                    Log.w("SignIn", "Drive authorization failed", e);
                    driveAccessDenied();
                });
    }

    private void launchDriveConsent(AuthorizationResult result) {
        if (driveConsentShown || result.getPendingIntent() == null) {
            driveAccessDenied();
            return;
        }
        driveConsentShown = true;
        try {
            driveConsentLauncher.launch(new IntentSenderRequest.Builder(
                    result.getPendingIntent().getIntentSender()).build());
        } catch (RuntimeException e) {
            Log.w("SignIn", "Could not show the Drive consent sheet", e);
            driveAccessDenied();
        }
    }

    private void onDriveConsentResult(int resultCode) {
        if (!isAdded()) {
            return;
        }
        if (driveAccount == null) {
            // The sign-in screen was rebuilt while the sheet was up: the account was
            // remembered before the sheet was shown.
            driveAccount = GoogleAccounts.lastSignedInAccount(requireContext());
        }
        if (resultCode == Activity.RESULT_OK && driveAccount != null) {
            // Asking again now returns the grant without a pending intent.
            authorizeDriveAccount();
        } else {
            driveAccessDenied();
        }
    }

    /** The user is signed in, but with no Drive grant there is nothing to sync. */
    private void driveAccessDenied() {
        GoogleAccounts.forget(requireContext());
        driveAccount = null;
        continueToDashboard();
    }

    private void continueToDashboard() {
        if (driveAccount != null) {
            handleBackup(driveAccount);
        }

        Intent intent = new Intent(getActivity(), SplashActivity.class);
        if (driveAccount != null) {
            intent.putExtra("google_account", driveAccount);
        }
        startActivity(intent);
        requireActivity().finish();
    }

    /* ----------------------- Drive backup helpers -------------------- */
    private void handleBackup(Account acc) {
        DriveServiceHelper helper = new DriveServiceHelper(acc, requireContext());

        helper.readBackup()
                .addOnSuccessListener(json -> {
                    if (json != null) {
                        try {
                            databaseHelper.importBackup(json);
                            showToastSafe("Data restored from backup");
                        } catch (Exception e) {
                            Log.e("Backup", "Restore failed", e);
                            createNewBackup(helper);
                        }
                    } else {
                        createNewBackup(helper);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Backup", "Error reading backup", e);
                    createNewBackup(helper);
                });
    }

    private void createNewBackup(DriveServiceHelper helper) {
        try {
            String json = databaseHelper.exportBackup();
            helper.createOrUpdateBackup(json)
                    .addOnSuccessListener(id -> {
                        Log.d("Backup", "Backup created: " + id);
                        showToastSafe("Backup created successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Backup", "Failed to create backup", e);
                        showToastSafe("Failed to create backup");
                    });
        } catch (Exception e) {
            Log.e("Backup", "Error preparing backup", e);
            showToastSafe("Error preparing backup data");
        }
    }

    /* ----------------------- email / pwd login ----------------------- */
    private void checkValidation() {
        String emailStr = emailid.getText().toString();
        String pwdStr   = password.getText().toString();

        Matcher m = Pattern.compile(Utils.regEx).matcher(emailStr);

        if (emailStr.isEmpty() || pwdStr.isEmpty()) {
            loginLayout.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(requireActivity(), view, "Enter both credentials.");
            return;
        }
        if (!m.find()) {
            new CustomToast().Show_Toast(requireActivity(), view, "Your Email Id is Invalid.");
            return;
        }

        List<String> data = databaseHelper.searchPass(emailStr);
        if (data.isEmpty()) {
            new CustomToast().Show_Toast(requireActivity(), view, "Hello, Sign up as user to login.");
            return;
        }

        String actualPwd = data.get(0);
        String userId    = data.get(1);
        String budget    = data.get(2);

        if (PasswordHasher.verifyPassword(pwdStr, actualPwd)) {
            Utils.userId = userId;
            Utils.budget = budget;
            ((MainActivity) requireActivity()).moveToSplash();
        } else {
            loginLayout.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(requireActivity(), view, "Username and Password do not match.");
        }
    }
}
