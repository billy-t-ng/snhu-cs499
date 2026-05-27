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

        refreshForSelectedChip();
        updateSummary();

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
        refreshForSelectedChip();
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

        chip7d.setOnClickListener(v -> loadFilteredEntries(7));
        chip30d.setOnClickListener(v -> loadFilteredEntries(30));
        chip90d.setOnClickListener(v -> loadFilteredEntries(90));
    }

    private void setupRecycler() {
        rvWeightEntries.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WeightAdapter(items, new WeightAdapter.OnDeleteClick() {
            @Override
            public void onDelete(DatabaseHelper.WeightEntry entry) {
                boolean deleted = db.deleteWeightById(entry.id);
                if (deleted) {
                    Toast.makeText(MainActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                    refreshForSelectedChip();
                    updateSummary();
                } else {
                    Toast.makeText(MainActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rvWeightEntries.setAdapter(adapter);
    }

    private void refreshForSelectedChip() {
        int checkedId = chipGroupRange.getCheckedChipId();
        if (checkedId == R.id.chip7d) {
            loadFilteredEntries(7);
        } else if (checkedId == R.id.chip30d) {
            loadFilteredEntries(30);
        } else if (checkedId == R.id.chip90d) {
            loadFilteredEntries(90);
        } else {
            loadRecentEntries();
        }
    }

    private void loadFilteredEntries(int days) {
        if (username.isEmpty()) return;

        java.time.LocalDate cutoff = java.time.LocalDate.now().minusDays(days - 1);
        String startDate = cutoff.toString();

        List<DatabaseHelper.WeightEntry> filtered = db.getWeightsSince(username, startDate);

        items.clear();
        items.addAll(filtered);
        adapter.notifyDataSetChanged();

        updateWeightChangeSummary(filtered, days);
    }

    private void updateWeightChangeSummary(List<DatabaseHelper.WeightEntry> entries, int days) {
        if (entries.size() < 2) {
            tvLastEntry.setText("Not enough data for " + days + " day trend");
            return;
        }

        double newest = entries.get(0).weight;
        double oldest = entries.get(entries.size() - 1).weight;
        double change = newest - oldest;

        String direction;
        if (change > 0) {
            direction = "up";
        } else if (change < 0) {
            direction = "down";
        } else {
            direction = "no change";
        }

        if (change != 0) {
            tvLastEntry.setText(String.format(Locale.US, "%d day trend: %.1f lbs %s", days, Math.abs(change), direction));
        } else {
            tvLastEntry.setText(String.format(Locale.US, "%d day trend: no change", days));
        }
    }

    private void loadRecentEntries() {
        if (username.isEmpty()) return;

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

        List<DatabaseHelper.WeightEntry> mostRecent = db.getRecentWeights(username, 1);
        if (mostRecent.isEmpty()) {
            tvLeftWeight.setText("Current: --");
            tvRightToGoal.setText("Tap to set goal");
            tvLastEntry.setText("Last entry: --");
            return;
        }

        DatabaseHelper.WeightEntry entry = mostRecent.get(0);
        tvLeftWeight.setText(String.format(Locale.US, "Current: %.1f", entry.weight));

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