package com.example.billyng;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        setContentView(R.layout.activity_sms_permission);

        // Initialize UI components using IDs from activity_sms_permission.xml
        tvPermissionState = findViewById(R.id.tvPermissionState);
        tvSmsResult = findViewById(R.id.tvSmsResult);
        btnEnableSms = findViewById(R.id.btnEnableSms);

        // Check current permission state on load
        updateStatusUI();

        // Prompt for permission when button is clicked
        btnEnableSms.setOnClickListener(v -> requestSmsPermission());
    }

    /**
     * Checks if permission is granted and updates the UI labels accordingly.
     * Ensures the app continues to function even if permission is denied.
     */
    private void updateStatusUI() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            tvPermissionState.setText("Permission status: Granted");
            tvSmsResult.setText("Text alerts are currently ON.");
            btnEnableSms.setEnabled(false); // Disable button if already enabled
        } else {
            tvPermissionState.setText("Permission status: Not granted");
            tvSmsResult.setText("Text alerts are currently OFF.");
            btnEnableSms.setEnabled(true);
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
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
            // Refresh UI to reflect the user's choice
            updateStatusUI();
        }
    }
}