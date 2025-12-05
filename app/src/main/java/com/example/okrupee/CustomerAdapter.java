package com.example.okrupee;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    public interface OnCustomerChangeListener {
        void onCustomerDeleted();
    }
    private final Context context;
    private final List<CustomerModel> customerList;
    private ActivityResultLauncher<Intent> launcher;
    private OnCustomerChangeListener listener;

    DatabaseHelper db;

    public CustomerAdapter(Context context, List<CustomerModel> customerList,ActivityResultLauncher<Intent> launcher, OnCustomerChangeListener listener) {
        this.context = context;
        this.customerList = customerList;
        this.db = new DatabaseHelper(context);
        this.launcher = launcher;
        this.listener = listener;

    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer_item, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        CustomerModel customer = customerList.get(position);

        // Set name
        holder.tvName.setText(customer.getName());

        // Always show +91 prefix for number
        String number = customer.getPhone();
        if (number != null && !number.startsWith("+91")) {
            number = "+91 " + number;
        }
        holder.tvNumber.setText(number);

        // Show amount with ₹ sign
        String amountStr = customer.getAmount();
        if (amountStr == null || amountStr.trim().isEmpty()) {
            amountStr = "0";
        }
        holder.tvAmount.setText("₹" + amountStr);

        // Create initial from name safely
        String initial;
        if (customer.getName() != null && customer.getName().length() >= 2) {
            initial = customer.getName().substring(0, 2).toUpperCase();
        } else if (customer.getName() != null && customer.getName().length() > 0) {
            initial = customer.getName().substring(0, 1).toUpperCase();
        } else {
            initial = "NA";
        }
        holder.tvInitial.setText(initial);

        // On click → open detail activity
        String finalNumber = number;
        String finalAmountStr = amountStr;
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CustomerDetailActivity.class);
            intent.putExtra("customer_id", customerList.get(position).getId());
            intent.putExtra("name", customer.getName());
            intent.putExtra("number", finalNumber);
            intent.putExtra("amount", finalAmountStr);
            intent.putExtra("initial", initial);
            //context.startActivity(intent);
            launcher.launch(intent);
        });

        //long press delete customer
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Customer")
                    .setMessage("Are you sure you want to delete " + customerList.get(position).getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        int customerId = customerList.get(position).getId();

                        // Delete from database
                        db.deleteCustomer(customerId);

                        // Remove from adapter list
                        customerList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, customerList.size());

                        // Also remove from MainActivity's customersList
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).customersList.removeIf(c -> c.getId() == customerId);
                        }

                        if (listener != null) {
                            listener.onCustomerDeleted();
                        }

                        Toast.makeText(context, "Customer deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true; // handled
        });


        // Change color based on amount sign
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount >= 0) {
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.red));
            }
        } catch (NumberFormatException ignored) {}

    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber, tvInitial, tvAmount;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tv_initial);
            tvName = itemView.findViewById(R.id.tv_name);
            tvNumber = itemView.findViewById(R.id.tv_number);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }

}
