package com.thriftyApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import com.thriftyApp.BaseActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class AddBudgetActivity extends BaseActivity {

    DatabaseHelper databaseHelper;
    EditText budgetEdit;
    TextView thrifty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_add_budget);
        databaseHelper = new DatabaseHelper (this);
        budgetEdit = findViewById (R.id.budAmountEditText);

        thrifty = findViewById (R.id.thriftyTitleAddBud);
        // Fix: null‑safe
        budgetEdit.setText(Utils.budget == null ? "0" : Utils.budget);

        findViewById(R.id.close_addbud).setOnClickListener(
                arg0 -> onBackPressed ());

        thrifty.setOnClickListener (v -> {
            Intent intent = new Intent (getApplicationContext (), Dashboard.class);
            startActivity (intent);
            finish ();
        });

        findViewById (R.id.floatingActionButtonAddBud).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                if(budgetEdit.getText().toString().trim().isEmpty())
                {
                    Toast.makeText (getApplicationContext (),"Enter valid budget amount and end date.",Toast.LENGTH_SHORT).show ();
                }
                else {

                    Utils.budget = budgetEdit.getText ().toString ();
                    databaseHelper.changeBudget ();
                    Toast.makeText (getApplicationContext ( ), "Changed Budget Amount.", Toast.LENGTH_SHORT).show ( );
                    Intent intent = new Intent (getApplicationContext ( ), AlertsActivity.class);
                    startActivity (intent);
                    finish ( );
                }
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Redirect to SettingsActivity as "Change Budget" is now accessed from there
        Intent intent = new Intent (getApplicationContext (),SettingsActivity.class);
        startActivity (intent);
        finish ();
    }
}
