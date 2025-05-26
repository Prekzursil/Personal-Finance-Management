/*
 *  ChartUtils.java  — one-stop styling helper for all PieCharts
 *
 *  ✔  Percentage shown first:  “77 %  Income”
 *  ✔  Theme-aware text colour (white in Dark, black in Light)
 *  ✔  Adaptive behaviour:
 *         • 1–2 slices  →  label drawn INSIDE the slice (dashboard)
 *         • 3+ slices   →  label OUTSIDE with shorter connector lines
 *  ✔  Never lets the pie slip off-screen
 *
 *  Place this in the same package as your Activities/renderer.
 *  Change the package line below if your project uses a different name.
 */
package com.thriftyApp;   // <-- adjust if your package differs

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate; // Added import
import com.github.mikephil.charting.utils.Utils;

import java.util.Locale;

public final class ChartUtils {

    private ChartUtils() { /* static class */ }

    // Using a predefined vibrant template as a base, can be overridden by calling activity
    // private static final int[] CUSTOM_PIE_CHART_COLORS = { ... }; // Removed custom array for now

    /**
     * Call this right after you create <br>
     * <code>PieData data = new PieData(dataSet);</code>
     */
    public static void configurePieChart(PieChart chart,
                                         PieData  data,
                                         PieDataSet ds,
                                         Context  ctx) {

        /* ------------------------------------------------------------------
         * 1)  Foreground colour that flips with the theme
         * ------------------------------------------------------------------ */
        TypedValue tv = new TypedValue();
        ctx.getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true);
        final int onBg = tv.data;

        /* ------------------------------------------------------------------
         * 2)  Base DataSet settings (common to every pie)
         * ------------------------------------------------------------------ */
        chart.setUsePercentValues(true);              // formatter receives 0-100 %
        ds.setDrawValues(true);
        ds.setValueTextColor(onBg);
        ds.setValueTypeface(Typeface.DEFAULT_BOLD);
        ds.setValueTextSize(15f);                     // sp – good default on phones
        // Ensure entry labels (if ever enabled) also use theme color
        // These are chart level, not dataset level
        // ds.setEntryLabelColor(onBg); // Incorrect: Belongs to chart
        // ds.setEntryLabelTextSize(12f); // Incorrect: Belongs to chart
        chart.setEntryLabelColor(onBg); // Corrected: Call on chart
        chart.setEntryLabelTextSize(12f); // Corrected: Call on chart

        // Set default colors if not already set on the dataset
        if (ds.getColors() == null || ds.getColors().isEmpty() || ds.getColors().get(0) == ColorTemplate.COLOR_NONE) {
            ds.setColors(ColorTemplate.JOYFUL_COLORS); // Using JOYFUL_COLORS as a vibrant default
        }

