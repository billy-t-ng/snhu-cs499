package com.example.billyng;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmsPermissionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;

    private TextView tvPermissionState;
    private TextView tvSmsResult;
    private Button btnEnableSms;
    private EditText etPhoneNumber;
    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String incoming = getIntent().getStringExtra("username");
        if (incoming != null) username = incoming;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_sms_permission);

        tvPermissionState = findViewById(R.id.tvPermissionState);
        tvSmsResult = findViewById(R.id.tvSmsResult);
        btnEnableSms = findViewById(R.id.btnEnableSms);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);

        // Load saved phone number
        SharedPreferences prefs = getSharedPreferences("weight_tracker", MODE_PRIVATE);
        String savedNumber = prefs.getString("phone_number", "");
        etPhoneNumber.setText(savedNumber);

        // Save when focus leaves the field
        etPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                savePhoneNumber();
            }
        });



        updateStatusUI();

        // Bottom navigation
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navHistory).setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePhoneNumber();
    }

    private void savePhoneNumber() {
        SharedPreferences prefs = getSharedPreferences("weight_tracker", MODE_PRIVATE);
        String current = etPhoneNumber.getText().toString().trim();
        String saved = prefs.getString("phone_number", "");

        if (!current.equals(saved)) {
            prefs.edit().putString("phone_number", current).apply();
            Toast.makeText(this, "Phone number saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatusUI() {
        SharedPreferences prefs = getSharedPreferences("weight_tracker", MODE_PRIVATE);
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            Toast.makeText(this, "Permission already granted!", Toast.LENGTH_SHORT).show();
        }
    }

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