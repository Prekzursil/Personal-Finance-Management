package com.thriftyApp;

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

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

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
    private GoogleSignInClient mGoogleSignInClient;
    private MaterialCardView googleSignInButton;
    private static final int RC_SIGN_IN = 9001;
    private final Executor executor = Executors.newSingleThreadExecutor();

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
        GoogleSignInOptions gsoCleanup = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignIn.getClient(requireActivity(), gsoCleanup)
                .signOut()
                .addOnCompleteListener(requireActivity(), task -> {
                    // optionally revoke access: 
                    GoogleSignIn.getClient(requireActivity(), gsoCleanup).revokeAccess();
                });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .requestScopes(new Scope("https://www.googleapis.com/auth/drive.file"))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

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
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                showToastSafe("Google Sign-In failed");
                Log.e("SignIn", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential cred =
                GoogleAuthProvider.getCredential(account.getIdToken(), null);

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
                String userId = data.get(1);
                String budget = data.get(2);

                handleBackup(account);
                Utils.userId = userId;
                Utils.budget = budget;

                Intent intent = new Intent(getActivity(), SplashActivity.class);
                intent.putExtra("google_account", account);
                startActivity(intent);
                requireActivity().finish();
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

    /* ----------------------- Drive backup helpers -------------------- */
    private void handleBackup(GoogleSignInAccount acc) {
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

        if (actualPwd.equals(pwdStr)) {
            Utils.userId = userId;
            Utils.budget = budget;
            ((MainActivity) requireActivity()).moveToSplash();
        } else {
            loginLayout.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(requireActivity(), view, "Username and Password do not match.");
        }
    }
}
