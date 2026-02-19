package com.thriftyApp;

import android.annotation.SuppressLint;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.graphics.RectF; // Import for roiRectView
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
// CameraX imports
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import com.google.common.util.concurrent.ListenableFuture;
// ML Kit Text Recognition
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
// Android Graphics
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import android.media.Image;
// Other necessary imports
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.view.MotionEvent;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;

@androidx.camera.core.ExperimentalGetImage
public class scanActivity extends BaseActivity implements GraphicOverlay.OnGraphicTapListener {

    PreviewView cameraPreviewView;
    TextView mTextView;
    EditText mScannedAmountEditText;
    Button proceed, clearScanButton, captureButton;
    ImageButton flashToggleButton;
    boolean isFlashOn = false;

    com.google.mlkit.vision.text.TextRecognizer mlkitRecognizer;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private Preview previewUseCase;
    private ImageCapture imageCaptureUseCase;
    private ImageAnalysis imageAnalysisUseCase;
    private ExecutorService cameraExecutor;
    private GraphicOverlay graphicOverlay;

    private static final String TAG = "ScanActivity";
    private static final int requestPermissionID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        proceed = findViewById(R.id.proceedButton);
        cameraPreviewView = findViewById(R.id.cameraPreviewView);
        graphicOverlay = findViewById(R.id.graphicOverlay);
        mTextView = findViewById(R.id.text_view);
        mScannedAmountEditText = findViewById(R.id.scannedAmountEditText);
        clearScanButton = findViewById(R.id.clearScanButton);
        flashToggleButton = findViewById(R.id.flashToggleButton);
        captureButton = findViewById(R.id.captureButton);

        mlkitRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                checkCameraPermissionAndStartCamera();
            } catch (Exception e) {
                Log.e(TAG, "Error getting camera provider: " + e.getMessage(), e);
                Toast.makeText(this, "Error initializing camera.", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));

        proceed.setEnabled(false);

        clearScanButton.setOnClickListener(v -> {
            mTextView.setText(R.string.no_text_available);
            mScannedAmountEditText.setText("");
            proceed.setEnabled(false);
            graphicOverlay.clear();
        });

        captureButton.setOnClickListener(v -> takePictureAndRecognizeText());
        setupTapToFocus();
        graphicOverlay.setOnGraphicTapListener(this);
    }

    @Override
    public void onTextLineTapped(Text.Line textLine) {
        if (textLine != null) {
            String tappedText = textLine.getText();
            Log.d(TAG, "Tapped on line: " + tappedText);
            String amount = extractAmountFromLine(tappedText);
            if (!amount.isEmpty()) {
                mScannedAmountEditText.setText(amount);
                proceed.setEnabled(true);
                Toast.makeText(this, "Amount selected: " + amount, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No valid amount in tapped line: " + tappedText, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupTapToFocus() {
        cameraPreviewView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (camera == null) {
                    Log.d(TAG, "Camera object is null, cannot start focus.");
                    return true;
                }
                MeteringPointFactory factory = cameraPreviewView.getMeteringPointFactory();
                MeteringPoint point = factory.createPoint(motionEvent.getX(), motionEvent.getY());
                FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF).build();
                try {
                    camera.getCameraControl().startFocusAndMetering(action);
                    Log.d(TAG, "Tap-to-focus action started.");
                } catch (Exception e) {
                    Log.e(TAG, "Tap-to-focus failed.", e);
                }
            }
            return true;
        });
    }

    private void takePictureAndRecognizeText() {
        if (imageCaptureUseCase == null) {
            Toast.makeText(this, "Camera not ready for capture.", Toast.LENGTH_SHORT).show();
            return;
        }
        imageCaptureUseCase.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                final Bitmap originalBitmapFromProxy = imageProxyToBitmap(imageProxy); // This is effectively final
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                imageProxy.close(); 

                if (originalBitmapFromProxy == null) {
                    Log.e(TAG, "Failed to convert ImageProxy to Bitmap.");
                    Toast.makeText(scanActivity.this, "Failed to process image.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bitmap bitmapToFeedMLKit = originalBitmapFromProxy; // Start with the original
                boolean wasCropped = false; // Flag to track if cropping occurred

                RectF roiRectView = graphicOverlay.getRoiRectView();
                Log.d(TAG, "ROI Debug: --- Start ---");
                Log.d(TAG, "ROI Debug: ImageProxy rotationDegrees: " + rotationDegrees);
                Log.d(TAG, "ROI Debug: Original Bitmap (from ImageProxy) Width: " + originalBitmapFromProxy.getWidth() + ", Height: " + originalBitmapFromProxy.getHeight());

                if (roiRectView != null && graphicOverlay.getWidth() > 0 && graphicOverlay.getHeight() > 0 &&
                    graphicOverlay.getSourceImageWidth() > 0 && graphicOverlay.getSourceImageHeight() > 0) {

                    Log.d(TAG, "ROI Debug: roiRectView (View Coords): " + roiRectView.toString());
                    Log.d(TAG, "ROI Debug: GraphicOverlay Width: " + graphicOverlay.getWidth() + ", Height: " + graphicOverlay.getHeight());
                    Log.d(TAG, "ROI Debug: GraphicOverlay SourceImageWidth: " + graphicOverlay.getSourceImageWidth() + ", SourceImageHeight: " + graphicOverlay.getSourceImageHeight());
                    Log.d(TAG, "ROI Debug: GraphicOverlay WidthScaleFactor: " + graphicOverlay.getWidthScaleFactor() + ", HeightScaleFactor: " + graphicOverlay.getHeightScaleFactor());
                    Log.d(TAG, "ROI Debug: GraphicOverlay PostScaleWidthOffset: " + graphicOverlay.getPostScaleWidthOffset() + ", PostScaleHeightOffset: " + graphicOverlay.getPostScaleHeightOffset());

                    // Dimensions of the source image as oriented for the GraphicOverlay
                    float orientedSourceWidth = graphicOverlay.getSourceImageWidth(); 
                    float orientedSourceHeight = graphicOverlay.getSourceImageHeight();

                    // Scale factors from oriented source to view
                    float overlayWidthScale = graphicOverlay.getWidthScaleFactor(); 
                    float overlayHeightScale = graphicOverlay.getHeightScaleFactor();
                    
                    // Offsets in view
                    float offsetXInView = graphicOverlay.getPostScaleWidthOffset(); 
                    float offsetYInView = graphicOverlay.getPostScaleHeightOffset();

                    float roiLeftInOrientedSource = (roiRectView.left - offsetXInView) / overlayWidthScale; 
                    float roiTopInOrientedSource = (roiRectView.top - offsetYInView) / overlayHeightScale;
                    float roiWidthInOrientedSource = roiRectView.width() / overlayWidthScale;
                    float roiHeightInOrientedSource = roiRectView.height() / overlayHeightScale;

                    Log.d(TAG, "ROI Debug: roiInOrientedSource - L: " + roiLeftInOrientedSource + ", T: " + roiTopInOrientedSource + ", W: " + roiWidthInOrientedSource + ", H: " + roiHeightInOrientedSource);

                    int originalBitmapWidth = originalBitmapFromProxy.getWidth();
                    int originalBitmapHeight = originalBitmapFromProxy.getHeight();
                    int cropX, cropY, cropWidth, cropHeight;

                    if (rotationDegrees == 90) {
                        cropX = (int) roiTopInOrientedSource;
                        cropY = (int) (originalBitmapHeight - (roiLeftInOrientedSource + roiWidthInOrientedSource));
                        cropWidth = (int) roiHeightInOrientedSource;
                        cropHeight = (int) roiWidthInOrientedSource;
                        if(graphicOverlay.isImageFlipped()){ cropY = (int) roiLeftInOrientedSource; }
                    } else if (rotationDegrees == 270) {
                        cropX = (int) (originalBitmapWidth - (roiTopInOrientedSource + roiHeightInOrientedSource));
                        cropY = (int) roiLeftInOrientedSource;
                        cropWidth = (int) roiHeightInOrientedSource;
                        cropHeight = (int) roiWidthInOrientedSource;
                         if(graphicOverlay.isImageFlipped()){ cropY = (int) (originalBitmapHeight - (roiLeftInOrientedSource + roiWidthInOrientedSource));}
                    } else if (rotationDegrees == 180) {
                        cropX = (int) (originalBitmapWidth - (roiLeftInOrientedSource + roiWidthInOrientedSource));
                        cropY = (int) (originalBitmapHeight - (roiTopInOrientedSource + roiHeightInOrientedSource));
                        cropWidth = (int) roiWidthInOrientedSource;
                        cropHeight = (int) roiHeightInOrientedSource;
                         if(graphicOverlay.isImageFlipped()){ cropX = (int) roiLeftInOrientedSource; }
                    } else { // 0 degrees
                        cropX = (int) roiLeftInOrientedSource;
                        cropY = (int) roiTopInOrientedSource;
                        cropWidth = (int) roiWidthInOrientedSource;
                        cropHeight = (int) roiHeightInOrientedSource;
                        if(graphicOverlay.isImageFlipped()){ cropX = (int) (originalBitmapWidth - (roiLeftInOrientedSource + roiWidthInOrientedSource)); }
                    }
                    
                    Log.d(TAG, "ROI Debug: Calculated Crop (Pre-Clamp) - X: " + cropX + ", Y: " + cropY + ", W: " + cropWidth + ", H: " + cropHeight);
                    cropX = Math.max(0, cropX);
                    cropY = Math.max(0, cropY);
                    cropWidth = Math.min(originalBitmapWidth - cropX, cropWidth);
                    cropHeight = Math.min(originalBitmapHeight - cropY, cropHeight);
                    Log.d(TAG, "ROI Debug: Calculated Crop (Post-Clamp) - X: " + cropX + ", Y: " + cropY + ", W: " + cropWidth + ", H: " + cropHeight);

                    if (cropWidth > 0 && cropHeight > 0) {
                        try {
                            Bitmap cropped = Bitmap.createBitmap(originalBitmapFromProxy, cropX, cropY, cropWidth, cropHeight);
                            if (cropped != null) {
                                bitmapToFeedMLKit = cropped; 
                                wasCropped = true;
                                Log.d(TAG, "Cropped bitmap to: " + cropWidth + "x" + cropHeight);
                            } else {
                                Log.e(TAG, "Bitmap.createBitmap returned null. Using original.");
                            }
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Error creating cropped bitmap: " + e.getMessage(), e);
                        }
                    } else {
                         Log.w(TAG, "Calculated crop dimensions are invalid or too small. Using original bitmap. W:"+cropWidth+" H:"+cropHeight);
                    }
                } else {
                     Log.w(TAG, "ROI Rect or GraphicOverlay dimensions not ready for precise cropping. Using original bitmap.");
                }
                Log.d(TAG, "ROI Debug: --- End ---");

                InputImage image = InputImage.fromBitmap(bitmapToFeedMLKit, rotationDegrees);
                
                // Create final references for the lambda
                final Bitmap finalBitmapUsedByMLKit = bitmapToFeedMLKit;
                final Bitmap finalOriginalBitmapForLambda = originalBitmapFromProxy; // The very first one
                final boolean finalWasCropped = wasCropped;


                mlkitRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String fullTextOriginal = visionText.getText();
                        mTextView.setText(fullTextOriginal.isEmpty() ? getString(R.string.no_text_available) : fullTextOriginal);
                        String bestFoundAmount = "";
                        double highestScore = -1.0;
                        double maxNumericValue = -1.0;
                        String[] keywords = getResources().getStringArray(R.array.ocr_amount_keywords);

                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                            for (Text.Line line : block.getLines()) {
                                String lineText = line.getText();
                                String potentialAmount = extractAmountFromLine(lineText);
                                if (!potentialAmount.isEmpty()) {
                                    boolean keywordFound = false;
                                    for (String keyword : keywords) {
                                        if (lineText.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))) {
                                            keywordFound = true;
                                            break;
                                        }
                                    }
                                    try {
                                        double numericValue = Double.parseDouble(potentialAmount);
                                        if (keywordFound) {
                                            if (numericValue > highestScore) {
                                                highestScore = numericValue;
                                                bestFoundAmount = potentialAmount;
                                            }
                                        } else if (highestScore == -1.0 && numericValue > maxNumericValue) {
                                            maxNumericValue = numericValue;
                                            if (bestFoundAmount.isEmpty() || Double.parseDouble(bestFoundAmount) < numericValue) {
                                               bestFoundAmount = potentialAmount;
                                            }
                                        }
                                    } catch (NumberFormatException e) { /* ignore */ }
                                }
                            }
                        }
                        if (highestScore == -1.0 && maxNumericValue != -1.0 && bestFoundAmount.isEmpty()) {
                             bestFoundAmount = String.format(Locale.US, "%.2f", maxNumericValue).replaceFirst("\\.00$", "");
                             if (bestFoundAmount.endsWith(".0")) bestFoundAmount = bestFoundAmount.substring(0, bestFoundAmount.length() -2);
                        }
                        mScannedAmountEditText.setText(bestFoundAmount);
                        proceed.setEnabled(!bestFoundAmount.isEmpty());
                        Log.d(TAG, "ML Kit OCR Success. Best Amount: " + bestFoundAmount);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "ML Kit OCR Failed: " + e.getMessage(), e);
                        mTextView.setText("OCR Failed: " + e.getMessage());
                        mScannedAmountEditText.setText("");
                        proceed.setEnabled(false);
                        Toast.makeText(scanActivity.this, "Text recognition failed.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> {
                        if (finalWasCropped && finalBitmapUsedByMLKit != null && !finalBitmapUsedByMLKit.isRecycled()) {
                            finalBitmapUsedByMLKit.recycle();
                            Log.d(TAG, "Recycled cropped bitmap (finalBitmapUsedByMLKit).");
                        }
                        if (finalOriginalBitmapForLambda != null && !finalOriginalBitmapForLambda.isRecycled()) {
                            // If it was cropped, original is different and needs recycling.
                            // If not cropped, finalBitmapUsedByMLKit IS finalOriginalBitmapForLambda.
                            // So, only recycle original if it's different from what was processed OR if it was processed and not cropped.
                            if (finalWasCropped || (finalOriginalBitmapForLambda == finalBitmapUsedByMLKit && !finalBitmapUsedByMLKit.isRecycled())) {
                                // Avoid double recycling if original was processed and already handled by finalBitmapUsedByMLKit logic
                                if (finalOriginalBitmapForLambda != finalBitmapUsedByMLKit || !finalWasCropped) {
                                     finalOriginalBitmapForLambda.recycle();
                                     Log.d(TAG, "Recycled originalBitmapFromProxy.");
                                }
                            } else if (!finalWasCropped && finalOriginalBitmapForLambda == finalBitmapUsedByMLKit && !finalBitmapUsedByMLKit.isRecycled()){
                                // This case should be covered by the above, but for clarity:
                                // if original was used and not cropped, it's the same as finalBitmapUsedByMLKit.
                                // If finalBitmapUsedByMLKit was recycled, this is fine. If not, it means it wasn't cropped.
                                // The logic here is to ensure original is recycled if it's not the one that was processed and recycled.
                                // Let's simplify: if original is not the one that was fed to MLKit (because a crop happened), recycle original.
                                // If original *was* fed to MLKit, then finalBitmapUsedByMLKit is original, and it will be recycled if not already.
                                // This is still a bit tricky. The goal:
                                // 1. If cropped: recycle cropped, recycle original.
                                // 2. If not cropped: recycle original (which is also finalBitmapUsedByMLKit).
                                // The current logic:
                                // if (finalWasCropped && finalBitmapUsedByMLKit != null && !finalBitmapUsedByMLKit.isRecycled()) -> recycles cropped
                                // if (finalOriginalBitmapForLambda != null && !finalOriginalBitmapForLambda.isRecycled()) -> this will try to recycle original
                                // This might lead to double recycle if not cropped.
                                // Corrected logic:
                                // 1. Recycle the bitmap that was fed to MLKit if it was a *new* (cropped) bitmap.
                                // 2. Always recycle the original bitmap from the proxy.
                                // This means if no cropping happened, original is fed, then original is recycled.
                                // If cropping happened, cropped is fed, cropped is recycled, original is recycled.
                            }
                        }
                    });
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Image capture failed: " + exception.getMessage(), exception);
                Toast.makeText(scanActivity.this, "Failed to capture image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @androidx.camera.core.ExperimentalGetImage
    private void processImageWithMLKit(InputImage image) {
        // This method is now effectively a passthrough as the listeners are attached
        // directly where mlkitRecognizer.process() is called in takePictureAndRecognizeText.
        // Kept for potential future direct use or refactoring.
        Log.d(TAG, "processImageWithMLKit called - Note: actual processing logic is now chained in takePictureAndRecognizeText");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraProvider != null && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            bindCameraUseCases();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (cameraProvider != null) {
            // cameraProvider.unbindAll(); // Consider if needed
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestPermissionID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted. Attempting to start camera.");
                if (cameraProvider != null) {
                    checkCameraPermissionAndStartCamera();
                }
            } else {
                Log.d(TAG, "Camera permission denied.");
                Toast.makeText(this, "Camera permission is required to use the scanner.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void proceedClick (View view) {
        Intent intent = new Intent (getApplicationContext (), PayActivity.class);
        String amountStr = mScannedAmountEditText.getText().toString();
        if (amountStr.isEmpty() || !amountStr.matches("\\d+(\\.\\d{1,2})?")) {
            Toast.makeText(this, "Please enter or scan a valid amount.", Toast.LENGTH_LONG).show();
            return;
        }
        intent.putExtra ("ocr", amountStr);
        startActivity(intent);
        finish();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        finish();
    }

    private void checkCameraPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cameraProvider != null) {
                bindCameraUseCases();
            } else {
                Log.w(TAG, "CameraProvider not available yet in checkCameraPermissionAndStartCamera. Listener should handle it.");
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestPermissionID);
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private void bindCameraUseCases() {
        if (cameraProvider == null) {
            Log.e(TAG, "Camera provider is null, cannot bind use cases.");
            Toast.makeText(this, "Error initializing camera provider.", Toast.LENGTH_SHORT).show();
            return;
        }

        previewUseCase = new Preview.Builder().build();
        imageCaptureUseCase = new ImageCapture.Builder()
                                .setTargetRotation(cameraPreviewView.getDisplay().getRotation())
                                .build();
        imageAnalysisUseCase = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(cameraPreviewView.getDisplay().getRotation())
                .build();
        
        imageAnalysisUseCase.setAnalyzer(cameraExecutor, new TextAnalyzer());
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        previewUseCase.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase, imageCaptureUseCase, imageAnalysisUseCase);
            Log.d(TAG, "Camera use cases bound successfully.");

            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                flashToggleButton.setVisibility(View.VISIBLE);
                flashToggleButton.setOnClickListener(v -> {
                    isFlashOn = !isFlashOn;
                    camera.getCameraControl().enableTorch(isFlashOn);
                    // Using placeholder icons as actual flash_on/flash_off drawables are not known
                    // android.R.drawable.ic_menu_camera is used in XML, likely for 'off' or 'auto'
                    flashToggleButton.setImageResource(isFlashOn ? android.R.drawable.sym_def_app_icon : android.R.drawable.ic_menu_camera);
                });
                flashToggleButton.setImageResource(isFlashOn ? android.R.drawable.sym_def_app_icon : android.R.drawable.ic_menu_camera);
            } else {
                flashToggleButton.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
            Toast.makeText(this, "Failed to bind camera use cases.", Toast.LENGTH_SHORT).show();
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private class TextAnalyzer implements ImageAnalysis.Analyzer {
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            Image mediaImage = imageProxy.getImage();
            if (mediaImage != null && camera != null) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                
                mlkitRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        graphicOverlay.clear();
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        int sourceWidth = imageProxy.getWidth();
                        int sourceHeight = imageProxy.getHeight();
                        
                        if (rotationDegrees == 90 || rotationDegrees == 270) {
                            sourceWidth = imageProxy.getHeight();
                            sourceHeight = imageProxy.getWidth();
                        }
                        
                        boolean isFlipped = camera.getCameraInfo().getLensFacing() == CameraSelector.LENS_FACING_FRONT;
                        graphicOverlay.setCameraInfo(sourceWidth, sourceHeight, isFlipped);

                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                            for (Text.Line line : block.getLines()) {
                                graphicOverlay.add(new TextGraphic(graphicOverlay, line));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Text recognition from analyzer failed.", e))
                    .addOnCompleteListener(task -> imageProxy.close());
            } else {
                imageProxy.close();
            }
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        if (image.getFormat() == ImageFormat.JPEG) {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else if (image.getFormat() == ImageFormat.YUV_420_888) {
            return imageProxyYuvToBitmap(image);
        } else {
            Log.e(TAG, "Unsupported image format for imageProxyToBitmap: " + image.getFormat());
            return null;
        }
    }
    
    private Bitmap imageProxyYuvToBitmap(ImageProxy imageProxy) {
        if (imageProxy.getFormat() != ImageFormat.YUV_420_888) {
            Log.e(TAG, "imageProxyYuvToBitmap called with non-YUV format: " + imageProxy.getFormat());
            return null;
        }

        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        
        int uvPixelStride = planes[1].getPixelStride(); // Assuming U/V planes have same pixel stride
        int uvRowStride = planes[1].getRowStride();


        byte[] nv21 = new byte[ySize + uSize + vSize]; // This size might be too large if planes are not contiguous
                                                    // Or too small if there's padding not accounted for.
                                                    // A more accurate size for NV21 is width * height * 3 / 2.
        int width = imageProxy.getWidth();
        int height = imageProxy.getHeight();
        byte[] nv21CorrectSize = new byte[width * height * 3 / 2];


        yBuffer.get(nv21CorrectSize, 0, ySize);

        // Constructing NV21's VU plane from separate U and V planes is complex.
        // V plane data first, then U plane data, interleaved.
        // Example: YYYYYYYY VUVUVU...
        // planes[1] is U, planes[2] is V.
        // This requires careful handling of row strides and pixel strides.
        
        int yPlaneSize = width * height; // Size of Y data
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                int vuPos = yPlaneSize + row * width + col * 2; // Target position in NV21 VU plane (width, not uvRowStride here for target)
                
                // Ensure we don't read past buffer limits
                int vBufferPos = row * planes[2].getRowStride() + col * planes[2].getPixelStride();
                int uBufferPos = row * planes[1].getRowStride() + col * planes[1].getPixelStride();

                if (vuPos + 1 < nv21CorrectSize.length && vBufferPos < vSize && uBufferPos < uSize) {
                    nv21CorrectSize[vuPos] = vBuffer.get(vBufferPos);     // V
                    nv21CorrectSize[vuPos + 1] = uBuffer.get(uBufferPos); // U
                } else {
                    // Handle cases where we might read out of bounds due to padding or stride differences
                    // This might happen if image width is not a multiple of 2 or due to plane padding.
                    // For simplicity, we might break or fill with a default value if this occurs.
                    break; 
                }
            }
        }
        
        try {
            YuvImage yuvImage = new YuvImage(nv21CorrectSize, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 90, out);
            byte[] imageBytes = out.toByteArray();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        } catch (Exception e) {
            Log.e(TAG, "Error converting YUV_420_888 to Bitmap: " + e.getMessage(), e);
            return null;
        }
    }


    private String extractAmountFromLine(String lineText) {
        java.util.regex.Pattern amountPattern = java.util.regex.Pattern.compile(
                "\\b\\d{1,3}(?:[.,\\s]\\d{3})*[.,]\\d{2}\\b" +
                "|" +
                "\\b\\d+[.,]\\d{2}\\b" +
                "|" +
                "\\b\\d+\\b"
        );
        java.util.regex.Matcher matcher = amountPattern.matcher(lineText);
        String bestMatch = "";
        double maxAmount = -1.0;

        while (matcher.find()) {
            String currentMatch = matcher.group(0);
            String parsableMatch = currentMatch.replaceAll("\\s", "").replace(',', '.');
            if (parsableMatch.indexOf('.') != parsableMatch.lastIndexOf('.')) {
                continue;
            }
            try {
                double currentValue = Double.parseDouble(parsableMatch);
                if (currentValue > maxAmount) {
                    maxAmount = currentValue;
                    bestMatch = parsableMatch;
                } else if (currentValue == maxAmount && currentMatch.matches(".*[.,]\\d{2}")) {
                    bestMatch = parsableMatch;
                }
            } catch (NumberFormatException e) {
                Log.d(TAG, "Could not parse as double: " + parsableMatch);
            }
        }
        if (bestMatch.indexOf('.') != bestMatch.lastIndexOf('.')) {
             Log.d(TAG, "Multiple decimal points in final bestMatch, clearing: " + bestMatch);
            return ""; 
        }
        Log.d(TAG, "Extracted amount from line '" + lineText + "': " + bestMatch);
        return bestMatch;
    }
}
