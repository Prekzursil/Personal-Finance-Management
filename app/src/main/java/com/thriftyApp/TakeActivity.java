package com.thriftyApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import com.thriftyApp.BaseActivity;
import com.thriftyApp.Transactions;

import android.os.Bundle;
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

        take =  findViewById (R.id.takeEditText);
        tag =  findViewById (R.id.tagEditTextTake);
        databaseHelper = new DatabaseHelper (this);
        thrifty = findViewById (R.id.thriftyTitleTake);

        addIncome = findViewById (R.id.floatingActionButtonTake);
        findViewById(R.id.close_take).setOnClickListener(
                arg0 -> onBackPressed ());

        // Edit mode if transaction_id passed
        Intent intent = getIntent();
        int editId = intent.getIntExtra("transaction_id", -1);
        if (editId != -1) {
            long amountVal = intent.getLongExtra("amount", 0);
            take.setText(String.valueOf(amountVal));
            tag.setText(intent.getStringExtra("tag"));
            addIncome.setImageResource(android.R.drawable.ic_menu_save);
            addIncome.setOnClickListener(v -> {
                Transactions t = new Transactions();
                t.setTid(editId);
                t.setExin(1);
                double d2 = Double.parseDouble(take.getText().toString());
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
            });
        }

        final TextView dateTextView = (TextView) findViewById (R.id.dateTextViewTake);
        Locale currentAppLocale = LocaleUtils.getResolvedLocale(this); 
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format_display), currentAppLocale);
        String formattedDate = dateFormat.format(new Date());
        dateTextView.setText(formattedDate); 

        addIncome.setOnClickListener(v -> {
            if (take.getText().toString().equals("") || tag.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "Enter valid amount and tag.", Toast.LENGTH_SHORT).show();
            } else {
                addTake();
            }
        });

        thrifty.setOnClickListener (v -> {
            Intent dashIntent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(dashIntent);
            finish();
        });

    }
    public void addTake () {
        Transactions t = new Transactions ();
        t.setExin (1);
        t.setAmount (Long.parseLong (take.getText ().toString ()));
        t.setTag (tag.getText ().toString ());
        t.setUid (Integer.parseInt (Utils.userId));
        databaseHelper.insertTransaction (t);
        Toast.makeText (getApplicationContext (),"Added Income", Toast.LENGTH_SHORT).show ();
        databaseHelper.getTransactions ();
        Intent intent = new Intent (getApplicationContext (), TransactionsActivity.class);
        startActivity (intent);
        finish ();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent (getApplicationContext (),TransactionsActivity.class);
        startActivity (intent);
        finish ();
    }
}
