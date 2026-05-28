package com.thriftyApp;

/**
 * Pure geometry for mapping a region-of-interest (expressed in the oriented
 * source-image coordinate space) onto the raw captured bitmap, accounting for
 * the capture rotation and a possible horizontal flip (front camera).
 *
 * <p>Extracting this out of {@code scanActivity} removes the heavily branched
 * rotation logic from the capture callback and makes it unit-testable without a
 * camera or bitmap.
 */
public final class RoiCropCalculator {

    /** An axis-aligned crop rectangle in raw-bitmap pixel coordinates. */
    public static final class CropRect {
        public final int x;
        public final int y;
        public final int width;
        public final int height;

        CropRect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        /** True when the rectangle has a positive area and can be cropped. */
        public boolean isValid() {
            return width > 0 && height > 0;
        }
    }

    private RoiCropCalculator() {
        // Utility class: no instances.
    }

    /**
     * Computes the (un-clamped) crop rectangle for the given rotation/flip, then
     * clamps it to the bitmap bounds.
     *
     * @param rotationDegrees capture rotation: 0, 90, 180 or 270
     * @param flipped         true for a horizontally mirrored (front) image
     * @param roiLeft         ROI left in oriented-source space
     * @param roiTop          ROI top in oriented-source space
     * @param roiWidth        ROI width in oriented-source space
     * @param roiHeight       ROI height in oriented-source space
     * @param bitmapWidth     raw bitmap width
     * @param bitmapHeight    raw bitmap height
     * @return the clamped {@link CropRect}
     */
    public static CropRect compute(int rotationDegrees, boolean flipped,
            float roiLeft, float roiTop, float roiWidth, float roiHeight,
            int bitmapWidth, int bitmapHeight) {
        int cropX;
        int cropY;
        int cropWidth;
        int cropHeight;

        if (rotationDegrees == 90) {
            cropX = (int) roiTop;
            cropY = flipped ? (int) roiLeft
                            : (int) (bitmapHeight - (roiLeft + roiWidth));
            cropWidth = (int) roiHeight;
            cropHeight = (int) roiWidth;
        } else if (rotationDegrees == 270) {
            cropX = (int) (bitmapWidth - (roiTop + roiHeight));
            cropY = flipped ? (int) (bitmapHeight - (roiLeft + roiWidth))
                            : (int) roiLeft;
            cropWidth = (int) roiHeight;
            cropHeight = (int) roiWidth;
        } else if (rotationDegrees == 180) {
            cropX = flipped ? (int) roiLeft
                            : (int) (bitmapWidth - (roiLeft + roiWidth));
            cropY = (int) (bitmapHeight - (roiTop + roiHeight));
            cropWidth = (int) roiWidth;
            cropHeight = (int) roiHeight;
        } else { // 0 degrees
            cropX = flipped ? (int) (bitmapWidth - (roiLeft + roiWidth))
                            : (int) roiLeft;
            cropY = (int) roiTop;
            cropWidth = (int) roiWidth;
            cropHeight = (int) roiHeight;
        }

        cropX = Math.max(0, cropX);
        cropY = Math.max(0, cropY);
        cropWidth = Math.min(bitmapWidth - cropX, cropWidth);
        cropHeight = Math.min(bitmapHeight - cropY, cropHeight);
        return new CropRect(cropX, cropY, cropWidth, cropHeight);
    }
}
