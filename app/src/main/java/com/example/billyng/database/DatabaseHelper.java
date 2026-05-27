package com.example.billyng.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

// Database
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weight_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table: Users
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    // Table: Weight Entries
    private static final String TABLE_WEIGHTS = "weights";
    private static final String COL_W_ID = "id";
    private static final String COL_W_USERNAME = "username";
    private static final String COL_W_DATE = "date";
    private static final String COL_W_VALUE = "weight_value";
    private static final String COL_W_NOTE = "note";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table for login info
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)";
        db.execSQL(createUsers);

        // Create Weights table for the database
        String createWeights = "CREATE TABLE " + TABLE_WEIGHTS + " (" +
                COL_W_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_W_USERNAME + " TEXT, " +
                COL_W_DATE + " TEXT, " +
                COL_W_VALUE + " REAL, " +
                COL_W_NOTE + " TEXT)";
        db.execSQL(createWeights);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        onCreate(db);
    }

    // User (Login/Account Creation) ---

    public boolean createUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // CRUD

    // CREATE: Add item to database
    public boolean insertWeightEntry(String username, String date, double weight, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_W_USERNAME, username);
        cv.put(COL_W_DATE, date);
        cv.put(COL_W_VALUE, weight);
        cv.put(COL_W_NOTE, note);
        long result = db.insert(TABLE_WEIGHTS, null, cv);
        return result != -1;
    }

    // READ: View all items (returns list for UI grid/RecyclerView)
    public List<WeightEntry> getWeightsForDate(String username, String date) {
        List<WeightEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS, null, COL_W_USERNAME + "=? AND " + COL_W_DATE + "=?",
                new String[]{username, date}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new WeightEntry(
                        cursor.getLong(0),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getString(4)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<WeightEntry> getRecentWeights(String username, int limit) {
        List<WeightEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS, null, COL_W_USERNAME + "=?",
                new String[]{username}, null, null, COL_W_ID + " DESC", String.valueOf(limit));

        if (cursor.moveToFirst()) {
            do {
                list.add(new WeightEntry(
                        cursor.getLong(0),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getString(4)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // UPDATE: Change value of individual database items
    public boolean updateWeightById(long id, double newWeight, String newNote) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_W_VALUE, newWeight);
        cv.put(COL_W_NOTE, newNote);
        int result = db.update(TABLE_WEIGHTS, cv, COL_W_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // DELETE: Remove items from database
    public boolean deleteWeightById(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_WEIGHTS, COL_W_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public WeightEntry getWeightById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS, null, COL_W_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            WeightEntry entry = new WeightEntry(
                    cursor.getLong(0),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getString(4));
            cursor.close();
            return entry;
        }
        return null;
    }

    // SMS Notification Logic


    //Functions based on individual user's permission response.
    public void sendGoalReachedSMS(Context context, String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            } catch (Exception e) {
                Toast.makeText(context, "SMS failed to send.", Toast.LENGTH_SHORT).show();
            }
        }
        // If permission is denied, the application continues to function without this feature.
    }


    public static class WeightEntry {
        public long id;
        public String date;
        public double weight;
        public String note;

        public WeightEntry(long id, String date, double weight, String note) {
            this.id = id;
            this.date = date;
            this.weight = weight;
            this.note = note;
        }
    }

    public List<WeightEntry> getWeightsSince(String username, String startDate) {
        List<WeightEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_WEIGHTS,
                null,
                COL_W_USERNAME + "=? AND " + COL_W_DATE + ">=?",
                new String[]{username, startDate},
                null,
                null,
                COL_W_DATE + " DESC, " + COL_W_ID + " DESC"
        );

        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new WeightEntry(
                            cursor.getLong(0),
                            cursor.getString(2),
                            cursor.getDouble(3),
                            cursor.getString(4)));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return list;
    }
    public List<WeightEntry> getAllWeights(String username) {
        List<WeightEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_WEIGHTS,
                null,
                COL_W_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                COL_W_DATE + " DESC, " + COL_W_ID + " DESC"
        );

        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new WeightEntry(
                            cursor.getLong(0),
                            cursor.getString(2),
                            cursor.getDouble(3),
                            cursor.getString(4)));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return list;
    }
}