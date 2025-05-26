package com.thriftyApp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LegendAdapter extends ListAdapter<PieEntry, LegendAdapter.LegendViewHolder> {

    private final OnLegendItemClickListener onClickListener;
    private int selectedPosition = -1; // To keep track of selected item for highlighting

    public interface OnLegendItemClickListener {
        void onItemClick(int position);
    }

    public LegendAdapter(List<PieEntry> entries, OnLegendItemClickListener listener) {
        super(new DiffCallback());
        this.onClickListener = listener;
        submitList(entries); // Submit the initial list
    }

    @NonNull
    @Override
    public LegendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_legend_chip, parent, false);
        return new LegendViewHolder(itemView, onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LegendViewHolder holder, int position) {
        PieEntry entry = getItem(position);
        holder.bind(entry, position == selectedPosition);
    }

    public void setSelectedPosition(int position) {
        int oldSelectedPosition = selectedPosition;
        selectedPosition = position;
        if (oldSelectedPosition != -1) {
            notifyItemChanged(oldSelectedPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }
    
    public void clearSelection() {
        int oldSelectedPosition = selectedPosition;
        selectedPosition = -1;
        if (oldSelectedPosition != -1) {
            notifyItemChanged(oldSelectedPosition);
        }
    }


    static class LegendViewHolder extends RecyclerView.ViewHolder {
        Chip chip;

        LegendViewHolder(@NonNull View itemView, OnLegendItemClickListener listener) {
            super(itemView);
            chip = itemView.findViewById(R.id.chip);
            chip.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position);
                }
            });
        }

        void bind(PieEntry entry, boolean isSelected) {
            // 1.  show “67.0 %  Pets” instead of just “Pets”
            float percent = entry.getValue();
            chip.setText(String.format(Locale.getDefault(), "%.1f%%  %s", percent, entry.getLabel()));

            // 2.  colour‑match chip background to slice
            if (entry.getData() instanceof Integer) {
                chip.setChipBackgroundColor(ColorStateList.valueOf((Integer) entry.getData()));
            } else {
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.LTGRAY)); // Default color if data is not an Integer
            }

            // 3.  clear visual emphasis
            if (isSelected) {
                chip.setChipStrokeWidth(4f);
                chip.setChipStrokeColor(ColorStateList.valueOf(Color.WHITE));
                chip.setTypeface(chip.getTypeface(), Typeface.BOLD);
                chip.setElevation(8f);
            } else {
                chip.setChipStrokeWidth(0f);
                chip.setTypeface(Typeface.DEFAULT);
                chip.setElevation(0f);
            }
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<PieEntry> {
        @Override
        public boolean areItemsTheSame(@NonNull PieEntry oldItem, @NonNull PieEntry newItem) {
            // Assuming label is unique enough for item identity
            return Objects.equals(oldItem.getLabel(), newItem.getLabel());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PieEntry oldItem, @NonNull PieEntry newItem) {
            return oldItem.equalTo(newItem) && Objects.equals(oldItem.getData(), newItem.getData());
        }
    }
}
