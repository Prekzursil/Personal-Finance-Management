package com.thriftyApp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import com.thriftyApp.BaseActivity;
import com.thriftyApp.Transactions;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class PayActivity extends BaseActivity {

    EditText pay, tag;
    FloatingActionButton addExpense;
    DatabaseHelper databaseHelper;
    TextView thrifty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_pay);


        databaseHelper = new DatabaseHelper (this);
        pay = findViewById (R.id.payEditText);
        tag = findViewById (R.id.tagEditText);
        addExpense =  findViewById (R.id.floatingActionButtonPay);

        thrifty = findViewById (R.id.thriftyTitlePay);
        findViewById(R.id.close_pay).setOnClickListener(
                new View.OnClickListener () {

                    @Override
                    public void onClick(View arg0) {

                        onBackPressed ();

                    }
                });

        Intent intent = getIntent ();
        // OCR input
        if (intent.getStringExtra ("ocr") != null) {
            String str = intent.getStringExtra ("ocr");
            pay.setText (str);
        }
        // Edit mode if transaction_id passed
        int editId = intent.getIntExtra("transaction_id", -1);
        if (editId != -1) {
            long amountVal = intent.getLongExtra("amount", 0);
            pay.setText(String.valueOf(amountVal));
            tag.setText(intent.getStringExtra("tag"));
            addExpense.setImageResource(android.R.drawable.ic_menu_save);
                addExpense.setOnClickListener(v -> {
                    Transactions t = new Transactions();
                    t.setTid(editId);
                    t.setExin(0);
                    Double d2 = Double.parseDouble(pay.getText().toString());
                    t.setAmount(Math.round(d2));
                    t.setTag(tag.getText().toString());
                    String created = intent.getStringExtra("created_at");
                    t.setCreated_at(created != null ? created :
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                    databaseHelper.updateTransaction(t);
                    boolean fromAll = intent.getBooleanExtra("from_all", false);
                    Intent returnIntent = new Intent(getApplicationContext(),
                        fromAll ? TransactionsActivity.class : Dashboard.class);
                    startActivity(returnIntent);
                    finish();
                });
        }

        TextView dateTextView =  findViewById (R.id.dateTextViewPay);
        Locale currentAppLocale = LocaleUtils.getResolvedLocale(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format_display), currentAppLocale);
        String formattedDate = dateFormat.format(new Date());
        dateTextView.setText(formattedDate);

        if (editId == -1) {
            addExpense.setOnClickListener(v -> {
                if (pay.getText().toString().isEmpty() || tag.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(),"Enter valid amount and tag.",Toast.LENGTH_SHORT).show();
                } else {
                    addPay();
                }
            });
        }


        thrifty.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (getApplicationContext (), Dashboard.class);
                startActivity (intent);
                finish ();
            }
        });


        findViewById (R.id.transportImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_transport));
            }
        });
        findViewById (R.id.travelImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_travel));
            }
        });
        findViewById (R.id.foodImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_food));
            }
        });
        findViewById (R.id.billsImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_bills));
            }
        });
        findViewById (R.id.sportsImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_sports));
            }
        });
        findViewById (R.id.homeImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_home));
            }
        });
        findViewById (R.id.petsImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_pets));
            }
        });
        findViewById (R.id.educationImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_education));
            }
        });
        findViewById (R.id.beautyImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_beauty));
            }
        });
        findViewById (R.id.kidsImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_kids));
            }
        });
        findViewById (R.id.healthImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_healthcare));
            }
        });
        findViewById (R.id.movieImageViewP).setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                tag.setText (getString(R.string.cat_movie));
            }
        });
    }

    public void addPay () {
        Transactions t = new Transactions ();
        t.setExin (0);
        Double d = Double.parseDouble (pay.getText ().toString ());
        Log.i("Omg", d.toString ());
        long l = Math.round (d);
        t.setAmount (l);
        t.setTag (tag.getText ().toString ());
        t.setUid (Integer.parseInt (Utils.userId));
        databaseHelper.insertTransaction (t);
        databaseHelper.getTransactions ();
        databaseHelper.setIncomeExpenses (null, null);
        int exp = Utils.expense;
        int bud = Integer.parseInt (Utils.budget);
        Log.i("Alert build", bud*0.5 +" "+ exp);
        if (exp > (bud/2)) {
            Toast.makeText (getApplicationContext (),"Added Expense", Toast.LENGTH_SHORT).show ();
            buildAlertExpense ( );
        }
        else {
            Intent intent = new Intent (getApplicationContext ( ), TransactionsActivity.class);
            startActivity (intent);
            finish ( );
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent (getApplicationContext (),TransactionsActivity.class);
        startActivity (intent);
        finish ();
    }


    public void buildAlertExpense () {


            AlertDialog.Builder builder = new AlertDialog.Builder (this);
            builder.setCancelable (true);
            builder.setTitle ("Alert");
            builder.setMessage ("Spend money wisely. More than half of the allocated budget amount has already been spent in this month." );
            builder.setPositiveButton ("OK", new DialogInterface.OnClickListener ( ) {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText (getApplicationContext (),"Spend Less, Save more.",Toast.LENGTH_SHORT).show ();
                    Intent intent = new Intent (getApplicationContext (), TransactionsActivity.class);
                    startActivity (intent);
                    finish ();
                }
            });
            builder.show ();

    }
}
