package com.example.okrupee;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomerTransactionAdapter extends RecyclerView.Adapter<CustomerTransactionAdapter.ViewHolder> {

    Context context;
    List<TransactionModel> transactions;

    // --- NEW: Step 1 ---
    // Create an interface to send a signal back to the Activity
    public interface OnTransactionDeletedListener {
        void onTransactionDeleted(); // Method to call when delete happens
    }
    // --- END NEW ---

    private OnTransactionDeletedListener listener; // --- NEW: Listener variable

    public CustomerTransactionAdapter(Context context, List<TransactionModel> transactions) {
        this.context = context;
        this.transactions = transactions;

        // --- NEW: Step 2 ---
        // Save the listener from the context (the Activity)
        if (context instanceof OnTransactionDeletedListener) {
            this.listener = (OnTransactionDeletedListener) context;
        } else {
            // This is a safety check in case you forget to implement the interface
            throw new RuntimeException(context.toString() + " must implement OnTransactionDeletedListener");
        }
        // --- END NEW ---
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionModel model = transactions.get(position);
        holder.tvTitle.setText(model.getTitle());
        holder.tvAmount.setText("₹" + model.getAmount());
        holder.tvDate.setText(model.getDate());

        if (model.getAmount() >= 0) {
            holder.ivIcon.setImageResource(R.drawable.ic_up_green);
            holder.tvAmount.setTextColor(Color.parseColor("#28A745"));
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_down_red);
            holder.tvAmount.setTextColor(Color.parseColor("#DC3545"));
        }

        //delete transaction
        holder.itemView.setOnLongClickListener(v -> {

            // --- NEW: Get position safely ---
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return true; // Item is already being removed
            }
            // --- END NEW ---

            new AlertDialog.Builder(context)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Delete from database
                        DatabaseHelper db = new DatabaseHelper(context);
                        db.softDeleteTransaction(transactions.get(currentPosition).getId());

                        // Remove from list and update UI
                        transactions.remove(currentPosition);
                        notifyItemRemoved(currentPosition);

                        Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show();

                        // --- NEW: Step 3 ---
                        // Send the signal back to the Activity to update the balance
                        if (listener != null) {
                            listener.onTransactionDeleted();
                        }
                        // --- END NEW ---
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }


    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvAmount, tvDate;

        @SuppressLint("WrongViewCast")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.tv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}
