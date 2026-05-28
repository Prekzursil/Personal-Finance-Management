package com.thriftyApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.thriftyApp.BaseActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener; // Added import
import com.github.mikephil.charting.data.Entry; // Added import
import com.github.mikephil.charting.highlight.Highlight; // Added import
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.thriftyApp.ThemeUtils;
import com.thriftyApp.LocaleUtils;
import com.thriftyApp.TransactionAdapter;
import com.thriftyApp.Transactions;

/**
 * Dashboard activity: shows overall pie chart of income vs. expenses, and recent transactions.
 */
public class Dashboard extends BaseActivity {
    private String lastLanguage;
    private DatabaseHelper databaseHelper;
    private boolean flagFloatingButton;
    private FloatingActionButton floatingActionButton;
    private Button scan, take, pay;
    private PieChart pieChart;
    private RecyclerView legendRecyclerDashboard;
    private LegendAdapter legendAdapterDashboard;
    private TextView income, expense;
    // private TextView pieCenterTextD; // Removed, title is now chart center text
    private TextView userName, logout;
    private BackupManager backupManager;
    private AlertDialog progressDialog;
    private Spinner timeFilterSpinner;
    private String currentFilterStartDate;
    private String currentFilterEndDate;

    /* --------------------------------------------------------------- */
    public void FloatingButtonToggle(View view) {
        if (flagFloatingButton) {
            flagFloatingButton = false;
            scan.setVisibility(View.INVISIBLE);
            take.setVisibility(View.INVISIBLE);
            pay.setVisibility(View.INVISIBLE);
        } else {
            flagFloatingButton = true;
            scan.setVisibility(View.VISIBLE);
            take.setVisibility(View.VISIBLE);
            pay.setVisibility(View.VISIBLE);
        }
    }

    // Contrast helper for pie-chart *values*
    private int contrast(int color) {
        double y = (299 * Color.red(color)
                  + 587 * Color.green(color)
                  + 114 * Color.blue(color)) / 1000.0;
        return (y >= 128) ? Color.BLACK : Color.WHITE;
    }

    /* --------------------------------------------------------------- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        /* ---------- Google-Drive backup bootstrap ---------- */
        bootstrapBackup();

        /* ---------- UI wiring ---------- */
        income   = findViewById(R.id.income);
        expense  = findViewById(R.id.expense);
        logout   = findViewById(R.id.logoutTextView);
        scan     = findViewById(R.id.scanButton);
        take     = findViewById(R.id.takeButton);
        pay      = findViewById(R.id.payButton);
        // hide floating action sub-buttons initially
        scan.setVisibility(View.INVISIBLE);
        take.setVisibility(View.INVISIBLE);
        pay.setVisibility(View.INVISIBLE);
        pieChart = findViewById(R.id.pieChart);
        legendRecyclerDashboard = findViewById(R.id.legendRecyclerDashboard);
        // pieCenterTextD = findViewById(R.id.pieCenterTextD); // Removed
        pieChart.setRenderer(new CustomPieChartRenderer(pieChart, pieChart.getAnimator(), pieChart.getViewPortHandler()));
        databaseHelper     = new DatabaseHelper(this);
        flagFloatingButton = false;
        floatingActionButton = findViewById(R.id.floatingActionButtonD);
        floatingActionButton.setOnClickListener(this::FloatingButtonToggle);

