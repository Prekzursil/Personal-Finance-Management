package com.thriftyApp;   // <-- adjust if your package differs

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Draws the value twice – solid fill then a slim outline – so the text
 * remains readable on any slice colour and in both Light & Dark themes.
 */
public class CustomPieChartRenderer extends PieChartRenderer {

    public CustomPieChartRenderer(PieChart chart,
                                  ChartAnimator animator,
                                  ViewPortHandler vph) {
        super(chart, animator, vph);
    }

    @Override
    public void drawValue(Canvas c,
                          String valueText,
                          float x,
                          float y,
                          int color) {

        Paint p = mValuePaint;
        p.setAntiAlias(true);
        p.setTypeface(Typeface.DEFAULT_BOLD);

        /* --- solid fill --- */
        p.setStyle(Paint.Style.FILL);
        // For labels inside slices, always use WHITE fill for better contrast against various slice colors.
        // The 'color' parameter is the general valueTextColor (onBg), which is used for labels outside slices.
        // This renderer is primarily for enhancing text on slices.
        p.setColor(Color.WHITE); // Set fill to WHITE
        super.drawValue(c, valueText, x, y, Color.WHITE); // Pass WHITE as the color to draw with

        /* --- slim outline for contrast --- */
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4f);          // Increased stroke width further for better boldness
        // Always use BLACK stroke for WHITE fill for maximum contrast on slices.
        p.setColor(Color.BLACK);
        super.drawValue(c, valueText, x, y, Color.BLACK); // Pass BLACK as the color for the stroke pass
                                                    // The original super.drawValue uses mValuePaint.getColor().
    }
}
