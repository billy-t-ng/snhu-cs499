package com.example.billyng.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weight_tracker.db";
    private static final int DATABASE_VERSION = 2;

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

    // Table: Goals
    private static final String TABLE_GOALS = "goals";
    private static final String COL_G_ID = "id";
    private static final String COL_G_USERNAME = "username";
    private static final String COL_G_WEIGHT = "goal_weight";
    private static final String COL_G_DATE = "date_set";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table with hashed password storage
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL)");

        // Weights table
        db.execSQL("CREATE TABLE " + TABLE_WEIGHTS + " (" +
                COL_W_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_W_USERNAME + " TEXT NOT NULL, " +
                COL_W_DATE + " TEXT NOT NULL, " +
                COL_W_VALUE + " REAL NOT NULL, " +
                COL_W_NOTE + " TEXT)");

        // Goals table, one goal per user
        db.execSQL("CREATE TABLE " + TABLE_GOALS + " (" +
                COL_G_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_G_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COL_G_WEIGHT + " REAL NOT NULL, " +
                COL_G_DATE + " TEXT NOT NULL)");

        // Index for faster date range queries on the weights table
        db.execSQL("CREATE INDEX idx_weights_username_date ON " +
                TABLE_WEIGHTS + " (" + COL_W_USERNAME + ", " + COL_W_DATE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add goals table
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_GOALS + " (" +
                    COL_G_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_G_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COL_G_WEIGHT + " REAL NOT NULL, " +
                    COL_G_DATE + " TEXT NOT NULL)");

            // Add index for date range queries
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_weights_username_date ON " +
                    TABLE_WEIGHTS + " (" + COL_W_USERNAME + ", " + COL_W_DATE + ")");

            // Rebuild users table so all accounts use hashed passwords.
            // Weight entries stay intact because the weights table is not touched.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COL_PASSWORD + " TEXT NOT NULL)");
        }
    }

    // --- Security ---

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // --- User Authentication ---

    public boolean createUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, hashPassword(password));
        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                        COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, hashPassword(password)});
        try {
            return cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
    }

    // --- Goal Weight ---

    public boolean setGoal(String username, double goalWeight, String dateSet) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_G_USERNAME, username);
        cv.put(COL_G_WEIGHT, goalWeight);
        cv.put(COL_G_DATE, dateSet);
        long result = db.insertWithOnConflict(TABLE_GOALS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public double getGoal(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GOALS, new String[]{COL_G_WEIGHT},
                COL_G_USERNAME + "=?", new String[]{username},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
            return -1;
        } finally {
            cursor.close();
        }
    }

    // --- Weight Entry CRUD ---

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

    public WeightEntry getWeightById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS, null, COL_W_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return new WeightEntry(
                        cursor.getLong(0),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getString(4));
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public List<WeightEntry> getWeightsForDate(String username, String date) {
        List<WeightEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS, null,
                COL_W_USERNAME + "=? AND " + COL_W_DATE + "=?",
                new String[]{username, date}, null, null, null);
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

    public List<WeightEntry> getRecentWeights(String username, int limit) {
        List<WeightEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS, null, COL_W_USERNAME + "=?",
                new String[]{username}, null, null,
                COL_W_DATE + " DESC, " + COL_W_ID + " DESC",
                String.valueOf(limit));
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

    public List<WeightEntry> getWeightsSince(String username, String startDate) {
        List<WeightEntry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS, null,
                COL_W_USERNAME + "=? AND " + COL_W_DATE + ">=?",
                new String[]{username, startDate}, null, null,
                COL_W_DATE + " DESC, " + COL_W_ID + " DESC");
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
        Cursor cursor = db.query(TABLE_WEIGHTS, null,
                COL_W_USERNAME + "=?", new String[]{username},
                null, null,
                COL_W_DATE + " DESC, " + COL_W_ID + " DESC");
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

    public boolean updateWeightById(long id, double newWeight, String newNote) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_W_VALUE, newWeight);
        cv.put(COL_W_NOTE, newNote);
        int result = db.update(TABLE_WEIGHTS, cv, COL_W_ID + "=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteWeightById(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_WEIGHTS, COL_W_ID + "=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }

    // --- SMS ---

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
    }

    // --- Data Model ---

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
}