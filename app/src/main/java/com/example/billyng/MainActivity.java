package com.example.billyng;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billyng.database.DatabaseHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private FloatingActionButton fabAdd;
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

        fabAdd = findViewById(R.id.fabAdd);
        navHome = findViewById(R.id.navHome);
        navHistory = findViewById(R.id.navHistory);
        navSettings = findViewById(R.id.navSettings);
    }

    private void setupClicks() {

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEntryActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

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
            tvLeftWeight.setText("Current: --");
            tvRightToGoal.setText("To goal: --");
            tvLastEntry.setText("Last entry: --");
            return;
        }

        // Get most recent entry (limit 1)
        List<DatabaseHelper.WeightEntry> mostRecent = db.getRecentWeights(username, 1);
        if (mostRecent.isEmpty()) {
            tvLeftWeight.setText("Current: --");
            tvRightToGoal.setText("To goal: --");
            tvLastEntry.setText("Last entry: --");
            return;
        }

        DatabaseHelper.WeightEntry entry = mostRecent.get(0);
        tvLeftWeight.setText(String.format(Locale.US, "Current: %.1f", entry.weight));
        tvLastEntry.setText("Last entry: " + entry.date);

        // Not implemented yet, placeholder
        tvRightToGoal.setText("To goal: --");
    }

    private static class WeightAdapter extends RecyclerView.Adapter<WeightViewHolder> {

        interface OnDeleteClick {
            void onDelete(DatabaseHelper.WeightEntry entry);
        }

        private final List<DatabaseHelper.WeightEntry> items;
        private final OnDeleteClick onDeleteClick;

        WeightAdapter(List<DatabaseHelper.WeightEntry> items, OnDeleteClick onDeleteClick) {
            this.items = items;
            this.onDeleteClick = onDeleteClick;
        }

        @NonNull
        @Override
        public WeightViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_weight_entry, parent, false);
            return new WeightViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
            DatabaseHelper.WeightEntry entry = items.get(position);

            holder.tvEntryDate.setText(entry.date);

            String note = (entry.note == null) ? "" : entry.note.trim();
            if (note.isEmpty()) {
                holder.tvEntryNote.setText(String.format(Locale.US, "%.1f lbs", entry.weight));
            } else {
                holder.tvEntryNote.setText(String.format(Locale.US, "%.1f lbs  •  %s", entry.weight, note));
            }

            holder.btnDelete.setOnClickListener(v -> onDeleteClick.onDelete(entry));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class WeightViewHolder extends RecyclerView.ViewHolder {

        final TextView tvEntryDate;
        final TextView tvEntryNote;
        final android.widget.ImageButton btnDelete;

        WeightViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvEntryDate = itemView.findViewById(R.id.tvEntryDate);
            tvEntryNote = itemView.findViewById(R.id.tvEntryNote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
