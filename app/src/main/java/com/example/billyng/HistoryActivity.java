package com.example.billyng;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billyng.database.DatabaseHelper;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private CalendarView calendarHistory;
    private TextView tvSelectedDate;
    private RecyclerView rvEntriesForDay;
    private RecyclerView rvAllEntries;
    private TextView navHome, navHistory, navSettings;
    private Chip chipCalendar, chipList;
    private EditText etSearch;
    private TextView tvSortToggle;

    private ConstraintLayout selectedDateRow;
    private CardView cardSelectedDay;
    private LinearLayout listViewGroup;

    private String username = "";
    private String selectedDateIso = "";
    private DatabaseHelper db;

    private DayAdapter dayAdapter;
    private final List<DatabaseHelper.WeightEntry> dayItems = new ArrayList<>();

    private DayAdapter listAdapter;
    private final List<DatabaseHelper.WeightEntry> allItems = new ArrayList<>();
    private final List<DatabaseHelper.WeightEntry> filteredItems = new ArrayList<>();
    private boolean sortNewestFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        String incoming = getIntent().getStringExtra("username");
        if (incoming != null) username = incoming;

        db = new DatabaseHelper(this);

        bindViews();
        setupRecyclers();
        setupClicks();
        setupSearch();

        chipCalendar.setChecked(true);
        showCalendarView();

        Calendar cal = Calendar.getInstance();
        setSelectedDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        calendarHistory.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            setSelectedDate(year, month, dayOfMonth);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEntriesForSelectedDate();
        loadAllEntries();
    }

    private void bindViews() {
        calendarHistory = findViewById(R.id.calendarHistory);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        rvEntriesForDay = findViewById(R.id.rvEntriesForDay);
        rvAllEntries = findViewById(R.id.rvAllEntries);
        navHome = findViewById(R.id.navHome);
        navHistory = findViewById(R.id.navHistory);
        navSettings = findViewById(R.id.navSettings);
        chipCalendar = findViewById(R.id.chipCalendar);
        chipList = findViewById(R.id.chipList);
        etSearch = findViewById(R.id.etSearch);
        tvSortToggle = findViewById(R.id.tvSortToggle);
        selectedDateRow = findViewById(R.id.selectedDateRow);
        cardSelectedDay = findViewById(R.id.cardSelectedDay);
        listViewGroup = findViewById(R.id.listViewGroup);
    }

    private void setupRecyclers() {
        rvEntriesForDay.setLayoutManager(new LinearLayoutManager(this));
        dayAdapter = new DayAdapter(dayItems, new DayAdapter.OnEntryListener() {
            @Override
            public void onDelete(DatabaseHelper.WeightEntry entry) {
                if (db.deleteWeightById(entry.id)) {
                    Toast.makeText(HistoryActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                    loadEntriesForSelectedDate();
                    loadAllEntries();
                }
            }

            @Override
            public void onItemClick(DatabaseHelper.WeightEntry entry) {
                Intent intent = new Intent(HistoryActivity.this, EditEntryActivity.class);
                intent.putExtra("entryId", entry.id);
                startActivity(intent);
            }
        });
        rvEntriesForDay.setAdapter(dayAdapter);

        rvAllEntries.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new DayAdapter(filteredItems, new DayAdapter.OnEntryListener() {
            @Override
            public void onDelete(DatabaseHelper.WeightEntry entry) {
                if (db.deleteWeightById(entry.id)) {
                    Toast.makeText(HistoryActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                    loadAllEntries();
                    loadEntriesForSelectedDate();
                }
            }

            @Override
            public void onItemClick(DatabaseHelper.WeightEntry entry) {
                Intent intent = new Intent(HistoryActivity.this, EditEntryActivity.class);
                intent.putExtra("entryId", entry.id);
                startActivity(intent);
            }
        });
        rvAllEntries.setAdapter(listAdapter);
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

        chipCalendar.setOnClickListener(v -> showCalendarView());
        chipList.setOnClickListener(v -> showListView());

        tvSortToggle.setOnClickListener(v -> {
            sortNewestFirst = !sortNewestFirst;
            tvSortToggle.setText(sortNewestFirst ? "Sort: Newest first" : "Sort: Oldest first");
            applyFilterAndSort();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilterAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showCalendarView() {
        calendarHistory.setVisibility(View.VISIBLE);
        selectedDateRow.setVisibility(View.VISIBLE);
        cardSelectedDay.setVisibility(View.VISIBLE);
        listViewGroup.setVisibility(View.GONE);
    }

    private void showListView() {
        calendarHistory.setVisibility(View.GONE);
        selectedDateRow.setVisibility(View.GONE);
        cardSelectedDay.setVisibility(View.GONE);
        listViewGroup.setVisibility(View.VISIBLE);
        loadAllEntries();
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
        dayItems.clear();
        dayItems.addAll(fresh);
        dayAdapter.notifyDataSetChanged();

        TextView tvDayEntriesTitle = findViewById(R.id.tvDayEntriesTitle);
        if (fresh.isEmpty()) {
            tvDayEntriesTitle.setText("No entries on this day");
        } else if (fresh.size() == 1) {
            tvDayEntriesTitle.setText("1 entry on this day");
        } else {
            tvDayEntriesTitle.setText(fresh.size() + " entries on this day");
        }
    }

    private void loadAllEntries() {
        if (username.isEmpty()) return;
        allItems.clear();
        allItems.addAll(db.getAllWeights(username));
        applyFilterAndSort();
    }

    private void applyFilterAndSort() {
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.US);

        filteredItems.clear();

        for (DatabaseHelper.WeightEntry entry : allItems) {
            if (query.isEmpty()) {
                filteredItems.add(entry);
            } else {
                boolean matchesDate = entry.date != null && entry.date.toLowerCase(Locale.US).contains(query);
                boolean matchesNote = entry.note != null && entry.note.toLowerCase(Locale.US).contains(query);
                if (matchesDate || matchesNote) {
                    filteredItems.add(entry);
                }
            }
        }

        if (sortNewestFirst) {
            Collections.sort(filteredItems, (a, b) -> {
                int dateCompare = b.date.compareTo(a.date);
                if (dateCompare != 0) return dateCompare;
                return Long.compare(b.id, a.id);
            });
        } else {
            Collections.sort(filteredItems, (a, b) -> {
                int dateCompare = a.date.compareTo(b.date);
                if (dateCompare != 0) return dateCompare;
                return Long.compare(a.id, b.id);
            });
        }

        listAdapter.notifyDataSetChanged();
    }
}