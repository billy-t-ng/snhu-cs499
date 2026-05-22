package com.example.billyng;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billyng.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private CalendarView calendarHistory;
    private TextView tvSelectedDate;
    private RecyclerView rvEntriesForDay;
    private FloatingActionButton fabAdd;
    private TextView navHome, navHistory, navSettings;

    private String username = "";
    private String selectedDateIso = "";
    private DatabaseHelper db;
    private DayAdapter adapter;
    private final List<DatabaseHelper.WeightEntry> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        String incoming = getIntent().getStringExtra("username");
        if (incoming != null) username = incoming;

        db = new DatabaseHelper(this);

        bindViews();
        setupRecycler();
        setupClicks();

        Calendar cal = Calendar.getInstance();
        setSelectedDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        calendarHistory.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            setSelectedDate(year, month, dayOfMonth);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from Add or Edit screens
        loadEntriesForSelectedDate();
    }

    private void bindViews() {
        calendarHistory = findViewById(R.id.calendarHistory);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        rvEntriesForDay = findViewById(R.id.rvEntriesForDay);
        fabAdd = findViewById(R.id.fabAdd);
        navHome = findViewById(R.id.navHome);
        navHistory = findViewById(R.id.navHistory);
        navSettings = findViewById(R.id.navSettings);
    }

    private void setupRecycler() {
        rvEntriesForDay.setLayoutManager(new LinearLayoutManager(this));

        // Adapter with both Delete and Clicklisteners
        adapter = new DayAdapter(items, new DayAdapter.OnEntryListener() {
            @Override
            public void onDelete(DatabaseHelper.WeightEntry entry) {
                if (db.deleteWeightById(entry.id)) {
                    Toast.makeText(HistoryActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                    loadEntriesForSelectedDate();
                }
            }

            @Override
            public void onItemClick(DatabaseHelper.WeightEntry entry) {
                // Launch EditEntryActivity with the entry ID
                Intent intent = new Intent(HistoryActivity.this, EditEntryActivity.class);
                intent.putExtra("entryId", entry.id);
                startActivity(intent);
            }
        });
        rvEntriesForDay.setAdapter(adapter);
    }

    private void setupClicks() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, AddEntryActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });

        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, SmsPermissionActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    private void setSelectedDate(int year, int monthZeroBased, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, monthZeroBased, dayOfMonth);
        selectedDateIso = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
        tvSelectedDate.setText(new SimpleDateFormat("MMM d, yyyy", Locale.US).format(cal.getTime()));
        loadEntriesForSelectedDate();
    }

    private void loadEntriesForSelectedDate() {
        if (username.isEmpty() || selectedDateIso.isEmpty()) return;
        List<DatabaseHelper.WeightEntry> fresh = db.getWeightsForDate(username, selectedDateIso);
        items.clear();
        items.addAll(fresh);
        adapter.notifyDataSetChanged();
    }

    // Adapter Class
    private static class DayAdapter extends RecyclerView.Adapter<DayViewHolder> {
        interface OnEntryListener {
            void onDelete(DatabaseHelper.WeightEntry entry);
            void onItemClick(DatabaseHelper.WeightEntry entry);
        }

        private final List<DatabaseHelper.WeightEntry> items;
        private final OnEntryListener listener;

        DayAdapter(List<DatabaseHelper.WeightEntry> items, OnEntryListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_weight_entry, parent, false);
            return new DayViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            DatabaseHelper.WeightEntry entry = items.get(position);
            holder.tvEntryDate.setText(entry.date);
            String note = (entry.note == null) ? "" : entry.note.trim();
            holder.tvEntryNote.setText(String.format(Locale.US, "%.1f lbs %s", entry.weight,
                    note.isEmpty() ? "" : " • " + note));

            // Set listeners
            holder.itemView.setOnClickListener(v -> listener.onItemClick(entry));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(entry));
        }

        @Override
        public int getItemCount() { return items.size(); }
    }

    private static class DayViewHolder extends RecyclerView.ViewHolder {
        final TextView tvEntryDate, tvEntryNote;
        final android.widget.ImageButton btnDelete;

        DayViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            tvEntryDate = itemView.findViewById(R.id.tvEntryDate);
            tvEntryNote = itemView.findViewById(R.id.tvEntryNote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}