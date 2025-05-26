package com.thriftyApp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import com.thriftyApp.TransactionsActivity;
import android.widget.Toast;

public class TransactionAdapter extends ArrayAdapter<Transactions> {
    private Context context;

    public TransactionAdapter(Context context, List<Transactions> items) {
        super(context, 0, items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Transactions t = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_transaction_dashboard, parent, false);
        }

        TextView tv = convertView.findViewById(R.id.transactionText);
        String amountText = (t.getExin() == 1 ? "€ " : "- € ") + t.getAmount();
        tv.setText(t.getTag() + "\n" + amountText + "\n" + t.getCreated_at());

        ImageButton edit = convertView.findViewById(R.id.editButton);
        ImageButton del  = convertView.findViewById(R.id.deleteButton);

            edit.setOnClickListener(v -> {
                Toast.makeText(context, "Edit clicked: id=" + t.getTid(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context,
                    t.getExin() == 1 ? TakeActivity.class : PayActivity.class);
                // indicate origin for Show All tab
                intent.putExtra("from_all", context instanceof TransactionsActivity);
                intent.putExtra("transaction_id", t.getTid());
                intent.putExtra("amount", t.getAmount());
                intent.putExtra("tag", t.getTag());
                intent.putExtra("exin", t.getExin());
                intent.putExtra("created_at", t.getCreated_at());
                context.startActivity(intent);
                if (context instanceof Dashboard) {
                    ((Dashboard) context).finish();
                } else if (context instanceof TransactionsActivity) {
                    ((TransactionsActivity) context).finish();
                }
            });

del.setOnClickListener(v -> {
            Toast.makeText(context, "Delete clicked: id=" + t.getTid(), Toast.LENGTH_SHORT).show();
            new AlertDialog.Builder(context)
                .setTitle("Delete transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Yes", (d, w) -> {
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.deleteTransaction(t.getTid());
                    if (context instanceof Dashboard) {
                        Dashboard dashboard = (Dashboard) context;
                        // Use getter methods to access filter dates
                        dashboard.refreshData(dashboard.getCurrentFilterStartDate(), dashboard.getCurrentFilterEndDate());
                    } else if (context instanceof TransactionsActivity) {
                        // In TransactionsActivity, listView(null) should internally use its own current filter dates
                        ((TransactionsActivity) context).listView(null);
                    }
                })
                .setNegativeButton("No", null)
                .show();
        });

        return convertView;
    }
}
