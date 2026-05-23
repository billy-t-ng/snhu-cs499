package com.example.billyng;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


 // Activity to manage SMS notification permissions.

public class SmsPermissionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;

    private TextView tvPermissionState;
    private TextView tvSmsResult;
    private Button btnEnableSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_sms_permission);

        // Initialize UI components using IDs from activity_sms_permission.xml
        tvPermissionState = findViewById(R.id.tvPermissionState);
        tvSmsResult = findViewById(R.id.tvSmsResult);
        btnEnableSms = findViewById(R.id.btnEnableSms);

        // Check current permission state on load
        updateStatusUI();
        // Bottom navigation
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        findViewById(R.id.navHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            finish();
        });

        // Phone number input
        android.widget.EditText etPhoneNumber = findViewById(R.id.etPhoneNumber);
        android.content.SharedPreferences prefs = getSharedPreferences("weight_tracker", MODE_PRIVATE);

        // Load saved number
        String savedNumber = prefs.getString("phone_number", "");
        etPhoneNumber.setText(savedNumber);

        // Save when focus leaves the field
        etPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                prefs.edit().putString("phone_number", etPhoneNumber.getText().toString().trim()).apply();
            }
        });
    }

    /**
     * Checks if permission is granted and updates the UI labels accordingly.
     * Ensures the app continues to function even if permission is denied.
     */
    private void updateStatusUI() {
        android.content.SharedPreferences prefs = getSharedPreferences("weight_tracker", MODE_PRIVATE);
        boolean alertsEnabled = prefs.getBoolean("sms_alerts_enabled", false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            tvPermissionState.setText("Permission status: Granted");

            if (alertsEnabled) {
                tvSmsResult.setText("Text alerts are currently ON.");
                btnEnableSms.setText("Disable SMS Alerts");
            } else {
                tvSmsResult.setText("Text alerts are currently OFF.");
                btnEnableSms.setText("Enable SMS Alerts");
            }

            btnEnableSms.setEnabled(true);
            btnEnableSms.setOnClickListener(v -> {
                prefs.edit().putBoolean("sms_alerts_enabled", !alertsEnabled).apply();
                updateStatusUI();
            });
        } else {
            tvPermissionState.setText("Permission status: Not granted");
            tvSmsResult.setText("Text alerts are currently OFF.");
            btnEnableSms.setText("Enable SMS Alerts");
            btnEnableSms.setEnabled(true);
            btnEnableSms.setOnClickListener(v -> requestSmsPermission());
        }
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Standard Android permission request dialog
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            Toast.makeText(this, "Permission already granted!", Toast.LENGTH_SHORT).show();
        }
    }


     // Callback for the result from requesting permissions.

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
                getSharedPreferences("weight_tracker", MODE_PRIVATE)
                        .edit().putBoolean("sms_alerts_enabled", true).apply();
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
            // Refresh UI to reflect the user's choice
            updateStatusUI();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}