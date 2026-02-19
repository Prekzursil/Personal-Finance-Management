package com.thriftyApp;

import android.annotation.SuppressLint;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.thriftyApp.BaseActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import androidx.recyclerview.widget.RecyclerView; // Added import
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import android.util.TypedValue;
import com.thriftyApp.CustomPieChartRenderer;
import com.thriftyApp.ChartUtils;
import com.github.mikephil.charting.formatter.PercentFormatter;
import android.graphics.Typeface;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.thriftyApp.ThemeUtils;
import com.thriftyApp.LocaleUtils;
import java.util.ArrayList;
import com.thriftyApp.TransactionAdapter;
import com.thriftyApp.Transactions;

public class TransactionsActivity extends BaseActivity {

    private boolean flagFloatingButton;
    private Button scan, take, pay, list, income, graph, download;
    private ListView myListView;
    private DatabaseHelper databaseHelper;
    private PieChart pieChart;
    private RecyclerView legendRecycler;
    private LegendAdapter legendAdapter;
    // private TextView pieCenterTextT; // Removed
    private FloatingActionButton floatingActionButtonT;
    private Spinner timeFilterSpinner;
    private String currentFilterStartDate;
    private String currentFilterEndDate;

    private int contrast(int color) {
        double y = (299 * Color.red(color)
                  + 587 * Color.green(color)
                  + 114 * Color.blue(color)) / 1000.0;
        return (y >= 128) ? Color.BLACK : Color.WHITE;
    }

    private String labelFor(String code) {
        switch (code) {
            case "Transport": return getString(R.string.cat_transport);
            case "Food":      return getString(R.string.cat_food);
            case "Bills":     return getString(R.string.cat_bills);
            case "Sports":    return getString(R.string.cat_sports);
            case "Home":      return getString(R.string.cat_home);
            case "Pets":      return getString(R.string.cat_pets);
            case "Education": return getString(R.string.cat_education);
            case "Travel":    return getString(R.string.cat_travel);
            case "Beauty":    return getString(R.string.cat_beauty);
            case "Kids":      return getString(R.string.cat_kids);
            case "Healthcare":return getString(R.string.cat_healthcare);
            case "Movie":     return getString(R.string.cat_movie);
            default:          return code;
        }
    }

