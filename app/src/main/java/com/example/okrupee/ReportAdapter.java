package com.example.okrupee;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private Context context;
    private List<TransactionModel> transactionList;
    private NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());

    public ReportAdapter(Context context, List<TransactionModel> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We need to create an item layout file: R.layout.item_report_transaction
        View view = LayoutInflater.from(context).inflate(R.layout.item_report_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionModel transaction = transactionList.get(position);

        holder.tvCustomerName.setText(transaction.getCustomerName());
        holder.tvRemarks.setText(transaction.getTitle()); // "Title" holds the remarks
        holder.tvDate.setText(transaction.getDate());

        int amount = transaction.getAmount();
        String formattedAmount = "₹" + formatter.format(Math.abs(amount));

        if (amount >= 0) {
            holder.tvAmount.setText(formattedAmount);
            holder.tvAmount.setTextColor(Color.parseColor("#388E3C")); // Green
        } else {
            holder.tvAmount.setText(formattedAmount);
            holder.tvAmount.setTextColor(Color.parseColor("#D32F2F")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvRemarks, tvDate, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tv_item_customer_name);
            tvRemarks = itemView.findViewById(R.id.tv_item_remarks);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            tvAmount = itemView.findViewById(R.id.tv_item_amount);
        }
    }
}