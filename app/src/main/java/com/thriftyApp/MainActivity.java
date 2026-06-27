package com.thriftyApp;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import com.thriftyApp.BaseActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.View;
import android.view.View.OnClickListener;


public class MainActivity extends BaseActivity {

	private static FragmentManager fragmentManager;

	public DatabaseHelper databaseHelper = new DatabaseHelper (this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if( getIntent().getBooleanExtra("Exit me", false)){
			finish();
			return; // add this to prevent from doing unnecessary stuffs
		}
		fragmentManager = getSupportFragmentManager();

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				// Find the tag of sign up and forgot password fragment
				Fragment SignUp_Fragment = fragmentManager
						.findFragmentByTag(Utils.SignUp_Fragment);

				// If the sign-up fragment is showing, return to login; otherwise
				// perform the default back (finish this root activity).
				if (SignUp_Fragment != null)
					replaceLoginFragment();
				else
					finish();
			}
		});

		// If saved instance state is null then replace login fragment
		if (savedInstanceState == null) {
			fragmentManager
					.beginTransaction()
					.replace(R.id.frameContainer, new Login_Fragment(),
							Utils.Login_Fragment).commit();
		}

		// On close icon click finish activity
		findViewById(R.id.close_activity).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						finish();

					}
				});

	}

	// Replace Login Fragment with animation
	protected void replaceLoginFragment() {
		fragmentManager
				.beginTransaction()
				.setCustomAnimations(R.anim.left_enter, R.anim.right_out)
				.replace(R.id.frameContainer, new Login_Fragment(),
						Utils.Login_Fragment).commit();
	}

	public void moveToSplash () {
		Intent intent = new Intent (getApplicationContext (), SplashActivity.class);
		startActivity (intent);
		finish ();
	}
}
