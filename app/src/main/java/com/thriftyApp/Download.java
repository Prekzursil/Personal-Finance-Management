package com.thriftyApp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import com.thriftyApp.BaseActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.os.Bundle;
// import android.os.Handler; // Not used
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
// import android.widget.ImageView; // Replaced by buttons
import android.widget.Toast;
import android.app.DatePickerDialog;

// import com.github.barteksc.pdfviewer.PDFView; // Not used in this file
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;

import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Download extends BaseActivity {
    private static final String TAG = "PdfCreatorActivity";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    private File pdfFile;
    DatabaseHelper databaseHelper;
    ArrayList<Transactions> list;
    // Transactions transactions; // Not used as a field, instantiated locally
    Context context;
    String pdfname = "TransactionReport.pdf"; // Default name, will be updated
    private EditText editTextStartDate, editTextEndDate;
    private Button buttonDownloadCustomRange, buttonDownloadAllTime;
    private String selectedStartDateString, selectedEndDateString; // Store dates as yyyy-MM-dd
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    // private SimpleDateFormat dbDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); // Used locally


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download2);

        context = this;
        // transactions = new Transactions(); // Not needed as field
        databaseHelper = new DatabaseHelper(this);

        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        buttonDownloadCustomRange = findViewById(R.id.buttonDownloadCustomRange);
        buttonDownloadAllTime = findViewById(R.id.buttonDownloadAllTime);
        // The ImageView R.id.downloadpdf is still in the layout.
        // We'll make it trigger "All Time" download for now.
        findViewById(R.id.downloadpdf).setOnClickListener(v -> generatePdfWithDateRange(null, null));


        editTextStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        editTextEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        buttonDownloadCustomRange.setOnClickListener(v -> {
            if (selectedStartDateString == null || selectedEndDateString == null) {
                Toast.makeText(context, getString(R.string.select_start_end_dates_message), Toast.LENGTH_SHORT).show(); // Using string resource
                return;
            }
            // Convert yyyy-MM-dd to yyyy-MM-dd HH:mm:ss for DB query
            String queryStartDate = selectedStartDateString + " 00:00:00";
            String queryEndDate = selectedEndDateString + " 23:59:59";
            generatePdfWithDateRange(queryStartDate, queryEndDate);
        });

        buttonDownloadAllTime.setOnClickListener(v -> generatePdfWithDateRange(null, null));
    }

    private void showDatePickerDialog(final boolean isStartDateDialog) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
                    String formattedDate = dateFormatter.format(newDate.getTime());
                    if (isStartDateDialog) {
                        editTextStartDate.setText(formattedDate);
                        selectedStartDateString = formattedDate;
                    } else {
                        editTextEndDate.setText(formattedDate);
                        selectedEndDateString = formattedDate;
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void generatePdfWithDateRange(String startDate, String endDate) {
        try {
            // Update pdfname to include date range if applicable
            if (startDate != null && endDate != null) {
                // Extract date part for filename
                String startDateFile = startDate.substring(0, 10);
                String endDateFile = endDate.substring(0, 10);
                pdfname = "TransactionsReport_" + startDateFile + "_to_" + endDateFile + "_" + Utils.pdfNumber + ".pdf";
            } else {
                pdfname = "TransactionsReport_AllTime_" + Utils.pdfNumber + ".pdf";
            }
            Utils.pdfNumber++; // Increment for unique filenames

            createPdfWrapper(startDate, endDate);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: File not found. " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (DocumentException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error creating document. " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "IOException in generatePdfWithDateRange", e);
            Toast.makeText(context, "Error: IO Exception. " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void createPdfWrapper(String startDate, String endDate) throws IOException, DocumentException {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) { // Corrected permission
                    showMessageOKCancel(getString(R.string.storage_permission_needed_message), // Using string resource
                            (dialog, which) -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            });
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                return; 
            }
        }
        
        createPdf(startDate, endDate); // Call the method to create and save the PDF

        // Send a notification once the PDF is saved
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "pdf_channel") // Added channel ID
                .setSmallIcon(R.drawable.download)
                .setContentTitle(pdfname)
                .setContentText(getString(R.string.download_completed_notification_text)) // Using string resource
                .setPriority(NotificationCompat.PRIORITY_DEFAULT); // Added priority

        Intent notificationIntent = new Intent(this, TransactionsActivity.class); // Or where you want to navigate
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Added FLAG_IMMUTABLE
        );
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel("pdf_channel", getString(R.string.notification_channel_pdf_downloads), NotificationManager.IMPORTANCE_DEFAULT); // Using string resource
            manager.createNotificationChannel(channel);
        }
        if (manager != null) {
            manager.notify(Utils.pdfNumber, builder.build()); // Use unique ID for notification
        }

        Toast.makeText(getApplicationContext(), String.format(getString(R.string.report_pdf_downloaded_successfully_toast), pdfname), Toast.LENGTH_LONG).show(); // Using string resource
    }

    private void createPdf(String startDate, String endDate) throws IOException, DocumentException {
        String reportTitle;
        SimpleDateFormat inputDbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputDisplayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (startDate != null && endDate != null) {
            try {
                Date parsedStartDate = inputDbFormat.parse(startDate);
                Date parsedEndDate = inputDbFormat.parse(endDate);
                String startDateFormatted = parsedStartDate != null ? outputDisplayFormat.format(parsedStartDate) : startDate.substring(0,10);
                String endDateFormatted = parsedEndDate != null ? outputDisplayFormat.format(parsedEndDate) : endDate.substring(0,10);
                reportTitle = String.format(context.getString(R.string.pdf_title_transactions_for_user_custom_range), Utils.userName, startDateFormatted, endDateFormatted);
            } catch (Exception e) {
                 reportTitle = String.format(context.getString(R.string.pdf_title_transactions_for_user), Utils.userName); // Fallback to generic title
            }
        } else {
            reportTitle = String.format(context.getString(R.string.pdf_title_transactions_for_user_all_time), Utils.userName);
        }

        String generatedByString = String.format(context.getString(R.string.pdf_generated_by), context.getString(R.string.app_name));
        String generatedOnString = context.getString(R.string.pdf_generated_on) + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, pdfname);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

            ContentResolver resolver = getContentResolver();
            Uri pdfUri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);

            if (pdfUri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(pdfUri)) {
                    if (outputStream == null) {
                        throw new IOException("Failed to get output stream for MediaStore URI.");
                    }
                    Document document = new Document(PageSize.A4);
                    PdfWriter.getInstance(document, outputStream);
                    document.open();
                    Font titleFont = new Font(Font.FontFamily.HELVETICA, 24.0f, Font.UNDERLINE | Font.BOLD, BaseColor.BLUE);
                    document.add(new Paragraph(reportTitle + "\n\n", titleFont));
                    document.add(new Paragraph(generatedByString + "\n" + generatedOnString + "\n\n"));

                    PdfPTable table = new PdfPTable(new float[]{3, 3, 3, 3});
                    table.setWidthPercentage(100);
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.getDefaultCell().setFixedHeight(40); 
                    table.addCell(context.getString(R.string.pdf_header_tag));
                    table.addCell(context.getString(R.string.pdf_header_amount));
                    table.addCell(context.getString(R.string.pdf_header_date));
                    table.addCell(context.getString(R.string.pdf_header_type));
                    table.setHeaderRows(1);

                    list = databaseHelper.getTransactionsPDF(startDate, endDate);
                    if (list.isEmpty()) {
                        document.add(new Paragraph(context.getString(R.string.pdf_no_transactions_for_period)));
                    } else {
                        for (Transactions t : list) {
                            table.addCell(t.getTag());
                            table.addCell(String.valueOf(t.getAmount()));
                            table.addCell(t.getCreated_at());
                            table.addCell(t.getExin() == 0 ? context.getString(R.string.pdf_type_expense) : context.getString(R.string.pdf_type_income));
                        }
                        document.add(table);
                    }
                    document.close();
                }
            } else {
                 throw new IOException("Failed to create new MediaStore entry.");
            }
        } else {
            // For Android 9 and below
            File docsFolder = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOCUMENTS);
            if (!docsFolder.exists()) {
                if (!docsFolder.mkdirs()) {
                     Log.e(TAG, "Failed to create directory for PDF on older Android.");
                     throw new IOException("Failed to create directory for PDF on older Android.");
                }
            }
            pdfFile = new File(docsFolder, pdfname);

            try (OutputStream output = new FileOutputStream(pdfFile)) {
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, output);
                document.open();
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 24.0f, Font.UNDERLINE | Font.BOLD, BaseColor.BLUE);
                document.add(new Paragraph(reportTitle + "\n\n", titleFont));
                document.add(new Paragraph(generatedByString + "\n" + generatedOnString + "\n\n"));

                PdfPTable table = new PdfPTable(new float[]{3, 3, 3, 3});
                table.setWidthPercentage(100);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.getDefaultCell().setFixedHeight(40);
                table.addCell(context.getString(R.string.pdf_header_tag));
                table.addCell(context.getString(R.string.pdf_header_amount));
                table.addCell(context.getString(R.string.pdf_header_date));
                table.addCell(context.getString(R.string.pdf_header_type));
                table.setHeaderRows(1);

                list = databaseHelper.getTransactionsPDF(startDate, endDate);
                if (list.isEmpty()) {
                    document.add(new Paragraph(context.getString(R.string.pdf_no_transactions_for_period)));
                } else {
                    for (Transactions t : list) {
                        table.addCell(t.getTag());
                        table.addCell(String.valueOf(t.getAmount()));
                        table.addCell(t.getCreated_at());
                        table.addCell(t.getExin() == 0 ? context.getString(R.string.pdf_type_expense) : context.getString(R.string.pdf_type_income));
                    }
                    document.add(table);
                }
                document.close();
            }
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(pdfFile);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, getString(R.string.storage_permission_granted_toast), Toast.LENGTH_LONG).show(); // Using string resource
            } else {
                Toast.makeText(context, getString(R.string.storage_permission_denied_toast), Toast.LENGTH_LONG).show(); // Using string resource
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message) // Message is already a variable, potentially from string resource
                .setPositiveButton(android.R.string.ok, okListener) // Using system OK
                .setNegativeButton(android.R.string.cancel, null) // Using system Cancel
                .create()
                .show();
    }

    private void previewPdf() {
        if (pdfFile == null || !pdfFile.exists()) {
             Toast.makeText(context, getString(R.string.pdf_not_available_preview_toast), Toast.LENGTH_SHORT).show(); // Using string resource
             return;
        }
        PackageManager packageManager = context.getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List<ResolveInfo> listIntentActivities = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (!listIntentActivities.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uri = androidx.core.content.FileProvider.getUriForFile(context, getApplicationContext().getPackageName() + ".provider", pdfFile);
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                 Uri uri = Uri.fromFile(pdfFile);
                 intent.setDataAndType(uri, "application/pdf");
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); 
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, getString(R.string.no_app_to_open_pdf_toast), Toast.LENGTH_SHORT).show(); // Using string resource
            }
        } else {
            Toast.makeText(context, getString(R.string.download_pdf_viewer_toast), Toast.LENGTH_SHORT).show(); // Using string resource
        }
    }
}