        timeFilterSpinner = findViewById(R.id.timeFilterSpinner);
        populateTimeFilterSpinner();
        timeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = (String) parent.getItemAtPosition(position);
                updateDateFilter(selectedOption);
                refreshData(currentFilterStartDate, currentFilterEndDate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        // Initial data load (e.g., current month by default)
        updateDateFilter(getString(R.string.filter_current_month)); // Assuming you add this string resource
        refreshData(currentFilterStartDate, currentFilterEndDate);


        scan.setOnClickListener(v -> {
            startActivity(new Intent(this, scanActivity.class));
            finish();
        });
        take.setOnClickListener(v -> {
            startActivity(new Intent(this, TakeActivity.class));
            finish();
        });
        pay.setOnClickListener(v -> {
            startActivity(new Intent(this, PayActivity.class));
            finish();
        });

        logout.setOnClickListener(v -> {
            GoogleSignInClient gsc = GoogleSignIn.getClient(
                this, GoogleSignInOptions.DEFAULT_SIGN_IN);
            gsc.signOut().addOnCompleteListener(task -> {
                gsc.revokeAccess();
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                           Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            });
        });

        findViewById(R.id.showAllTextView).setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionsActivity.class);
            if (currentFilterStartDate != null && currentFilterEndDate != null) {
                intent.putExtra("START_DATE", currentFilterStartDate);
                intent.putExtra("END_DATE", currentFilterEndDate);
            }
            // If currentFilterStartDate is null, it implies "All Time", so no extras needed or handle in TransactionsActivity
            startActivity(intent);
            // Consider not finishing Dashboard if you want users to easily return
            // finish();
        });

        // Updated to use IDs from included layout
        setupNavBar();

        // refreshData will call getTList and getTChart
    }

    // Initializes Drive backup from an explicit account extra or the last sign-in.
    private void bootstrapBackup() {
        if (getIntent().hasExtra("google_account")) {
            GoogleSignInAccount account = getIntent().getParcelableExtra("google_account");
            backupManager = new BackupManager(this, account);
            showProgressDialog("Initializing backup...");
            backupManager.performSync(true)
                .addOnSuccessListener(result -> {
                    hideProgressDialog();
                    Toast.makeText(this, "Initial sync completed", Toast.LENGTH_SHORT).show();
                    refreshData(currentFilterStartDate, currentFilterEndDate);
                })
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    Toast.makeText(this, "Initial sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            return;
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            backupManager = new BackupManager(this, account);
            backupManager.performSync(false)
                .addOnSuccessListener(r -> refreshData(currentFilterStartDate, currentFilterEndDate))
                .addOnFailureListener(e -> Log.e("Dashboard", "Background sync failed: " + e.getMessage()));
        }
    }

    // Colors the top nav tabs and wires their click targets.
    private void setupNavBar() {
        View topNavBar = findViewById(R.id.top_navigation_bar);
        TextView homeTab    = topNavBar.findViewById(R.id.nav_home_text);
        TextView optionsTab = topNavBar.findViewById(R.id.nav_options_text);
        TextView alertsTab  = topNavBar.findViewById(R.id.nav_alerts_text);

        setTabColor(homeTab, R.color.secondary); // Active tab
        setTabColor(optionsTab, R.color.text_primary); // Inactive
        setTabColor(alertsTab, R.color.text_primary); // Inactive

        setTabTarget(homeTab, null); // Already on Dashboard: no navigation
        setTabTarget(optionsTab, SettingsActivity.class);
        setTabTarget(alertsTab, AlertsActivity.class);
    }

    private void setTabColor(TextView tab, int colorRes) {
        if (tab != null) {
            tab.setTextColor(ContextCompat.getColor(this, colorRes));
        }
    }

    private void setTabTarget(TextView tab, Class<?> target) {
        if (tab == null) {
            return;
        }
        tab.setOnClickListener(v -> {
            if (target != null) {
                startActivity(new Intent(this, target));
            }
        });
    }


    private void populateTimeFilterSpinner() {
        ArrayList<String> filterOptions = new ArrayList<>();
        filterOptions.add(getString(R.string.filter_current_month)); // Add R.string.filter_current_month = "Current Month"
        filterOptions.add(getString(R.string.filter_all_time));     // Add R.string.filter_all_time = "All Time"

        List<String> distinctMonths = databaseHelper.getDistinctTransactionMonths();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        for (String monthYearStr : distinctMonths) {
            try {
                Date date = dbFormat.parse(monthYearStr);
                if (date != null) {
                    filterOptions.add(displayFormat.format(date));
                }
            } catch (java.text.ParseException e) {
                Log.e("Dashboard", "Error parsing month for spinner: " + monthYearStr, e);
                filterOptions.add(monthYearStr); // Fallback to raw string
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeFilterSpinner.setAdapter(adapter);
        // Set default selection to "Current Month"
        timeFilterSpinner.setSelection(0, false); // false to prevent immediate onItemSelected call if not needed
    }

    private void updateDateFilter(String selectedOption) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        if (selectedOption.equals(getString(R.string.filter_current_month))) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            setTimeToBeginningOfDay(cal);
            currentFilterStartDate = dbDateFormat.format(cal.getTime());

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            setTimeToEndOfDay(cal);
            currentFilterEndDate = dbDateFormat.format(cal.getTime());
        } else if (selectedOption.equals(getString(R.string.filter_all_time))) {
            currentFilterStartDate = null;
            currentFilterEndDate = null;
        } else {
            // Specific month selected (e.g., "May 2025")
            try {
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                Date selectedMonthDate = displayFormat.parse(selectedOption);
                if (selectedMonthDate != null) {
                    cal.setTime(selectedMonthDate);

                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    setTimeToBeginningOfDay(cal);
                    currentFilterStartDate = dbDateFormat.format(cal.getTime());

                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    setTimeToEndOfDay(cal);
                    currentFilterEndDate = dbDateFormat.format(cal.getTime());
                }
            } catch (java.text.ParseException e) {
                Log.e("Dashboard", "Error parsing selected month: " + selectedOption, e);
                // Fallback to "All Time" or handle error
                currentFilterStartDate = null;
                currentFilterEndDate = null;
            }
        }
    }

    private void setTimeToBeginningOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setTimeToEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    // Getter methods for filter dates
    public String getCurrentFilterStartDate() {
        return currentFilterStartDate;
    }

    public String getCurrentFilterEndDate() {
        return currentFilterEndDate;
    }

    /* --------------------------------------------------------------- */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        if (backupManager == null) {
            menu.findItem(R.id.action_sync).setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            showSyncDialog(); return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* ---------- sync-dialog ---------- */
    private void showSyncDialog() {
        if (backupManager == null) {
            Toast.makeText(this,
                "Please sign in with Google to use sync",
                Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Sync Data")
            .setMessage("Choose sync option:")
            .setPositiveButton("Upload to Drive", (d,w) -> {
                showProgressDialog("Uploading backup...");
                backupManager.performSync(false)
                    .addOnSuccessListener(r -> {
                        hideProgressDialog();
                        Toast.makeText(this,
                            "Backup uploaded successfully",
                            Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        hideProgressDialog();
                        Toast.makeText(this,
                            "Upload failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Restore from Drive", (d,w) -> {
                new AlertDialog.Builder(this)
                    .setTitle("Confirm Restore")
                    .setMessage("This will replace your current data. Continue?")
                    .setPositiveButton("Yes", (d2,w2) -> {
                        showProgressDialog("Restoring from backup...");
                        backupManager.performSync(true)
                            .addOnSuccessListener(r -> {
                                hideProgressDialog();
                                refreshData(currentFilterStartDate, currentFilterEndDate);
                                Toast.makeText(this,
                                    "Restore completed successfully",
                                    Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                hideProgressDialog();
                                Toast.makeText(this,
                                    "Restore failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            })
            .setNeutralButton("Cancel", null)
            .show();
    }

    /* ---------- progress-dialog ---------- */
    private void showProgressDialog(String msg) {
        if (isFinishing()) return;
        if (progressDialog != null && progressDialog.isShowing()) {
            try { progressDialog.dismiss(); } catch (Exception ignored) {}
        }
        progressDialog = new AlertDialog.Builder(this)
            .setMessage(msg)
            .setCancelable(false)
            .create();
        try {
            progressDialog.show();
        } catch (Exception e) {
            progressDialog = null;
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            try { progressDialog.dismiss(); } catch (Exception ignored) {}
        }
        progressDialog = null;
    }

    /* --------------------------------------------------------------- */
    // Modified to accept date range
    public void refreshData(String startDate, String endDate) {
        databaseHelper.setIncomeExpenses(startDate, endDate); // Pass filter dates
        income.setText("€ " + Utils.income);
        expense.setText("€ " + Utils.expense);
        getTList(startDate, endDate); // Pass filter dates
        getTChart(); // Uses updated Utils.income/expense
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("Exit me", true);
        startActivity(i);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String theme = PreferenceManager
            .getDefaultSharedPreferences(this)
            .getString("pref_theme", "system");
        ThemeUtils.applyTheme(theme);

        String lang = PreferenceManager
            .getDefaultSharedPreferences(this)
            .getString("pref_language", "system");
        LocaleUtils.applyLocale(this, lang);
    }

    /* ---------- transaction list ---------- */
    // Modified to accept date range and show all filtered transactions
    public void getTList(String startDate, String endDate) {
        ListView listView = findViewById(R.id.transactionsListDash);
        ArrayList<Transactions> filteredTransactions = databaseHelper.getTransactionsPDF(startDate, endDate); // Pass filter dates

        if (filteredTransactions.isEmpty()) {
            Toast.makeText(this,
                "No transactions for selected period.", Toast.LENGTH_LONG).show();
            listView.setAdapter(null); // Clear the list
            return;
        }
        // Display all filtered transactions, not just a subset
        TransactionAdapter adapter = new TransactionAdapter(this, filteredTransactions);
        listView.setAdapter(adapter);
    }

    /* ---------- pie chart ---------- */
    public void getTChart() {
        if (Utils.expense == 0 && Utils.income == 0) {
            pieChart.clear(); // Clear chart if no data
            pieChart.invalidate(); // Refresh chart view
            if (legendAdapterDashboard != null) {
                legendAdapterDashboard.submitList(new ArrayList<>()); // Clear legend
            }
            return;
        }

        List<PieEntry> values = new ArrayList<>();
        List<Integer> sliceColors = new ArrayList<>(); // Use a specific list for colors
        buildPieEntries(values, sliceColors);


        // ----- pie‑chart polish -----
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setEntryLabelColor(Color.BLACK);      // always readable

        PieDataSet set = new PieDataSet(values, "");
        set.setColors(sliceColors); // Use defined colors
        set.setValueTextSize(14f);
        set.setValueTextColor(Color.BLACK);

        // optional: darker slice on touch
        set.setSelectionShift(8f);

        // Let ChartUtils handle common configurations, but ensure our specific settings are applied
        PieData data = new PieData(set);
        pieChart.setCenterText(getString(R.string.summary));
        pieChart.getLegend().setEnabled(false); // Disable MPAndroidChart legend

        // Apply ChartUtils configuration AFTER setting specific dataset properties like colors and text sizes
        ChartUtils.configurePieChart(pieChart, data, set, this);

        // Ensure PieEntry data has the color (already done during creation)
        // List<Integer> colors = set.getColors(); // No longer needed as color is set in setData


        // Setup RecyclerView legend for Dashboard
        setupLegendAdapter(values, data);
        legendAdapterDashboard.submitList(new ArrayList<>(values)); // Update legend data

        // Add OnChartValueSelectedListener to Dashboard's pieChart for bi-directional highlight
        attachChartSelectionListener();
    }

    // Builds the expense/income pie entries (and their colors) from current totals.
    private void buildPieEntries(List<PieEntry> values, List<Integer> sliceColors) {
        float total = Utils.expense + Utils.income; // Calculate total for percentage
        int expenseColor = ContextCompat.getColor(this, R.color.expense_color);
        int incomeColor = ContextCompat.getColor(this, R.color.income_color);
        if (Utils.expense > 0) {
            PieEntry expenseEntry = new PieEntry((Utils.expense / total) * 100f, getString(R.string.title_expenses));
            expenseEntry.setData(expenseColor); // Store color in data
            values.add(expenseEntry);
            sliceColors.add(expenseColor);
        }
        if (Utils.income > 0) {
            PieEntry incomeEntry = new PieEntry((Utils.income / total) * 100f, getString(R.string.title_income));
            incomeEntry.setData(incomeColor); // Store color in data
            values.add(incomeEntry);
            sliceColors.add(incomeColor);
        }
    }

    // Lazily creates the legend adapter, wiring chip clicks to chart highlights.
    private void setupLegendAdapter(List<PieEntry> values, PieData data) {
        if (legendAdapterDashboard != null) {
            return;
        }
        legendAdapterDashboard = new LegendAdapter(new ArrayList<>(values), position -> {
            highlightChartForLegend(values, data, position);
            legendAdapterDashboard.setSelectedPosition(position); // Highlight legend item
        });
        legendRecyclerDashboard.setLayoutManager(new FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP));
        legendRecyclerDashboard.setAdapter(legendAdapterDashboard);
    }

    // Highlights the chart slice matching the tapped legend chip.
    private void highlightChartForLegend(List<PieEntry> values, PieData data, int position) {
        if (values.size() <= position) {
            return;
        }
        String label = values.get(position).getLabel();
        int chartIndex = indexOfLabel(data.getDataSet(), label);
        if (chartIndex != -1) {
            pieChart.highlightValue(chartIndex, 0); // Highlight slice in chart
        }
    }

    private static int indexOfLabel(
            com.github.mikephil.charting.interfaces.datasets.IPieDataSet dataSet, String label) {
        for (int i = 0; i < dataSet.getEntryCount(); i++) {
            if (dataSet.getEntryForIndex(i).getLabel().equals(label)) {
                return i;
            }
        }
        return -1;
    }

    // Wires chart slice selection back to the legend for bi-directional highlight.
    private void attachChartSelectionListener() {
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (legendAdapterDashboard == null || h == null || !(e instanceof PieEntry)) {
                    return;
                }
                List<PieEntry> legendEntries = legendAdapterDashboard.getCurrentList();
                int legendPosition = indexOfLabelInList(legendEntries, ((PieEntry) e).getLabel());
                if (legendPosition != -1) {
                    legendAdapterDashboard.setSelectedPosition(legendPosition);
                    legendRecyclerDashboard.smoothScrollToPosition(legendPosition);
                }
            }

            @Override
            public void onNothingSelected() {
                if (legendAdapterDashboard != null) {
                    legendAdapterDashboard.clearSelection();
                }
            }
        });
    }

    private static int indexOfLabelInList(List<PieEntry> entries, String label) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getLabel().equals(label)) {
                return i;
            }
        }
        return -1;
    }
}
