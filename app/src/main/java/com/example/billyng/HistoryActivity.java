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

}