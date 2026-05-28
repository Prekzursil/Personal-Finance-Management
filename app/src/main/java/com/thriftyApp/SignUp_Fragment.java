package com.thriftyApp;


import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;


public class SignUp_Fragment extends Fragment implements OnClickListener {
	private static View view;
	private static EditText fullName, emailId, mobileNumber,
			password, budget, confirmPassword;
	private static TextView login;
	private static Button signUpButton;
	private static CheckBox terms_conditions;
	DatabaseHelper databaseHelper;
	private String uid, userEmail, userName, phoneNumber;

	public SignUp_Fragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.signup_layout, container, false);
		 databaseHelper = new DatabaseHelper (getContext ());

		// Retrieve the email passed from the previous fragment/activity
		if (getArguments() != null) {
			uid = getArguments().getString("USER_ID");
			userEmail = getArguments().getString("USER_EMAIL");
			userName = getArguments().getString("USER_NAME");
			phoneNumber = getArguments().getString("PHONE_NUMBER");
		}

		initViews();
		setListeners();



		return view;
	}

	// Initialize all views
	private void initViews() {
		fullName = (EditText) view.findViewById(R.id.fullName);
		emailId = (EditText) view.findViewById(R.id.userEmailId);
		mobileNumber = (EditText) view.findViewById(R.id.mobileNumber);
		password = (EditText) view.findViewById(R.id.password);
		budget = (EditText) view.findViewById(R.id.budget);
		confirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
		signUpButton = (Button) view.findViewById(R.id.signUpBtn);
		login = (TextView) view.findViewById(R.id.already_user);
		terms_conditions = (CheckBox) view.findViewById(R.id.terms_conditions);

		emailId.setText(userEmail);
		fullName.setText(userName);
		mobileNumber.setText(phoneNumber);
		password.setText(uid);
		confirmPassword.setText(uid);

		// Setting text selector over textviews
		try {
			ColorStateList csl = AppCompatResources.getColorStateList(requireContext(), R.color.text_selector);
			if (csl == null) {
				throw new IllegalStateException("Missing text selector color state list");
			}

			login.setTextColor(csl);
			terms_conditions.setTextColor(csl);
		} catch (Exception e) {
			android.util.Log.w("SignUp_Fragment", "Could not apply text selector colors", e);
		}
	}

	// Set Listeners
	private void setListeners() {
		signUpButton.setOnClickListener(this);
		login.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.signUpBtn) {
			// Call checkValidation method
			checkValidation();
		} else if (v.getId() == R.id.already_user) {
			// Replace login fragment
			new MainActivity().replaceLoginFragment();
		}
	}


	// Check Validation Method
	private void checkValidation() {

		// Get all edittext texts
		String getFullName = fullName.getText().toString();
		String getEmailId = emailId.getText().toString();
		String getMobileNumber = mobileNumber.getText().toString();
		String getBudget = budget.getText().toString();
		String getPassword = password.getText().toString();
		String getConfirmPassword = confirmPassword.getText().toString();

		SignUpValidator.Result result = SignUpValidator.validate(
				getFullName, getEmailId, getMobileNumber, getBudget,
				getPassword, getConfirmPassword, terms_conditions.isChecked());

		if (result == SignUpValidator.Result.VALID) {
			registerContact(getFullName, getEmailId, getMobileNumber, getBudget, getPassword);
		} else {
			new CustomToast().Show_Toast(getActivity(), view, messageFor(result));
		}
	}

	// Map a validation failure to its user-facing message.
	private String messageFor(SignUpValidator.Result result) {
		switch (result) {
			case INVALID_EMAIL:
				return "Your Email Id is Invalid.";
			case PASSWORD_MISMATCH:
				return "Both password doesn't match.";
			case TERMS_NOT_ACCEPTED:
				return "Please select Terms and Conditions.";
			default:
				return "All fields are required.";
		}
	}

	// Persist the new contact and route back to the login screen.
	private void registerContact(String name, String email, String mobile,
			String budgetValue, String pwd) {
		Contact c = new Contact();
		c.setName(name);
		c.setEmailId(email);
		c.setMobile(Long.parseLong(mobile));
		c.setPassword(pwd);
		c.setBudget(Long.parseLong(budgetValue));
		databaseHelper.insertContact(c, uid);
		Toast.makeText(getActivity(), "Login with Email ID and password.", Toast.LENGTH_SHORT).show();
		new MainActivity().replaceLoginFragment();
	}
}