    public void FloatingButtonToggle(View view) {
        flagFloatingButton = !flagFloatingButton;
        int vis = flagFloatingButton ? View.VISIBLE : View.INVISIBLE;
        scan.setVisibility(vis);
        take.setVisibility(vis);
        pay.setVisibility(vis);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Dashboard.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        // if returning from an edit, show list directly
        boolean fromAll = getIntent().getBooleanExtra("from_all", false);
        if (fromAll) {
            listView(null);
        }

        databaseHelper        = new DatabaseHelper(this);
        pieChart              = findViewById(R.id.pieChartExpenses);
        pieChart.setRenderer(new CustomPieChartRenderer(pieChart, pieChart.getAnimator(), pieChart.getViewPortHandler()));
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(16f);
        pieChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        legendRecycler        = findViewById(R.id.legendRecycler);
        // pieCenterTextT        = findViewById(R.id.pieCenterTextT); // Removed
        floatingActionButtonT = findViewById(R.id.floatingActionButtonT);
        scan                  = findViewById(R.id.scanButtonT);
        take                  = findViewById(R.id.takeButtonT);
        pay                   = findViewById(R.id.payButtonT);
        list                  = findViewById(R.id.listViewTab);
        graph                 = findViewById(R.id.graphViewTab);
        income                = findViewById(R.id.incomeGraphViewTab);
        download              = findViewById(R.id.downloadButton);
        myListView            = findViewById(R.id.transactionsListViewT);
        timeFilterSpinner     = findViewById(R.id.timeFilterSpinner);

        // hide scan/take/pay initially
        scan.setVisibility(View.INVISIBLE);
        take.setVisibility(View.INVISIBLE);
        pay.setVisibility(View.INVISIBLE);

        View topNavBar = findViewById(R.id.top_navigation_bar);
        TextView home    = topNavBar.findViewById(R.id.nav_home_text);
        TextView alert   = topNavBar.findViewById(R.id.nav_alerts_text);
        TextView options = topNavBar.findViewById(R.id.nav_options_text);

        // All top tabs are inactive in TransactionsActivity
        if (home != null) home.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        if (alert != null) alert.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        if (options != null) options.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        // default expense chart
        graphView(null);

        scan.setOnClickListener(v -> { startActivity(new Intent(this, scanActivity.class)); finish(); });
        take.setOnClickListener(v -> { startActivity(new Intent(this, TakeActivity.class)); finish(); });
        pay.setOnClickListener(v -> { startActivity(new Intent(this, PayActivity.class)); finish(); });
        download.setOnClickListener(v -> {
            Intent downloadIntent = new Intent(this, Download.class);
            // Pass the current filter dates to Download activity
            if (currentFilterStartDate != null && currentFilterEndDate != null) {
                downloadIntent.putExtra("START_DATE", currentFilterStartDate);
                downloadIntent.putExtra("END_DATE", currentFilterEndDate);
            }
            // If dates are null, Download activity will handle it as "All Time"
            startActivity(downloadIntent);
            // finish(); // Consider if finishing TransactionsActivity is desired here
        });

        graph.setOnClickListener(this::graphView);
        income.setOnClickListener(this::incomeGraphView);
        list.setOnClickListener(this::listView);

        if (options != null) {
            options.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }
        if (alert != null) {
            alert.setOnClickListener(v -> { startActivity(new Intent(this, AlertsActivity.class)); finish(); });
        }
        if (home != null) {
            home.setOnClickListener(v -> { startActivity(new Intent(this, Dashboard.class)); finish(); });
        }

        populateTimeFilterSpinner();
        Intent intent = getIntent();
        String initialStartDate = intent.getStringExtra("START_DATE");
        String initialEndDate = intent.getStringExtra("END_DATE");

        if (initialStartDate != null && initialEndDate != null) {
            currentFilterStartDate = initialStartDate;
            currentFilterEndDate = initialEndDate;
            // Set spinner to the corresponding period if possible, or default
            // This part can be complex if trying to match specific month strings.
            // For now, we'll just use the dates and the spinner will default or be set by user.
            // A more robust solution would parse these dates and find the matching spinner option.
        } else {
            updateDateFilter(getString(R.string.filter_current_month)); // Default
        }
        // Set spinner selection after populating and potentially receiving intent extras
        setSpinnerSelectionFromDates(currentFilterStartDate, currentFilterEndDate);


        timeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = (String) parent.getItemAtPosition(position);
                updateDateFilter(selectedOption);
                // Refresh the currently active view (list or chart)
                if (myListView.getVisibility() == View.VISIBLE) {
                    listView(null); // Reload list view
                } else { // If list is not visible, a chart must be. Refresh based on current chart title.
                    // We get the current chart title from pieChart.getCenterText() if needed,
                    // but simpler to just redraw the one that was active.
                    // For now, assume if not list, it's one of the charts.
                    // The specific chart (income/expense) is determined by which method was last called.
                    // This logic might need refinement if we want to be more precise about which chart to refresh.
                    // A simple way is to check which button/tab is "active" or store current chart type.
                    // For now, let's assume graphView() or incomeGraphView() will be called appropriately by user action.
                    // If current chart is expense chart:
                    if (pieChart.getCenterText() != null && pieChart.getCenterText().equals(getString(R.string.title_expenses))) {
                         graphView(null);
                    } else if (pieChart.getCenterText() != null && pieChart.getCenterText().equals(getString(R.string.title_income))) {
                         incomeGraphView(null);
                    } else {
                        // Default to expense chart if unsure or if center text is not set yet (e.g. initial load)
                        graphView(null);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        pieChart.setTouchEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            private List<Integer> originalLegendFormColors = new ArrayList<>();
            private boolean originalColorsStored = false;

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry)e;
                if (pe == null) return;

                // Show label in Snackbar
                if (pe.getLabel() != null && !pe.getLabel().isEmpty()) {
                    Snackbar.make(pieChart, pe.getLabel(), Snackbar.LENGTH_SHORT).show();
                }
                // Highlight the selected slice
                pieChart.highlightValue(h);
                // Notify adapter to update legend item style
                if (legendAdapter != null && h != null) { // Added null check for h
                    legendAdapter.setSelectedPosition((int) h.getX());
                }
            }

            @Override public void onNothingSelected() {
                // Clear highlight when nothing is selected
                pieChart.highlightValue(null);
                // Notify adapter to clear selection style
                if (legendAdapter != null) {
                    legendAdapter.clearSelection();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String theme = PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("pref_theme", "system");
        ThemeUtils.applyTheme(theme);
        String lang = PreferenceManager.getDefaultSharedPreferences(this)
                      .getString("pref_language", "system");
        LocaleUtils.applyLocale(this, lang);
    }


    private void populateTimeFilterSpinner() {
        ArrayList<String> filterOptions = new ArrayList<>();
        filterOptions.add(getString(R.string.filter_current_month));
        filterOptions.add(getString(R.string.filter_all_time));

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
                Log.e("TransactionsActivity", "Error parsing month for spinner: " + monthYearStr, e);
                filterOptions.add(monthYearStr);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeFilterSpinner.setAdapter(adapter);
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
                Log.e("TransactionsActivity", "Error parsing selected month: " + selectedOption, e);
                currentFilterStartDate = null;
                currentFilterEndDate = null;
            }
        }
    }

