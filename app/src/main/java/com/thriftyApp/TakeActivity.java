package com.thriftyApp;

import android.content.Intent;
import com.thriftyApp.BaseActivity;
import com.thriftyApp.Transactions;

import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TakeActivity extends BaseActivity {

    EditText take, tag;
    DatabaseHelper databaseHelper;
    FloatingActionButton addIncome;
    TextView thrifty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_take);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent (getApplicationContext (),TransactionsActivity.class);
                startActivity (intent);
                finish ();
            }
        });

        take =  findViewById (R.id.takeEditText);
        tag =  findViewById (R.id.tagEditTextTake);
        databaseHelper = new DatabaseHelper (this);
        thrifty = findViewById (R.id.thriftyTitleTake);

        addIncome = findViewById (R.id.floatingActionButtonTake);
        findViewById(R.id.close_take).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                onBackPressed ();
            }
        });

        // Edit mode if transaction_id passed
        Intent intent = getIntent();
        int editId = intent.getIntExtra("transaction_id", -1);
        if (editId != -1) {
            long amountVal = intent.getLongExtra("amount", 0);
            take.setText(String.valueOf(amountVal));
            tag.setText(intent.getStringExtra("tag"));
            addIncome.setImageResource(android.R.drawable.ic_menu_save);
            addIncome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double d2;
                    try {
                        d2 = Double.parseDouble(take.getText().toString());
                    } catch (NumberFormatException e) {
                        Toast.makeText(getApplicationContext(), "Enter a valid amount.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Transactions t = new Transactions();
                    t.setTid(editId);
                    t.setExin(1);
                    t.setAmount(Math.round(d2));
                    t.setTag(tag.getText().toString());
                    String created = intent.getStringExtra("created_at");
                    t.setCreated_at(created != null
                        ? created
                        : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(new Date()));
                    databaseHelper.updateTransaction(t);
                    boolean fromAll = intent.getBooleanExtra("from_all", false);
                    Intent returnIntent = new Intent(getApplicationContext(),
                        fromAll ? TransactionsActivity.class : Dashboard.class);
                    startActivity(returnIntent);
                    finish();
                }
            });
        }

        final TextView dateTextView = (TextView) findViewById (R.id.dateTextViewTake);
        Locale currentAppLocale = LocaleUtils.getResolvedLocale(this); 
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format_display), currentAppLocale);
        String formattedDate = dateFormat.format(new Date());
        dateTextView.setText(formattedDate); 

        addIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (take.getText().toString().isEmpty() || tag.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter valid amount and tag.", Toast.LENGTH_SHORT).show();
                } else {
                    addTake();
                }
            }
        });

        thrifty.setOnClickListener (v -> {
            Intent dashIntent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(dashIntent);
            finish();
        });

    }
    public void addTake () {
        long amount;
        try {
            amount = Long.parseLong (take.getText ().toString ());
        } catch (NumberFormatException e) {
            Toast.makeText (getApplicationContext (), "Enter a valid amount.", Toast.LENGTH_SHORT).show ();
            return;
        }
        Transactions t = new Transactions ();
        t.setExin (1);
        t.setAmount (amount);
        t.setTag (tag.getText ().toString ());
        t.setUid (Utils.safeParseInt (Utils.userId, 0));
        databaseHelper.insertTransaction (t);
        Toast.makeText (getApplicationContext (),"Added Income", Toast.LENGTH_SHORT).show ();
        databaseHelper.getTransactions ();
        Intent intent = new Intent (getApplicationContext (), TransactionsActivity.class);
        startActivity (intent);
        finish ();
    }

}
