package com.example.billyng;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.billyng.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEntryActivity extends AppCompatActivity {

    private EditText etWeight;
    private EditText etNote;
    private Button btnSaveEntry;
    private Button btnCancelEntry;

    private String username = "";
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        String incoming = getIntent().getStringExtra("username");
        if (incoming != null) username = incoming;

        db = new DatabaseHelper(this);

        etWeight = findViewById(R.id.etWeight);
        etNote = findViewById(R.id.etNote);
        btnSaveEntry = findViewById(R.id.btnSaveEntry);
        btnCancelEntry = findViewById(R.id.btnCancelEntry);
        btnSaveEntry.setOnClickListener(v -> saveEntry());
        btnCancelEntry.setOnClickListener(v -> finish());
    }

    private void saveEntry() {
        String weightStr = etWeight.getText().toString().trim();
        String noteStr = etNote.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "No logged-in user found.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(weightStr)) {
            Toast.makeText(this, "Enter a weight.", Toast.LENGTH_SHORT).show();
            return;
        }

        double weightValue;
        try {
            weightValue = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Weight must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (weightValue <= 0) {
            Toast.makeText(this, "Weight must be greater than 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        boolean inserted = db.insertWeightEntry(username, today, weightValue, noteStr);

        if (inserted) {
            Toast.makeText(this, "Entry saved.", Toast.LENGTH_SHORT).show();

            // SMS trigger using goal from database instead of SharedPreferences
            double goalWeight = db.getGoal(username);
            SharedPreferences prefs = getSharedPreferences("weight_tracker", MODE_PRIVATE);
            boolean alertsEnabled = prefs.getBoolean("sms_alerts_enabled", false);
            String phoneNumber = prefs.getString("phone_number", "");

            if (goalWeight > 0 && alertsEnabled && !phoneNumber.isEmpty() && weightValue <= goalWeight) {
                String msg = "Congratulations! You've reached your weight goal of " + goalWeight + " lbs!";
                db.sendGoalReachedSMS(this, phoneNumber, msg);
            }

            Intent intent = new Intent(AddEntryActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to save entry.", Toast.LENGTH_SHORT).show();
        }
    }
}