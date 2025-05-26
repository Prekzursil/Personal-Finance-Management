package com.thriftyApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "thrifty.db";
    private static final String TABLE_SIGNUP = "contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_MOBILE = "mobile";
    private static final String COLUMN_BUDGET = "budget";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD= "password";

    private static final String COLUMN_GOOGLE_ID = "google_id";
    private static final String TABLE_TRANSACT = "transactions";
    private static final String COL_TID = "id";
    private static final String COL_U_ID = "uid";
    private static final String COL_TAG = "tag";
    private static final String COL_EXIN = "exin";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_DATETIME = "created_at";

    private static final String TABLE_ALERTS = "alerts_table";
    private static final String COL_ALERT_ID = "alert_id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_ALERT_MESSAGE= "alert_message";
    private static final String COL_ALERT_TIME= "alert_time";

    private SQLiteDatabase db;
    private static final String CREATE_TABLE_SIGNUP = "CREATE TABLE " + TABLE_SIGNUP  + "( " + COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL , " + COLUMN_NAME + " TEXT NOT NULL , " + COLUMN_EMAIL +" TEXT NOT NULL , " + COLUMN_MOBILE +" INTEGER NOT NULL, " + COLUMN_BUDGET + " INTEGER NOT NULL, " + COLUMN_PASSWORD + " TEXT NOT NULL , " +
            COLUMN_GOOGLE_ID + " TEXT NOT NULL);";

    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE " + TABLE_TRANSACT  + "( " + COL_TID + " INTEGER PRIMARY KEY NOT NULL , " + COL_U_ID + " INTEGER NOT NULL " + " , " + COL_TAG + " TEXT NOT NULL , " + COL_EXIN +" INTEGER NOT NULL, " + COL_DATETIME +" DATETIME  NOT NULL, " + COL_AMOUNT + " INTEGER NOT NULL );";

    private static final String CREATE_TABLE_ALERTS = "CREATE TABLE " + TABLE_ALERTS  + "( " + COL_ALERT_ID + " INTEGER PRIMARY KEY NOT NULL , " + COL_USER_ID + " INTEGER NOT NULL " + " , " + COL_ALERT_MESSAGE + " TEXT NOT NULL , " + COL_ALERT_TIME +" DATETIME  NOT NULL );";

    @Override
    public void onCreate(SQLiteDatabase db2) {
        db = db2;
        db.execSQL (CREATE_TABLE_SIGNUP);
        db.execSQL (CREATE_TABLE_TRANSACTION);
        db.execSQL (CREATE_TABLE_ALERTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query1 = "DROP TABLE IF EXISTS " + TABLE_SIGNUP;
        String query2 = "DROP TABLE IF EXISTS " + TABLE_TRANSACT;
        String query3 = "DROP TABLE IF EXISTS " + TABLE_ALERTS;
        db.execSQL (query1);
        db.execSQL (query2);
        db.execSQL (query3);
        // Recreate tables with new schema
        onCreate(db);
    }

    public DatabaseHelper (Context context) {
        super(context,DATABASE_NAME,null, DATABASE_VERSION);
    }

    public void insertContact(Contact c, String googleUserId) {
        db = this.getWritableDatabase ();
        ContentValues values = new ContentValues ();
        values.put (COLUMN_NAME, c.getName ());
        values.put (COLUMN_EMAIL, c.getEmailId ());
        values.put (COLUMN_MOBILE, c.getMobile ());
        values.put (COLUMN_BUDGET, c.getBudget ());
        values.put (COLUMN_PASSWORD, c.getPassword ());
        values.put(COLUMN_GOOGLE_ID, googleUserId);
        db.insert (TABLE_SIGNUP, null, values);
    }

    public Contact getUserByGoogleId(String googleUserId) {
        db = this.getReadableDatabase();
        Contact c = new Contact();
        String query = "SELECT * FROM " + TABLE_SIGNUP + " WHERE " + COLUMN_GOOGLE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{googleUserId});

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            int mobileIndex = cursor.getColumnIndex(COLUMN_MOBILE);
            int budgetIndex = cursor.getColumnIndex(COLUMN_BUDGET);
            int nameIndex = cursor.getColumnIndex(COLUMN_NAME);

            if (idIndex >= 0) {
                c.setId(cursor.getInt(idIndex));
            }
            if (emailIndex >= 0) {
                c.setEmailId(cursor.getString(emailIndex));
            }
            if (passwordIndex >= 0) {
                c.setPassword(cursor.getString(passwordIndex));
            }
            if (mobileIndex >= 0) {
                c.setMobile(cursor.getLong(mobileIndex));
            }
            if (budgetIndex >= 0) {
                c.setBudget(cursor.getInt(budgetIndex));
            }
            if (nameIndex >= 0) {
                c.setName(cursor.getString(nameIndex));
            }
        }
        cursor.close();
        db.close();
        return c;
    }

    public List<String> searchUserId(String googleUserId) {
        String id = null, pass = "Not Found", budget = "1000";
        List<String> list = new ArrayList<>();
        db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_ID + ", " + COLUMN_EMAIL + ", " + COLUMN_PASSWORD + ", " + COLUMN_NAME + ", " + COLUMN_BUDGET +
                " FROM " + TABLE_SIGNUP +
                " WHERE " + COLUMN_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{googleUserId});

        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getString(0);        
            String email = cursor.getString(1);
            pass = cursor.getString(2);      
            Utils.userName = cursor.getString(3);  
            budget = cursor.getString(4);    

            Log.i("User Details", "ID: " + id + ", Name: " + Utils.userName);

            list.add(pass);
            list.add(id);
            list.add(budget);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return list;
    }

    public void insertTransaction(Transactions t) {
        db = this.getWritableDatabase ();
        ContentValues values = new ContentValues ();
        values.put (COL_U_ID, t.getUid());
        values.put (COL_TAG, t.getTag ());
        values.put (COL_AMOUNT, t.getAmount());
        values.put (COL_EXIN, t.getExin ());
        values.put (COL_DATETIME, DatabaseHelper.getDateTime ());
        db.insert (TABLE_TRANSACT, null, values);
    }

    // Keep only the version that returns long
    public long insertRemainder(AlertsTable t) {
        db = this.getWritableDatabase ();
        ContentValues values = new ContentValues ();
        values.put (COL_USER_ID, t.getUid ());
        values.put (COL_ALERT_MESSAGE, t.getMessage ());
        values.put (COL_ALERT_TIME, t.getalert_at ());
        // Return the ID of the newly inserted row
        long id = db.insert (TABLE_ALERTS, null, values);
        // db.close(); // Closing db here might cause issues if used immediately after
        return id;
    }


    public List<String> searchPass (String user) {
        String u, id = null, pass = "Not Found", budget = "1000";
        List<String> list = new ArrayList<> ();
        db = this.getReadableDatabase ();
        String query = "SELECT " + COLUMN_ID + ", " + COLUMN_EMAIL + ", " + COLUMN_PASSWORD +  ", " + COLUMN_NAME + ", " + COLUMN_BUDGET + " FROM " + TABLE_SIGNUP;
        Cursor cursor = db.rawQuery (query, null);
        if (cursor.getCount () > 0) {
            if (cursor.moveToFirst ( )) {
                do {
                    u = cursor.getString (1);
                    Log.i ("user", u);
                    if (u.equals (user)) {
                        pass = cursor.getString (2);
                        id = cursor.getString (0);
                        budget = cursor.getString (4);
                        Utils.userName = cursor.getString (3);
                        Log.i ("Password & UID", pass + id);
                        break;
                    }
                } while (cursor.moveToNext ( ));
            }
            list.add (pass);
            list.add (id);
            list.add (budget);
        }
        cursor.close ();
        db.close ();
        return list;
    }

    public Contact getUser () {
        db = this.getReadableDatabase ();
        Contact c = new Contact ();
        String query = "SELECT " + COLUMN_ID + ", " + COLUMN_EMAIL + ", " + COLUMN_PASSWORD +  ", " + COLUMN_NAME + ", " + COLUMN_BUDGET +  ", "+COLUMN_MOBILE +  " FROM " + TABLE_SIGNUP + " WHERE " + COLUMN_ID + " = " + Utils.userId + ";";
        Cursor cursor = db.rawQuery (query, null);
        if (cursor.moveToFirst ()) {
                c.setId (Integer.parseInt (cursor.getString (0)));
                c.setEmailId (cursor.getString (1));
                c.setPassword (cursor.getString (2));
                c.setMobile (Long.parseLong (cursor.getString (5)));
                c.setBudget (Integer.parseInt (cursor.getString (4)));
                c.setName (cursor.getString (3));
        }
        return c;
    }

    public void changeBudget () {
        db = this.getWritableDatabase ();

        SQLiteDatabase db = this.getWritableDatabase();
        Contact c = getUser ();
        c.setBudget (Long.parseLong (Utils.budget));
        ContentValues values = new ContentValues ();
        values.put (COLUMN_NAME, c.getName ());
        values.put (COLUMN_EMAIL, c.getEmailId ());
        values.put (COLUMN_MOBILE, c.getMobile ());
        values.put (COLUMN_BUDGET, c.getBudget ());
        values.put (COLUMN_PASSWORD, c.getPassword ());

        db.update(TABLE_SIGNUP, values, COLUMN_ID + " = ?",
                new String[] { String.valueOf(c.getId ()) });
    }

    public ArrayList<String> getTransactions () {
        ArrayList<String> list = new ArrayList<> ();
        db = this.getReadableDatabase ();
        String query = "SELECT " + COL_TID + ", " + COL_AMOUNT + ", " + COL_TAG + " , " + COL_DATETIME +" , " + COL_EXIN + ", " + COL_U_ID +" FROM " + TABLE_TRANSACT +" WHERE " + COL_U_ID + " = " + Utils.userId + " ORDER BY " + COL_DATETIME + " DESC ;" ;
        Cursor cursor = db.rawQuery (query, null);

        String tag, amount, exin, uid, timeA, timeB;
        if (cursor.moveToFirst ()) {
            if(cursor.getCount () > 0) {
                do {
                    int idxAmount   = cursor.getColumnIndex(COL_AMOUNT);
                    int idxTag      = cursor.getColumnIndex(COL_TAG);
                    int idxDatetime = cursor.getColumnIndex(COL_DATETIME);
                    int idxExin     = cursor.getColumnIndex(COL_EXIN);
                    int idxUid      = cursor.getColumnIndex(COL_U_ID);

                    amount = cursor.getString(idxAmount);
                    tag    = cursor.getString(idxTag);
                    timeA  = cursor.getString(idxDatetime);
                    exin   = cursor.getString(idxExin);
                    uid    = cursor.getString(idxUid);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault ());
                    Date sourceDate = null;
                    try {
                        sourceDate = dateFormat.parse(timeA);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat targetFormat = new SimpleDateFormat("EEE, MMM dd, yyyy HH:mm:ss a",Locale.getDefault ());
                    if (sourceDate != null) {
                        timeB = targetFormat.format(sourceDate);
                    } else {
                        timeB = timeA;
                    }

                    Log.i("PreethisTransaction",tag +" " + amount+ " " + timeB +" " + exin + " "+uid );
                    if ("0".equals (exin)) {
                       amount = "- € "+ amount;
                    }
                    else
                        amount = " € "+amount;
                    if(tag != null)
                        list.add("\n" + tag + "\n" + amount + "\n" + timeB + "\n");
                }while(cursor.moveToNext ());
            }
        }
        db.close ();
        cursor.close ();
        return list;
    }

    // Modified to accept date range for filtering
    public ArrayList<Transactions> getTransactionsPDF (String startDate, String endDate) {
        ArrayList<Transactions> list = new ArrayList<> ();
        db = this.getReadableDatabase ();
        Transactions t;
        String query = "SELECT " + COL_TID + ", " + COL_AMOUNT + ", " + COL_TAG + " , " + COL_DATETIME + " , " + COL_EXIN + " , " + COL_U_ID + " FROM " + TABLE_TRANSACT + " WHERE " + COL_U_ID + " = ?";
        ArrayList<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(String.valueOf(Utils.userId));

        if (startDate != null && endDate != null) {
            query += " AND " + COL_DATETIME + " BETWEEN ? AND ?";
            selectionArgsList.add(startDate);
            selectionArgsList.add(endDate);
        }
        query += " ORDER BY " + COL_DATETIME + " DESC ;";

        String[] selectionArgs = selectionArgsList.toArray(new String[0]);
        Cursor cursor = db.rawQuery (query, selectionArgs);

        String tag, amount, exin, uid, timeA, timeB;
        if (cursor.moveToFirst ()) {
            do {
            t = new Transactions ();
            t.setTid(cursor.getInt(cursor.getColumnIndex(COL_TID)));

            int idxAmount   = cursor.getColumnIndex(COL_AMOUNT);
            int idxTag      = cursor.getColumnIndex(COL_TAG);
            int idxDatetime = cursor.getColumnIndex(COL_DATETIME);
            int idxExin     = cursor.getColumnIndex(COL_EXIN);
            int idxUid      = cursor.getColumnIndex(COL_U_ID);

            amount = cursor.getString(idxAmount);
            tag    = cursor.getString(idxTag);
            timeA  = cursor.getString(idxDatetime);
            exin   = cursor.getString(idxExin);
            uid    = cursor.getString(idxUid);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault ());
                Date sourceDate = null;
                try {
                    sourceDate = dateFormat.parse(timeA);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat targetFormat = new SimpleDateFormat("EEE, MMM dd, yyyy HH:mm a",Locale.getDefault ());
                if (sourceDate != null) {
                    timeB = targetFormat.format(sourceDate);
                } else {
                    timeB = timeA;
                }

                Log.i("PreethisTransaction",tag +" " + amount+ " " + timeB+" " + exin + " "+uid );
                t.setUid (Integer.parseInt (uid));
                t.setTag (tag);
                t.setExin (Integer.parseInt (exin));
                t.setAmount (Integer.parseInt (amount));
                t.setCreated_at (timeB);
                list.add (t);
            }while(cursor.moveToNext ());
        }
        db.close ();
        cursor.close ();
        return list;
    }

    // Modified to accept date range for filtering
    public void setIncomeExpenses (String startDate, String endDate) {
        db = this.getReadableDatabase ();
        Utils.income = 0; // Reset before calculating
        Utils.expense = 0; // Reset before calculating

        // Base query parts
        String baseQuery = "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACT + " WHERE " + COL_U_ID + " = ?";
        String incomeCondition = " AND " + COL_EXIN + " = 1";
        String expenseCondition = " AND " + COL_EXIN + " = 0";
        String dateCondition = "";
        ArrayList<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(String.valueOf(Utils.userId));

        // Add date condition if applicable
        if (startDate != null && endDate != null) {
            dateCondition = " AND " + COL_DATETIME + " BETWEEN ? AND ?";
            selectionArgsList.add(startDate);
            selectionArgsList.add(endDate);
        }

        String[] selectionArgs = selectionArgsList.toArray(new String[0]);

        // Income Query
        String incomeQuery = baseQuery + incomeCondition + dateCondition + ";";
        Cursor c1 = db.rawQuery (incomeQuery, selectionArgs);
        if (c1.moveToFirst ()) {
            String income = c1.getString (0);
            if (income != null) {
                Utils.income = Integer.parseInt (income);
                Log.i("INCOME", String.valueOf (Utils.income));
            }
        }
        c1.close ();

        // Expense Query
        // Note: Need to rebuild selectionArgs if dates were added, as the list was consumed by the first query.
        // Re-create the list for the expense query.
        ArrayList<String> expenseSelectionArgsList = new ArrayList<>();
        expenseSelectionArgsList.add(String.valueOf(Utils.userId));
        if (startDate != null && endDate != null) {
            expenseSelectionArgsList.add(startDate);
            expenseSelectionArgsList.add(endDate);
        }
        String[] expenseSelectionArgs = expenseSelectionArgsList.toArray(new String[0]);

        String expenseQuery = baseQuery + expenseCondition + dateCondition + ";";
        Cursor c2 = db.rawQuery (expenseQuery, expenseSelectionArgs);
        if (c2.moveToFirst () ) {
            String expense = c2.getString (0);
            if (expense != null)
            Utils.expense = Integer.parseInt (expense);
            Log.i("EXPENSE", String.valueOf (Utils.expense));
        }
        c2.close ();
    }

    // Modified to accept date range for filtering
    public HashMap<String, Integer> getExpenses (String startDate, String endDate) {
        db = this.getReadableDatabase ();
        HashMap<String, Integer> list = new HashMap<> ();

        String query = "SELECT SUM(" + COL_AMOUNT + "), " + COL_TAG + " FROM " + TABLE_TRANSACT + " WHERE " + COL_U_ID + " = ? AND " + COL_EXIN + " = 0";
        ArrayList<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(String.valueOf(Utils.userId));

        if (startDate != null && endDate != null) {
            query += " AND " + COL_DATETIME + " BETWEEN ? AND ?";
            selectionArgsList.add(startDate);
            selectionArgsList.add(endDate);
        }
        query += " GROUP BY " + COL_TAG;

        String[] selectionArgs = selectionArgsList.toArray(new String[0]);
        Cursor cursor = db.rawQuery (query, selectionArgs);
        String tag, amount;
        if (cursor.moveToFirst ()) {
            do {
                tag = cursor.getString (1);
                amount = cursor.getString (0);
                list.put(tag, Integer.parseInt (amount));
                Log.i("PreethisExpenses",tag +" " + amount);
            }while(cursor.moveToNext ());
        }
        db.close ();
        cursor.close ();
        return list;
    }

    // Modified to accept date range for filtering
    public HashMap<String, Integer> getIncomes (String startDate, String endDate) {
        db = this.getReadableDatabase ();
        HashMap<String, Integer> list = new HashMap<> ();

        String query = "SELECT SUM(" + COL_AMOUNT + "), " + COL_TAG + " FROM " + TABLE_TRANSACT + " WHERE " + COL_U_ID + " = ? AND " + COL_EXIN + " = 1";
        ArrayList<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(String.valueOf(Utils.userId));

        if (startDate != null && endDate != null) {
            query += " AND " + COL_DATETIME + " BETWEEN ? AND ?";
            selectionArgsList.add(startDate);
            selectionArgsList.add(endDate);
        }
        query += " GROUP BY " + COL_TAG + ";";

        String[] selectionArgs = selectionArgsList.toArray(new String[0]);
        Cursor cursor = db.rawQuery (query, selectionArgs);
        String tag, amount;
        if (cursor.moveToFirst ()) {
        do {
            tag = cursor.getString (1);
            amount = cursor.getString (0);
            list.put(tag, Integer.parseInt (amount));
            Log.i("IncomeCategories",tag +" " + amount);
        }while(cursor.moveToNext ());
    }
    db.close ();
    cursor.close ();
    return list;
}

    // New method to get distinct months with transactions
    public List<String> getDistinctTransactionMonths() {
        List<String> months = new ArrayList<>();
        db = this.getReadableDatabase();
        String query = "SELECT DISTINCT strftime('%Y-%m', " + COL_DATETIME + ") as month FROM " + TABLE_TRANSACT +
                       " WHERE " + COL_U_ID + " = ? ORDER BY month DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(Utils.userId)});

        if (cursor.moveToFirst()) {
            int monthIndex = cursor.getColumnIndex("month");
            if (monthIndex >= 0) {
                do {
                    months.add(cursor.getString(monthIndex));
                } while (cursor.moveToNext());
            } else {
                 Log.e("DatabaseHelper", "Month column not found in getDistinctTransactionMonths query result.");
            }
        }
        cursor.close();
        db.close();
        return months;
    }

    // Modified to return ArrayList<AlertsTable>
    public ArrayList<AlertsTable> getReminders () {
        db = this.getReadableDatabase ();
        ArrayList<AlertsTable> list = new ArrayList<>();
        // Add COL_ALERT_ID to the selection
        String query = "SELECT " + COL_ALERT_ID + ", " + COL_ALERT_TIME + ", " + COL_ALERT_MESSAGE + ", " + COL_USER_ID +
                       " FROM " + TABLE_ALERTS +
                       " WHERE " + COL_USER_ID + " = " + Utils.userId +
                       " AND " + COL_ALERT_TIME + " > datetime('now')" +
                       " ORDER BY " + COL_ALERT_TIME + " ASC";
        Cursor cursor = db.rawQuery (query, null);

        if (cursor.moveToFirst ()) {
            int alertIdIndex = cursor.getColumnIndex(COL_ALERT_ID);
            int alertTimeIndex = cursor.getColumnIndex(COL_ALERT_TIME);
            int alertMessageIndex = cursor.getColumnIndex(COL_ALERT_MESSAGE);
            int userIdIndex = cursor.getColumnIndex(COL_USER_ID);

            do {
                AlertsTable reminder = new AlertsTable();
                if(alertIdIndex != -1) reminder.setAid(cursor.getInt(alertIdIndex));
                if(alertTimeIndex != -1) reminder.setalert_at(cursor.getString(alertTimeIndex)); // Keep original DB format
                if(alertMessageIndex != -1) reminder.setMessage(cursor.getString(alertMessageIndex));
                if(userIdIndex != -1) reminder.setUid(cursor.getInt(userIdIndex));
                list.add(reminder);
            } while(cursor.moveToNext ());
        }
        cursor.close ();
        db.close ();
        return list;
    }

    public AlertsTable getReminderById(int reminderId) {
        db = this.getReadableDatabase();
        AlertsTable reminder = null;
        String query = "SELECT " + COL_ALERT_ID + ", " + COL_ALERT_TIME + ", " + COL_ALERT_MESSAGE + ", " + COL_USER_ID +
                       " FROM " + TABLE_ALERTS +
                       " WHERE " + COL_ALERT_ID + " = ? AND " + COL_USER_ID + " = " + Utils.userId;
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(reminderId)});

        if (cursor.moveToFirst()) {
            reminder = new AlertsTable();
            int alertIdIndex = cursor.getColumnIndex(COL_ALERT_ID);
            int alertTimeIndex = cursor.getColumnIndex(COL_ALERT_TIME);
            int alertMessageIndex = cursor.getColumnIndex(COL_ALERT_MESSAGE);
            int userIdIndex = cursor.getColumnIndex(COL_USER_ID);

            if(alertIdIndex != -1) reminder.setAid(cursor.getInt(alertIdIndex));
            if(alertTimeIndex != -1) reminder.setalert_at(cursor.getString(alertTimeIndex));
            if(alertMessageIndex != -1) reminder.setMessage(cursor.getString(alertMessageIndex));
            if(userIdIndex != -1) reminder.setUid(cursor.getInt(userIdIndex));
        }
        cursor.close();
        db.close();
        return reminder;
    }

    public int updateReminder(AlertsTable reminder) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ALERT_MESSAGE, reminder.getMessage());
        values.put(COL_ALERT_TIME, reminder.getalert_at());
        // Update row
        int rowsAffected = db.update(TABLE_ALERTS, values, COL_ALERT_ID + " = ? AND " + COL_USER_ID + " = ?",
                new String[]{String.valueOf(reminder.getAid()), String.valueOf(reminder.getUid())});
        db.close();
        return rowsAffected;
    }

    public void deleteReminder(int reminderId) {
        db = this.getWritableDatabase();
        db.delete(TABLE_ALERTS, COL_ALERT_ID + " = ? AND " + COL_USER_ID + " = ?",
                new String[]{String.valueOf(reminderId), String.valueOf(Utils.userId)});
        db.close();
    }

    private static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    // Delete transaction by ID
    public void deleteTransaction(int id) {
        db = this.getWritableDatabase();
        int count = db.delete(TABLE_TRANSACT, COL_TID + " = ?", new String[]{String.valueOf(id)});
        Log.i("DatabaseHelper", "deleteTransaction id=" + id + " rows deleted=" + count);
        db.close();
    }

    // Update an existing transaction
    public void updateTransaction(Transactions t) {
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TAG, t.getTag());
        values.put(COL_AMOUNT, t.getAmount());
        values.put(COL_EXIN, t.getExin());
        // preserve existing timestamp; do not overwrite datetime on edit
        int count = db.update(TABLE_TRANSACT, values, COL_TID + " = ?", new String[]{String.valueOf(t.getTid())});
        Log.i("DatabaseHelper", "updateTransaction id=" + t.getTid() + " rows updated=" + count);
        db.close();
    }
    
    // Export database data as a JSON backup
    public String exportBackup() {
        try {
            JSONObject backup = new JSONObject();
            
            // Export contacts table
            JSONArray contacts = new JSONArray();
            String contactsQuery = "SELECT * FROM " + TABLE_SIGNUP;
            Cursor cursor = this.getReadableDatabase().rawQuery(contactsQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    JSONObject contact = new JSONObject();
                    contact.put("id", cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    contact.put("name", cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                    contact.put("email", cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                    contact.put("mobile", cursor.getLong(cursor.getColumnIndex(COLUMN_MOBILE)));
                    contact.put("budget", cursor.getInt(cursor.getColumnIndex(COLUMN_BUDGET)));
                    contact.put("password", cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
                    contact.put("google_id", cursor.getString(cursor.getColumnIndex(COLUMN_GOOGLE_ID)));
                    contacts.put(contact);
                } while (cursor.moveToNext());
            }
            cursor.close();
            backup.put("contacts", contacts);
            
            // Export transactions table
            JSONArray transactions = new JSONArray();
            String transactionsQuery = "SELECT * FROM " + TABLE_TRANSACT;
            cursor = this.getReadableDatabase().rawQuery(transactionsQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    JSONObject transaction = new JSONObject();
                    transaction.put("id", cursor.getInt(cursor.getColumnIndex(COL_TID)));
                    transaction.put("uid", cursor.getInt(cursor.getColumnIndex(COL_U_ID)));
                    transaction.put("tag", cursor.getString(cursor.getColumnIndex(COL_TAG)));
                    transaction.put("exin", cursor.getInt(cursor.getColumnIndex(COL_EXIN)));
                    transaction.put("amount", cursor.getInt(cursor.getColumnIndex(COL_AMOUNT)));
                    transaction.put("created_at", cursor.getString(cursor.getColumnIndex(COL_DATETIME)));
                    transactions.put(transaction);
                } while (cursor.moveToNext());
            }
            cursor.close();
            backup.put("transactions", transactions);
            
            // Export alerts table
            JSONArray alerts = new JSONArray();
            String alertsQuery = "SELECT * FROM " + TABLE_ALERTS;
            cursor = this.getReadableDatabase().rawQuery(alertsQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    JSONObject alert = new JSONObject();
                    alert.put("alert_id", cursor.getInt(cursor.getColumnIndex(COL_ALERT_ID)));
                    alert.put("user_id", cursor.getInt(cursor.getColumnIndex(COL_USER_ID)));
                    alert.put("message", cursor.getString(cursor.getColumnIndex(COL_ALERT_MESSAGE)));
                    alert.put("time", cursor.getString(cursor.getColumnIndex(COL_ALERT_TIME)));
                    alerts.put(alert);
                } while (cursor.moveToNext());
            }
            cursor.close();
            backup.put("alerts", alerts);
            
            return backup.toString();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating backup: " + e.getMessage());
            return "{}";
        }
    }
    
    // Import and restore database data from a JSON backup
    public void importBackup(String json) {
        try {
            JSONObject backup = new JSONObject(json);
            SQLiteDatabase db = this.getWritableDatabase();
            
            // Clear existing data
            db.delete(TABLE_SIGNUP, null, null);
            db.delete(TABLE_TRANSACT, null, null);
            db.delete(TABLE_ALERTS, null, null);
            
            // Import contacts
            JSONArray contacts = backup.getJSONArray("contacts");
            for (int i = 0; i < contacts.length(); i++) {
                JSONObject contact = contacts.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(COLUMN_ID, contact.getInt("id"));
                values.put(COLUMN_NAME, contact.getString("name"));
                values.put(COLUMN_EMAIL, contact.getString("email"));
                values.put(COLUMN_MOBILE, contact.getLong("mobile"));
                values.put(COLUMN_BUDGET, contact.getInt("budget"));
                values.put(COLUMN_PASSWORD, contact.getString("password"));
                values.put(COLUMN_GOOGLE_ID, contact.getString("google_id"));
                db.insert(TABLE_SIGNUP, null, values);
            }
            
            // Import transactions
            JSONArray transactions = backup.getJSONArray("transactions");
            for (int i = 0; i < transactions.length(); i++) {
                JSONObject transaction = transactions.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(COL_TID, transaction.getInt("id"));
                values.put(COL_U_ID, transaction.getInt("uid"));
                values.put(COL_TAG, transaction.getString("tag"));
                values.put(COL_EXIN, transaction.getInt("exin"));
                values.put(COL_AMOUNT, transaction.getInt("amount"));
                values.put(COL_DATETIME, transaction.getString("created_at"));
                db.insert(TABLE_TRANSACT, null, values);
            }
            
            // Import alerts
            JSONArray alerts = backup.getJSONArray("alerts");
            for (int i = 0; i < alerts.length(); i++) {
                JSONObject alert = alerts.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(COL_ALERT_ID, alert.getInt("alert_id"));
                values.put(COL_USER_ID, alert.getInt("user_id"));
                values.put(COL_ALERT_MESSAGE, alert.getString("message"));
                values.put(COL_ALERT_TIME, alert.getString("time"));
                db.insert(TABLE_ALERTS, null, values);
            }
            
            db.close();
            Log.d("DatabaseHelper", "Backup restored successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error restoring backup: " + e.getMessage());
        }
    }
}
