package com.thriftyApp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.google.mlkit.vision.text.Text;

public class TextGraphic extends GraphicOverlay.Graphic {
    private static final int TEXT_COLOR = Color.RED;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final Text.Line line; // Changed from Text.Element to Text.Line
    private RectF transformedRect; // Store the transformed rect for contains() check

    public TextGraphic(GraphicOverlay overlay, Text.Line line) {
        super(overlay);
        this.line = line;

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
        // postInvalidate(); // Not needed here, overlay handles invalidation
    }

    @Override
    public void draw(Canvas canvas) {
        if (line == null || line.getBoundingBox() == null) {
            return;
        }

        // Draws the bounding box around the Text.Line.
        transformedRect = translateRect(new RectF(line.getBoundingBox()));
        canvas.drawRect(transformedRect, rectPaint);

        // Renders the text at the bottom of the box.
        // Adjust text size and position as needed for debugging or display
        // canvas.drawText(line.getText(), transformedRect.left, transformedRect.bottom - STROKE_WIDTH, textPaint);
    }

    public Text.Line getLine() {
        return line;
    }

    public boolean contains(float x, float y) {
        return transformedRect != null && transformedRect.contains(x, y);
    }
}
