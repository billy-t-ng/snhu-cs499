package com.example.billyng;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.billyng.database.DatabaseHelper;

import java.util.Locale;


//Activity to modify existing weight entries.

public class EditEntryActivity extends AppCompatActivity {

    private TextView tvEditDate;
    private EditText etEditWeight;
    private EditText etEditNote;
    private Button btnSaveEdit;
    private Button btnCancelEdit;

    private DatabaseHelper db;
    private long entryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);

        db = new DatabaseHelper(this);

        // Link views to XML IDs
        tvEditDate = findViewById(R.id.tvEditDate);
        etEditWeight = findViewById(R.id.etEditWeight);
        etEditNote = findViewById(R.id.etEditNote);
        btnSaveEdit = findViewById(R.id.btnSaveEdit);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        // Retrieve entry ID passed from MainActivity or HistoryActivity
        entryId = getIntent().getLongExtra("entryId", -1);

        if (entryId == -1) {
            Toast.makeText(this, "Missing entry id.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load existing data into the fields
        loadEntryData();

        btnSaveEdit.setOnClickListener(v -> saveChanges());
        btnCancelEdit.setOnClickListener(v -> finish());
    }

    private void loadEntryData() {
        DatabaseHelper.WeightEntry entry = db.getWeightById(entryId);
        if (entry != null) {
            tvEditDate.setText("Date: " + entry.date);
            etEditWeight.setText(String.format(Locale.US, "%.1f", entry.weight));
            etEditNote.setText(entry.note == null ? "" : entry.note);
        } else {
            Toast.makeText(this, "Entry not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


     // Updates the record in the persistent database.
    private void saveChanges() {
        String weightStr = etEditWeight.getText().toString().trim();
        String noteStr = etEditNote.getText().toString().trim();

        if (TextUtils.isEmpty(weightStr)) {
            Toast.makeText(this, "Enter a weight.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double weightValue = Double.parseDouble(weightStr);
            if (weightValue <= 0) {
                Toast.makeText(this, "Weight must be greater than 0.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update operation in database
            boolean updated = db.updateWeightById(entryId, weightValue, noteStr);

            if (updated) {
                Toast.makeText(this, "Entry updated.", Toast.LENGTH_SHORT).show();
                finish(); // Returns to the previous screen (Home or History)
            } else {
                Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Weight must be a number.", Toast.LENGTH_SHORT).show();
        }
    }
}