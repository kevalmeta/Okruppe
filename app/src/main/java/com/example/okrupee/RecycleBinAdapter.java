package com.example.okrupee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecycleBinAdapter extends RecyclerView.Adapter<RecycleBinAdapter.ViewHolder> {

    Context context;
    ArrayList<TransactionModel> list;

    public RecycleBinAdapter(Context context, ArrayList<TransactionModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 1. Inflate your NEW layout file
        View view = LayoutInflater.from(context).inflate(R.layout.item_deleted_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 2. Get the model
        TransactionModel model = list.get(position);

        // 3. Bind all the data to the new TextViews
        holder.tvCustomerName.setText(model.getCustomerName());
        holder.tvTransactionRemarks.setText(model.getTitle()); // getTitle() gets the "remarks"
        holder.tvTransactionDate.setText(model.getDate());

        // 4. Set amount and color
        int amount = model.getAmount();
        if (amount < 0) {
            // "You Gave" - shown in red
            holder.tvTransactionAmount.setText("₹ " + Math.abs(amount));
            holder.tvTransactionAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else {
            // "You Got" - shown in green
            holder.tvTransactionAmount.setText("₹ " + amount);
            holder.tvTransactionAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // 5. ViewHolder class that finds all the NEW TextViews
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvCustomerName, tvTransactionRemarks, tvTransactionAmount, tvTransactionDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvTransactionRemarks = itemView.findViewById(R.id.tvTransactionRemarks);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
        }
    }
}
