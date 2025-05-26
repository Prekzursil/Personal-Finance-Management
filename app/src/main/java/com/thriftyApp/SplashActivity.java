package com.thriftyApp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.thriftyApp.BaseActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Simple splash screen that shows a loading GIF, waits 1 s, then
 * launches {@link Dashboard}.  No layout id mismatches.
 */
public class SplashActivity extends BaseActivity {

    private static final int SPLASH_TIME = 1000; // ms
    private GoogleSignInAccount googleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getIntent().hasExtra("google_account")) {
            googleAccount = getIntent().getParcelableExtra("google_account");
        }

        ImageView imageView = findViewById(R.id.imageView);  // id matches layout
        Glide.with(this).load(R.raw.loading).into(imageView);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, Dashboard.class);
            if (googleAccount != null) {
                intent.putExtra("google_account", googleAccount);
            }
            startActivity(intent);
            finish();
        }, SPLASH_TIME);
    }
}
