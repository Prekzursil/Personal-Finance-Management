package com.thriftyApp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color; // Added
import android.graphics.Paint; // Added
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.google.mlkit.vision.text.Text; // For Text.Line in listener
import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends View {
    private final Object lock = new Object();
    private final List<Graphic> graphics = new ArrayList<>();
    private OnGraphicTapListener onGraphicTapListener;

    private float widthScaleFactor = 1.0f;
    private float heightScaleFactor = 1.0f;
    private int sourceImageWidth;
    private int sourceImageHeight;
    private boolean isImageFlipped;
    private float postScaleWidthOffset = 0f;
    private float postScaleHeightOffset = 0f;
    private Paint roiPaint;
    private RectF roiRectView; // ROI rectangle in View coordinates


    public static abstract class Graphic {
        private GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public float scaleX(float horizontal) {
            return horizontal * overlay.widthScaleFactor;
        }

        public float scaleY(float vertical) {
            return vertical * overlay.heightScaleFactor;
        }

        public float translateX(float x) {
            float scaledX = scaleX(x);
            if (overlay.isImageFlipped) {
                return overlay.getWidth() - scaledX - overlay.postScaleWidthOffset;
            } else {
                return scaledX + overlay.postScaleWidthOffset;
            }
        }

        public float translateY(float y) {
            return scaleY(y) + overlay.postScaleHeightOffset;
        }

        public RectF translateRect(RectF rect) {
            return new RectF(
                translateX(rect.left),
                translateY(rect.top),
                translateX(rect.right),
                translateY(rect.bottom)
            );
        }

        public void postInvalidate() {
            overlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        initRoiPaint();
    }

    private void initRoiPaint() {
        roiPaint = new Paint();
        roiPaint.setColor(Color.GREEN); // Example color
        roiPaint.setStyle(Paint.Style.STROKE);
        roiPaint.setStrokeWidth(6f); // Example stroke width
        roiPaint.setAlpha(150); // Semi-transparent
    }

    public interface OnGraphicTapListener {
        void onTextLineTapped(Text.Line textLine);
    }

    public void setOnGraphicTapListener(OnGraphicTapListener listener) {
        this.onGraphicTapListener = listener;
    }

    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }

    public void add(Graphic graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
        postInvalidate();
    }

    public void remove(Graphic graphic) {
        synchronized (lock) {
            graphics.remove(graphic);
        }
        postInvalidate();
    }

    public void setCameraInfo(int sourceImageWidth, int sourceImageHeight, boolean isImageFlipped) {
        synchronized (lock) {
            this.sourceImageWidth = sourceImageWidth;
            this.sourceImageHeight = sourceImageHeight;
            this.isImageFlipped = isImageFlipped;
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            if (sourceImageWidth != 0 && sourceImageHeight != 0 && getWidth() != 0 && getHeight() != 0) {
                float viewAspectRatio = (float) getWidth() / (float) getHeight();
                float sourceAspectRatio = (float) sourceImageWidth / (float) sourceImageHeight;

                postScaleWidthOffset = 0f;
                postScaleHeightOffset = 0f;

                if (viewAspectRatio > sourceAspectRatio) {
                    // View is wider or less tall than source (letterboxed if FIT_CENTER)
                    // Scale to fit height
                    widthScaleFactor = (float) getHeight() / (float) sourceImageHeight;
                    heightScaleFactor = widthScaleFactor;
                    postScaleWidthOffset = ((float) getWidth() - (sourceImageWidth * widthScaleFactor)) / 2.0f;
                } else {
                    // View is taller or less wide than source (pillarboxed if FIT_CENTER)
                    // Scale to fit width
                    heightScaleFactor = (float) getWidth() / (float) sourceImageWidth; // Corrected: was heightScaleFactor = getWidth / sourceImageWidth
                    widthScaleFactor = heightScaleFactor; // Corrected: was widthScaleFactor = heightScaleFactor
                    postScaleHeightOffset = ((float) getHeight() - (sourceImageHeight * heightScaleFactor)) / 2.0f;
                }
            }

            // Draw ROI rectangle (example: 80% of view width, 30% of view height, centered)
            if (getWidth() > 0 && getHeight() > 0) {
                float roiWidth = getWidth() * 0.8f;
                float roiHeight = getHeight() * 0.3f;
                float roiLeft = (getWidth() - roiWidth) / 2f;
                float roiTop = (getHeight() - roiHeight) / 2f;
                roiRectView = new RectF(roiLeft, roiTop, roiLeft + roiWidth, roiTop + roiHeight);
                canvas.drawRect(roiRectView, roiPaint);
            }

            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        }
    }
    
    // Getter for the ROI rectangle in view coordinates
    public RectF getRoiRectView() {
        return roiRectView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        if (onGraphicTapListener != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            Graphic tappedGraphic = null;
            synchronized (lock) {
                // Iterate in reverse order so top-most graphic is processed first
                for (int i = graphics.size() - 1; i >= 0; i--) {
                    Graphic graphic = graphics.get(i);
                    if (graphic instanceof TextGraphic) {
                        TextGraphic textGraphic = (TextGraphic) graphic;
                        if (textGraphic.contains(x, y)) {
                            tappedGraphic = textGraphic;
                            break;
                        }
                    }
                }
            }

            if (tappedGraphic != null) {
                onGraphicTapListener.onTextLineTapped(((TextGraphic) tappedGraphic).getLine());
                handled = true;
            }
        }
        return handled || super.onTouchEvent(event);
    }

    // Getters for transformation parameters needed by scanActivity
    public int getSourceImageWidth() {
        return sourceImageWidth;
    }

    public int getSourceImageHeight() {
        return sourceImageHeight;
    }

    public float getWidthScaleFactor() {
        return widthScaleFactor;
    }

    public float getHeightScaleFactor() {
        return heightScaleFactor;
    }

    public float getPostScaleWidthOffset() {
        return postScaleWidthOffset;
    }

    public float getPostScaleHeightOffset() {
        return postScaleHeightOffset;
    }

    public boolean isImageFlipped() {
        return isImageFlipped;
    }
}
