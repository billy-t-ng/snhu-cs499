package com.example.billyng;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billyng.database.DatabaseHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvLeftWeight;
    private TextView tvRightToGoal;
    private TextView tvLastEntry;

    private TextView tvViewAll;

    private ChipGroup chipGroupRange;
    private Chip chip7d, chip30d, chip90d;

    private RecyclerView rvWeightEntries;

    private TextView navHome, navHistory, navSettings;

    private String username = "";
    private DatabaseHelper db;

    private WeightAdapter adapter;
    private final List<DatabaseHelper.WeightEntry> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String incoming = getIntent().getStringExtra("username");
        if (incoming != null) username = incoming;

        db = new DatabaseHelper(this);

        bindViews();
        setupClicks();
        setupRecycler();

        chip7d.setChecked(true);

        // Initial load
        loadRecentEntries();
        updateSummary();
        // Goal weight tap to edit
        tvRightToGoal.setOnClickListener(v -> showGoalDialog());

        tvLeftWeight.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEntryActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When returning from AddEntry/History, refresh list + summary
        loadRecentEntries();
        updateSummary();
    }

    private void bindViews() {
        tvLeftWeight = findViewById(R.id.tvLeftWeight);
        tvRightToGoal = findViewById(R.id.tvRightToGoal);
        tvLastEntry = findViewById(R.id.tvLastEntry);

        tvViewAll = findViewById(R.id.tvViewAll);

        chipGroupRange = findViewById(R.id.chipGroupRange);
        chip7d = findViewById(R.id.chip7d);
        chip30d = findViewById(R.id.chip30d);
        chip90d = findViewById(R.id.chip90d);

        rvWeightEntries = findViewById(R.id.rvWeightEntries);

        navHome = findViewById(R.id.navHome);
        navHistory = findViewById(R.id.navHistory);
        navSettings = findViewById(R.id.navSettings);
    }

    private void setupClicks() {

        tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        navHome.setOnClickListener(v ->
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        );

        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SmsPermissionActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        chipGroupRange.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip7d) {
                Toast.makeText(this, "Range: 7 days", Toast.LENGTH_SHORT).show();
                //Data filtering not implemented yet
                loadRecentEntries();
            } else if (checkedId == R.id.chip30d) {
                Toast.makeText(this, "Range: 30 days", Toast.LENGTH_SHORT).show();
                loadRecentEntries();
            } else if (checkedId == R.id.chip90d) {
                Toast.makeText(this, "Range: 90 days", Toast.LENGTH_SHORT).show();
                loadRecentEntries();
            }
        });
    }

    private void setupRecycler() {
        rvWeightEntries.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WeightAdapter(items, new WeightAdapter.OnDeleteClick() {
            @Override
            public void onDelete(DatabaseHelper.WeightEntry entry) {
                boolean deleted = db.deleteWeightById(entry.id);
                if (deleted) {
                    Toast.makeText(MainActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                    loadRecentEntries();
                    updateSummary();
                } else {
                    Toast.makeText(MainActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rvWeightEntries.setAdapter(adapter);
    }

    private void loadRecentEntries() {
        if (username.isEmpty()) return;

        // Recent 10 entries on main screen
        List<DatabaseHelper.WeightEntry> fresh = db.getRecentWeights(username, 10);

        items.clear();
        items.addAll(fresh);
        adapter.notifyDataSetChanged();
    }

    private void updateSummary() {
        if (username.isEmpty()) {
            tvLeftWeight.setText("Tap to\nset weight");
            tvRightToGoal.setText("Tap to set goal");
            tvLastEntry.setText("Last entry: --");
            return;
        }

        // Get most recent entry (limit 1)
        List<DatabaseHelper.WeightEntry> mostRecent = db.getRecentWeights(username, 1);
        if (mostRecent.isEmpty()) {
            tvLeftWeight.setText("Current: --");
            tvRightToGoal.setText("Tap to set goal");
            tvLastEntry.setText("Last entry: --");
            return;
        }

        DatabaseHelper.WeightEntry entry = mostRecent.get(0);
        tvLeftWeight.setText(String.format(Locale.US, "Current: %.1f", entry.weight));
        tvLastEntry.setText("Last entry: " + entry.date);

        // Check for saved goal weight
        android.content.SharedPreferences prefs = getSharedPreferences("weight_tracker", MODE_PRIVATE);
        float goal = prefs.getFloat("goal_weight", -1);

        if (goal > 0) {
            double diff = Math.abs(entry.weight - goal);
            if (diff == 0) {
                tvRightToGoal.setText("Goal reached!");
            } else {
                tvRightToGoal.setText(String.format(Locale.US, "To goal: %.1f lbs", diff));
            }
        } else {
            tvRightToGoal.setText("Tap to set goal");
        }
    }

    private void showGoalDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Set Goal Weight");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter goal weight (lbs)");

        // Pre-fill with current goal if one exists
        android.content.SharedPreferences prefs = getSharedPreferences("weight_tracker", MODE_PRIVATE);
        float currentGoal = prefs.getFloat("goal_weight", -1);
        if (currentGoal > 0) {
            input.setText(String.valueOf(currentGoal));
        }

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                try {
                    float goal = Float.parseFloat(value);
                    if (goal > 0) {
                        prefs.edit().putFloat("goal_weight", goal).apply();
                        updateSummary();
                    }
                } catch (NumberFormatException e) {
                    android.widget.Toast.makeText(this, "Enter a valid number", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }




}
