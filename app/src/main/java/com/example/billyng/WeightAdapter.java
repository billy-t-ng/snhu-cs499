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

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {

    public interface OnDeleteClick {
        void onDelete(DatabaseHelper.WeightEntry entry);
    }

    private final List<DatabaseHelper.WeightEntry> items;
    private final OnDeleteClick onDeleteClick;

    public WeightAdapter(List<DatabaseHelper.WeightEntry> items, OnDeleteClick onDeleteClick) {
        this.items = items;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
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

    static class WeightViewHolder extends RecyclerView.ViewHolder {

        final TextView tvEntryDate;
        final TextView tvEntryNote;
        final ImageButton btnDelete;

        WeightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEntryDate = itemView.findViewById(R.id.tvEntryDate);
            tvEntryNote = itemView.findViewById(R.id.tvEntryNote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}