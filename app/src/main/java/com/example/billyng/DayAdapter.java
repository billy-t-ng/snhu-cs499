package com.example.billyng;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.billyng.database.DatabaseHelper;

import java.util.List;
import java.util.Locale;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    public interface OnEntryListener {
        void onDelete(DatabaseHelper.WeightEntry entry);
        void onItemClick(DatabaseHelper.WeightEntry entry);
    }

    private final List<DatabaseHelper.WeightEntry> items;
    private final OnEntryListener listener;

    public DayAdapter(List<DatabaseHelper.WeightEntry> items, OnEntryListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
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

        holder.itemView.setOnClickListener(v -> listener.onItemClick(entry));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(entry));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        final TextView tvEntryDate, tvEntryNote;
        final ImageButton btnDelete;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEntryDate = itemView.findViewById(R.id.tvEntryDate);
            tvEntryNote = itemView.findViewById(R.id.tvEntryNote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}