        ds.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float pct, PieEntry e) {
                // Display only the percentage with one decimal place.
                // The category label will be shown in the RecyclerView legend.
                return String.format(Locale.getDefault(), "%.1f%%", pct);
            }
        });

        /* ------------------------------------------------------------------
         * 3)  Decide INSIDE vs OUTSIDE depending on slice count
         * ------------------------------------------------------------------ */
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        final float dp5  = Utils.convertDpToPixel(5f);
        final float dp10 = Utils.convertDpToPixel(10f);

        // NEW LOGIC: Labels inside for 1-4 slices
        if (ds.getEntryCount() <= 4) {
            /* --------------- INSIDE LABELS (1-4 slices) --------------- */
            ds.setValueTextSize(14f); // Decreased text size further for inside labels for more space
            ds.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
            ds.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
            ds.setValueLinePart1Length(0f);
            ds.setValueLinePart2Length(0f);
            ds.setSliceSpace(0f);                     // no radial gaps
            chart.setExtraOffsets(0f, 0f, 0f, 0f);    // use full width/height
            chart.getLegend().setEnabled(false);      // Explicitly disable legend for inside labels
        } else {
            /* --------------- OUTSIDE LABELS + LEGEND (5+ slices) ------------ */
            ds.setValueTextSize(16f); // Text size for outside labels
            ds.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            ds.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

            // Connector lines settings (remain the same)
            ds.setValueLinePart1OffsetPercentage(75f);
            ds.setValueLinePart1Length(0.40f);
            ds.setValueLinePart2Length(0.33f);
            ds.setValueLineWidth(2f); // Thicker line
            // Line color will be set based on theme below

            ds.setSliceSpace(3f); // Increased for better separation

            // --- Logic for 5+ slices (Legend is now handled by RecyclerView) ---
            // Ensure built-in legend is disabled, as RecyclerView is used.
            chart.getLegend().setEnabled(false);

            // Values on slices are not drawn when RecyclerView legend is used (as per previous logic)
            ds.setDrawValues(false);
            // If values were outside, lines would also be hidden.
            // ds.setValueLinePart1Length(0f);
            // ds.setValueLinePart2Length(0f);

            // Set minimal offsets, actual spacing for RecyclerView legend is handled in XML layout
            // and by RecyclerView's own item margins.
            // The fabClearance was an attempt to make space for FAB *within* chart offsets,
            // but it's better handled by constraining/margining the RecyclerView itself.
            chart.setExtraOffsets(dp5, dp10, dp5, dp10); // Minimal offsets

            // Set line color based on theme (good practice, though lines are not drawn here)
            boolean isDarkTheme = Color.luminance(onBg) > 0.5;
            ds.setValueLineColor(isDarkTheme ? Color.DKGRAY : Color.GRAY);


            // The logic for hiding small slice labels (entry.setLabel("")) is best handled in the Activity
            // before creating PieEntry objects, as ChartUtils receives an already formed DataSet.
            /*
            if (data != null && data.getDataSet() instanceof PieDataSet) { // Start of commented block
                PieDataSet pieDataSet = (PieDataSet) data.getDataSet();
                float total = 0f;
                for (int i = 0; i < pieDataSet.getEntryCount(); i++) {
                    total += pieDataSet.getEntryForIndex(i).getValue();
                }
                if (total > 0) { // Avoid division by zero
                    for (int i = 0; i < pieDataSet.getEntryCount(); i++) {
                        PieEntry entry = pieDataSet.getEntryForIndex(i);
                        if ((entry.getValue() / total) < 0.05f) { // Less than 5%
                            // To "hide" the label, the formatter should return an empty string for it.
                            // The current formatter formats based on PieEntry.getLabel().
                            // So, we'd need to modify the PieEntry's label itself before formatting.
                            // This is tricky as PieEntry objects are often created outside ChartUtils.
                            // An alternative is a more complex formatter or drawing logic in CustomPieChartRenderer.
                            // For now, the existing formatter will still show "0% Label" if value is too small.
                            // The logic from TransactionsActivity to setLabel("") on PieEntry is better if applied before calling this.
                            // Let's assume for now the calling code (TransactionsActivity) handles setting empty labels for small slices.
                        }
                    }
                }
            }
            */ // End of commented block
        }

        /* ------------------------------------------------------------------
         * 4)  Final chart-level tweaks
         * ------------------------------------------------------------------ */
        // Configure as Donut Chart
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT); // Or use a theme-aware background color
        chart.setHoleRadius(45f); // Example, adjust as needed
        chart.setTransparentCircleRadius(50f); // Example, adjust for border effect
        chart.setDrawCenterText(true);
        chart.setCenterTextSize(18f); // Prominent title size
        chart.setCenterTextColor(onBg);
        chart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);

        chart.setData(data);
        chart.setDrawEntryLabels(false);              // we draw a combined string
        // chart.setDrawHoleEnabled(false); // Removed, now a donut
        chart.setMinAngleForSlices(15f);              // Group small slices more aggressively

        chart.setNoDataText(ctx.getString(R.string.no_data_for_period));
        chart.setNoDataTextColor(onBg);
        chart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);


        chart.setRenderer(new CustomPieChartRenderer(
                chart, chart.getAnimator(), chart.getViewPortHandler()));

        // chart.getLegend().setEnabled(false); // This line is removed, legend visibility is handled by adaptive logic
        chart.getDescription().setEnabled(false);

        chart.animateXY(800, 800);
        chart.invalidate();
    }
}
