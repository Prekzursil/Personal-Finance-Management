package com.thriftyApp;

import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast; // For delete confirmation
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private List<AlertsTable> reminderList;
    private Context context;
    private OnReminderActionListener actionListener;
    private SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM dd, yyyy HH:mm a", Locale.getDefault());

    // Interface for handling actions
    public interface OnReminderActionListener {
        void onEditReminder(int reminderId);
        void onDeleteReminder(int reminderId, int position);
    }

    public ReminderAdapter(Context context, List<AlertsTable> reminderList, OnReminderActionListener listener) {
        this.context = context;
        this.reminderList = reminderList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    // This is the onBindViewHolder method
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlertsTable reminder = reminderList.get(position);
        holder.messageTextView.setText(reminder.getMessage());

        try {
            Date date = dbFormat.parse(reminder.getalert_at());
            if (date != null) {
                holder.dateTimeTextView.setText(displayFormat.format(date));
            } else {
                holder.dateTimeTextView.setText(reminder.getalert_at()); // Fallback
            }
        } catch (ParseException e) {
            e.printStackTrace();
            holder.dateTimeTextView.setText(reminder.getalert_at()); // Fallback
        }

        holder.editButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditReminder(reminder.getAid());
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (actionListener != null) {
                new AlertDialog.Builder(context)
                    .setTitle("Delete Reminder")
                    .setMessage("Are you sure you want to delete this reminder?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        actionListener.onDeleteReminder(reminder.getAid(), holder.getAdapterPosition());
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            }
        });
    }

    // This is the getItemCount method
    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView dateTimeTextView;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.reminderMessageTextView);
            dateTimeTextView = itemView.findViewById(R.id.reminderDateTimeTextView);
            editButton = itemView.findViewById(R.id.editReminderButton);
            deleteButton = itemView.findViewById(R.id.deleteReminderButton);
        }
    }

    // Helper method to update the list of reminders
    public void setReminders(List<AlertsTable> newReminders) {
        this.reminderList.clear();
        this.reminderList.addAll(newReminders);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < reminderList.size()) {
            reminderList.remove(position);
            notifyItemRemoved(position);
            // Optional: notifyItemRangeChanged if positions change
        }
    }
}
