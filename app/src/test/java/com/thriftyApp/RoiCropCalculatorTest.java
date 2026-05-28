package com.thriftyApp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests for the pure {@link RoiCropCalculator} geometry. */
public class RoiCropCalculatorTest {

    @Test
    public void zeroRotationMapsRoiDirectly() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                0, false, 10f, 20f, 30f, 40f, 200, 200);
        assertEquals(10, r.x);
        assertEquals(20, r.y);
        assertEquals(30, r.width);
        assertEquals(40, r.height);
        assertTrue(r.isValid());
    }

    @Test
    public void zeroRotationFlippedMirrorsX() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                0, true, 10f, 20f, 30f, 40f, 200, 200);
        assertEquals(200 - (10 + 30), r.x);
        assertEquals(20, r.y);
    }

    @Test
    public void rotation90SwapsDimensions() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                90, false, 10f, 20f, 30f, 40f, 200, 200);
        assertEquals(20, r.x);
        assertEquals(200 - (10 + 30), r.y);
        assertEquals(40, r.width);
        assertEquals(30, r.height);
    }

    @Test
    public void rotation90FlippedAdjustsY() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                90, true, 10f, 20f, 30f, 40f, 200, 200);
        assertEquals(10, r.y);
    }

    @Test
    public void rotation270MapsCorners() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                270, false, 10f, 20f, 30f, 40f, 200, 200);
        assertEquals(200 - (20 + 40), r.x);
        assertEquals(10, r.y);
    }

    @Test
    public void rotation270FlippedAdjustsY() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                270, true, 10f, 20f, 30f, 40f, 200, 200);
        assertEquals(200 - (10 + 30), r.y);
    }

    @Test
    public void rotation180MapsCorners() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                180, false, 10f, 20f, 30f, 40f, 200, 200);
        assertEquals(200 - (10 + 30), r.x);
        assertEquals(200 - (20 + 40), r.y);
    }

    @Test
    public void rotation180FlippedAdjustsX() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                180, true, 10f, 20f, 30f, 40f, 200, 200);
        assertEquals(10, r.x);
    }

    @Test
    public void negativeOriginsAreClampedToZero() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                0, false, -50f, -60f, 30f, 40f, 200, 200);
        assertEquals(0, r.x);
        assertEquals(0, r.y);
    }

    @Test
    public void oversizedCropIsClampedToBitmap() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                0, false, 150f, 150f, 500f, 500f, 200, 200);
        assertEquals(50, r.width);
        assertEquals(50, r.height);
    }

    @Test
    public void degenerateCropIsInvalid() {
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                0, false, 200f, 200f, 0f, 0f, 200, 200);
        assertFalse(r.isValid());
    }

    @Test
    public void positiveWidthButZeroHeightIsInvalid() {
        // Width clamps positive while height clamps to zero: exercises the
        // second operand of the isValid() short-circuit.
        RoiCropCalculator.CropRect r = RoiCropCalculator.compute(
                0, false, 10f, 200f, 30f, 0f, 200, 200);
        assertTrue(r.width > 0);
        assertEquals(0, r.height);
        assertFalse(r.isValid());
    }
}