    private void setSpinnerSelectionFromDates(String startDate, String endDate) {
        if (startDate == null && endDate == null) {
            timeFilterSpinner.setSelection(getSpinnerIndex(getString(R.string.filter_all_time)));
            return;
        }
        try {
            SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date checkDate = dbDateFormat.parse(startDate);
            Calendar cal = Calendar.getInstance();
            if (checkDate != null) {
                cal.setTime(checkDate);

                // Check for current month
                Calendar currentCal = Calendar.getInstance();
                if (cal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH)) {
                    timeFilterSpinner.setSelection(getSpinnerIndex(getString(R.string.filter_current_month)));
                    return;
                }
                // Check for specific month
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                String displayMonth = displayFormat.format(cal.getTime());
                timeFilterSpinner.setSelection(getSpinnerIndex(displayMonth));
            }
        } catch (java.text.ParseException e) {
            Log.e("TransactionsActivity", "Error parsing date for spinner selection", e);
            timeFilterSpinner.setSelection(getSpinnerIndex(getString(R.string.filter_current_month))); // Default
        }
    }

    private int getSpinnerIndex(String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) timeFilterSpinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(value)) {
                    return i;
                }
            }
        }
        return 0; // Default to first item (Current Month) if not found
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


    // Modified to use filtered data
    private void setTList(String startDate, String endDate) {
        // This method was originally using databaseHelper.getTransactions() which returns ArrayList<String>
        // For consistency and to use the TransactionAdapter, we should use getTransactionsPDF
        ArrayList<Transactions> listData = databaseHelper.getTransactionsPDF(startDate, endDate);
        if (listData.isEmpty()) {
            Toast.makeText(this, "No transactions for selected period.", Toast.LENGTH_LONG).show();
            myListView.setAdapter(null); // Clear the list
            download.setEnabled(false); // Assuming download button should be disabled
        } else {
            myListView.setAdapter(new TransactionAdapter(this, listData));
            download.setEnabled(true);
        }
    }

    // Modified to use filtered data
    public void graphView(View view) {
        // TypedValue tv = new TypedValue(); // Handled by ChartUtils
        // getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true); // Handled by ChartUtils
        // int onBg = tv.data; // Handled by ChartUtils
        // pieChart.setExtraOffsets(10f, 10f, 10f, 10f); // Handled by ChartUtils

        pieChart.setCenterText(getString(R.string.title_expenses)); // Set title as chart center text
        pieChart.setVisibility(View.VISIBLE);
        legendRecycler.setVisibility(View.VISIBLE); // Show legend
        myListView.setVisibility(View.INVISIBLE);

        // pieChart.setDrawHoleEnabled(false); // Handled by ChartUtils, now true for donut
        // pieChart.getLegend().setEnabled(false); // Handled by ChartUtils
        // pieChart.getDescription().setEnabled(false); // Handled by ChartUtils

        HashMap<String,Integer> hash = databaseHelper.getExpenses(currentFilterStartDate, currentFilterEndDate);
        List<PieEntry> entries = new ArrayList<>();
        if (hash.isEmpty()) {
            Toast.makeText(this, "No expenses for selected period.", Toast.LENGTH_SHORT).show();
            pieChart.clear(); // Clear chart if no data
            pieChart.invalidate();
            return;
        }

        float totalValueExpenses = 0f;
        for (Map.Entry<String,Integer> e : hash.entrySet()) {
            totalValueExpenses += e.getValue();
        }
        if (totalValueExpenses == 0) totalValueExpenses = 1f; // Avoid division by zero for percentages

        for (Map.Entry<String,Integer> e : hash.entrySet()) {
            //float percentage = (e.getValue() / totalValueExpenses) * 100f; // Percentage calculation removed from here
            String categoryName = labelFor(e.getKey());
            // String legendLabel = String.format(Locale.getDefault(), "%.0f%% %s", percentage, categoryName); // No longer pre-format with % for legend
            // The PieEntry label is used by the legend directly, and by ValueFormatter if legend is off.
            // ValueFormatter in ChartUtils will add the percentage.
            // Store color in PieEntry data for the legend adapter
            // This assumes ds.getColors() will be populated before this loop,
            // or we need to assign colors here. Let's assign them here for clarity.
            // We'll use the same ColorTemplate.JOYFUL_COLORS as an example.
            // The actual color assignment will happen when creating the PieDataSet.
            PieEntry pieEntry = new PieEntry(e.getValue(), categoryName);
            // We will set data (color) after creating the dataset and getting its colors.
            entries.add(pieEntry);
        }

        // --- REMOVED: Hide labels for small slices (< 5%) ---
        // if (totalValueExpenses > 0) { // Avoid division by zero
        //     for (PieEntry entry : entries) {
        //         if ((entry.getValue() / totalValueExpenses) < 0.05f) {
        //             entry.setLabel(""); // Hide label for small slices
        //         }
        //     }
        // }
        // The logic for hiding small slice labels by setting pe.setLabel("")
        // This will now correctly hide the category name from the legend for small slices.
        // However, with the legend showing for >3 slices, this might be less critical
        // as the legend itself will list all items.
        // If we still want to hide tiny items from the legend:
        /*
        ArrayList<PieEntry> filteredEntries = new ArrayList<>();
        for (PieEntry pe : entries) {
            if ((pe.getValue() / totalValueExpenses) >= 0.05f) {
                filteredEntries.add(pe);
            }
        }
        PieDataSet ds = new PieDataSet(filteredEntries.isEmpty() ? entries : filteredEntries, ""); // Use original if all are small
        */
        // For now, let's show all in legend and rely on minAngleForSlices.
        // The old small slice label hiding logic is removed as labels are now pre-formatted for legend.
        PieDataSet ds = new PieDataSet(entries, "");
        // ds.setSliceSpace(2f); // Handled by ChartUtils (set to 3f)
        // ds.setSelectionShift(5f); // Can be kept if specific, ChartUtils doesn't set this.
        // ds.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE); // Handled by ChartUtils
        // ds.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE); // Handled by ChartUtils
        // ds.setValueLinePart1OffsetPercentage(80f); // Handled by ChartUtils
        // ds.setValueLinePart1Length(0.3f); // Handled by ChartUtils
        // ds.setValueLinePart2Length(0.4f); // Handled by ChartUtils
        // ChartUtils will use MATERIAL_COLORS by default if ds.setColors() is not called here.
        // If specific colors are needed for expense chart, set them here. Otherwise, let ChartUtils handle it.
        ds.setColors(ColorTemplate.JOYFUL_COLORS); // Using JOYFUL_COLORS for expense chart

        // After setting colors to dataset, update PieEntry data with these colors
        List<Integer> colors = ds.getColors();
        List<PieEntry> legendEntries = new ArrayList<>();
        for(int i = 0; i < entries.size() && i < colors.size(); i++) {
            PieEntry originalEntry = entries.get(i);
            originalEntry.setData(colors.get(i)); // Set color for the main chart entry

            // Create a new entry for the legend with the percentage as its value
            float percentage = (originalEntry.getValue() / totalValueExpenses) * 100f;
            PieEntry legendEntry = new PieEntry(percentage, originalEntry.getLabel());
            legendEntry.setData(colors.get(i)); // Also set color for legend entry
            legendEntries.add(legendEntry);
        }
        
        // Set dynamic text color for slice labels
        List<Integer> valueTextColors = new ArrayList<>();
        for (int color : colors) {
            valueTextColors.add(contrast(color));
        }
        ds.setValueTextColors(valueTextColors);

        PieData pd = new PieData(ds); 
        pieChart.getLegend().setEnabled(false); 
        ChartUtils.configurePieChart(pieChart, pd, ds, this); 
                                                                
        // Re-apply styles that ChartUtils might override or to set specific styles
        ds.setValueTextColors(valueTextColors); // Apply contrast-based text colors
        ds.setValueTypeface(Typeface.DEFAULT_BOLD); // Ensure bold typeface for values
        ds.setValueTextSize(14f); // Set a specific text size for slice values, e.g., 14f

        // The LegendAdapter is already receiving legendEntries with percentages.
        // The chart's PieDataSet (ds) uses raw values.
        // ChartUtils sets usePercentValues(true) and a ValueFormatter that now shows "%.1f%%".

        legendAdapter = new LegendAdapter(legendEntries, position -> {
            // Highlighting logic: position is the index in legendEntries
            // We need to find the corresponding entry in the main chart's dataset if they can differ
            // For now, assume a direct mapping if legendEntries are derived in order from chart entries
            if (position < ds.getEntryCount()) { 
                 pieChart.highlightValue(position, 0); // Highlight slice in chart
            }
            legendAdapter.setSelectedPosition(position); // Highlight item in legend
        });
        legendRecycler.setLayoutManager(new FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP));
        legendRecycler.setAdapter(legendAdapter);
        legendAdapter.submitList(legendEntries); 
    }

    // Modified to use filtered data
    public void incomeGraphView(View view) {
        // TypedValue tv = new TypedValue(); // Handled by ChartUtils
        // getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true); // Handled by ChartUtils
        // int onBg = tv.data; // Handled by ChartUtils
        // pieChart.setExtraOffsets(10f, 10f, 10f, 10f); // Handled by ChartUtils

        // pieCenterTextT.setVisibility(View.VISIBLE); // External TextView for title is now redundant
        // pieCenterTextT.setText(R.string.title_income);
        pieChart.setVisibility(View.VISIBLE);
        myListView.setVisibility(View.INVISIBLE);

        // pieChart.setDrawHoleEnabled(false); // Handled by ChartUtils, now true for donut
        // pieChart.getLegend().setEnabled(false); // Handled by ChartUtils
        // pieChart.getDescription().setEnabled(false); // Handled by ChartUtils
        pieChart.setCenterText(getString(R.string.title_income)); // Set title as chart center text
        legendRecycler.setVisibility(View.VISIBLE); // Show legend

        HashMap<String,Integer> hash = databaseHelper.getIncomes(currentFilterStartDate, currentFilterEndDate);
        List<PieEntry> entries = new ArrayList<>();
        if (hash.isEmpty()) {
            Toast.makeText(this, "No income for selected period.", Toast.LENGTH_SHORT).show();
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        float totalValueIncome = 0f;
        for (Map.Entry<String,Integer> e : hash.entrySet()) {
            totalValueIncome += e.getValue();
        }
        if (totalValueIncome == 0) totalValueIncome = 1f;

        for (Map.Entry<String,Integer> e : hash.entrySet()) {
            // float percentage = (e.getValue() / totalValueIncome) * 100f; // Percentage calculation removed
            String categoryName = e.getKey(); // Income categories might not use labelFor if they are direct
            // String legendLabel = String.format(Locale.getDefault(), "%.0f%% %s", percentage, categoryName);
            PieEntry pieEntry = new PieEntry(e.getValue(), categoryName);
            entries.add(pieEntry);
        }

        // --- REMOVED: Hide labels for small slices (< 5%) ---
        // if (totalValueIncome > 0) { // Avoid division by zero
        //     for (PieEntry entry : entries) {
        //         if ((entry.getValue() / totalValueIncome) < 0.05f) {
        //             entry.setLabel(""); // Hide label for small slices
        //         }
        //     }
        // }

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(ColorTemplate.COLORFUL_COLORS); // Using COLORFUL_COLORS for income chart

        // After setting colors to dataset, update PieEntry data with these colors
        List<Integer> colorsIncome = ds.getColors();
        List<PieEntry> legendEntriesIncome = new ArrayList<>();
        for(int i = 0; i < entries.size() && i < colorsIncome.size(); i++) {
            PieEntry originalEntry = entries.get(i);
            originalEntry.setData(colorsIncome.get(i));

            float percentage = (originalEntry.getValue() / totalValueIncome) * 100f;
            PieEntry legendEntry = new PieEntry(percentage, originalEntry.getLabel());
            legendEntry.setData(colorsIncome.get(i));
            legendEntriesIncome.add(legendEntry);
        }

        // Set dynamic text color for slice labels
        List<Integer> valueTextColorsIncome = new ArrayList<>();
        for (int color : colorsIncome) {
            valueTextColorsIncome.add(contrast(color));
        }
        ds.setValueTextColors(valueTextColorsIncome);

        PieData pd = new PieData(ds); 
        pieChart.getLegend().setEnabled(false); 
        ChartUtils.configurePieChart(pieChart, pd, ds, this);
        
        // Re-apply styles that ChartUtils might override or to set specific styles
        ds.setValueTextColors(valueTextColorsIncome); // Apply contrast-based text colors
        ds.setValueTypeface(Typeface.DEFAULT_BOLD); // Ensure bold typeface for values
        ds.setValueTextSize(14f); // Set a specific text size for slice values, e.g., 14f

        // The LegendAdapter is already receiving legendEntriesIncome with percentages.
        // The chart's PieDataSet (ds) uses raw values.
        // ChartUtils sets usePercentValues(true) and a ValueFormatter that now shows "%.1f%%".

        legendAdapter = new LegendAdapter(legendEntriesIncome, position -> {
            if (position < ds.getEntryCount()) {
                pieChart.highlightValue(position, 0); // Highlight slice in chart
            }
            legendAdapter.setSelectedPosition(position); // Highlight item in legend
        });
        legendRecycler.setLayoutManager(new FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP));
        legendRecycler.setAdapter(legendAdapter);
        legendAdapter.submitList(legendEntriesIncome);
    }

    // Modified to use filtered data
    public void listView(View view) {
        pieChart.setVisibility(View.INVISIBLE);
        legendRecycler.setVisibility(View.GONE); // Hide legend
        myListView.setVisibility(View.VISIBLE);
        // pieCenterTextT.setVisibility(View.GONE); // External TextView for title is now redundant
        setTList(currentFilterStartDate, currentFilterEndDate);
    }
}
