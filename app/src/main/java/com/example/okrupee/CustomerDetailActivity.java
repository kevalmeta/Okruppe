package com.example.okrupee;

import android.app.DatePickerDialog;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// --- NEW: Step 1 ---
// Implement the adapter's listener interface
public class CustomerDetailActivity extends AppCompatActivity
        implements CustomerTransactionAdapter.OnTransactionDeletedListener {

    private RecyclerView rvTransactions;
    private TextView tvBalance, tvCustomerName, tvInitial, tvNumber;
    private List<TransactionModel> transactionList;
    private CustomerTransactionAdapter transactionAdapter;

    // This will be set by updateBalanceFromDatabase()
    private double currentBalance = 0.0;

    private ImageButton btnBack;
    private Button btnReminder, btnYouGet, btnYouGive;
    private int customerId;
    DatabaseHelper db;
    private String customerName;   // 👈 ADD
    private String customerPhone;  // 👈 ADD
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        // --- find views (IDs must match your layout)
        rvTransactions = findViewById(R.id.rv_transaction_list);
        tvBalance = findViewById(R.id.tv_balance_amount);   // from your XML
        btnReminder = findViewById(R.id.btn_reminder);
        btnYouGet = findViewById(R.id.btn_you_get);
        btnYouGive = findViewById(R.id.btn_you_give);
        btnBack = findViewById(R.id.btn_back);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvInitial = findViewById(R.id.tv_initial);
        tvNumber = findViewById(R.id.tv_number);

        // --- receive intent extras (supports both your older/newer keys)
//        String name = getIntent().getStringExtra("name");
//        if (name == null) name = getIntent().getStringExtra("customer_name");
//
//        String number = getIntent().getStringExtra("number");
//        if (number == null) number = getIntent().getStringExtra("customer_number");

        customerName = getIntent().getStringExtra("name");
        if (customerName == null) customerName = getIntent().getStringExtra("customer_name");

        customerPhone = getIntent().getStringExtra("number");
        if (customerPhone == null) customerPhone = getIntent().getStringExtra("customer_number");

        String name = customerName;     // keep for header UI below
        String number = customerPhone;  // keep for header UI below

        String initial = getIntent().getStringExtra("initial");
        if (initial == null && name != null) initial = name.substring(0, Math.min(1, name.length())).toUpperCase();

        // --- set header (only if available)
        if (name != null) tvCustomerName.setText(name);
        if (number != null) tvNumber.setText(number);
        if (initial != null) tvInitial.setText(initial);

        //CustomerID
        customerId = getIntent().getIntExtra("customer_id", -1);

        if (customerId == -1) {
            Toast.makeText(this, "Error: No customer ID found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Back
        btnBack.setOnClickListener(v -> finish());

        // Reminder
        btnReminder.setOnClickListener(v -> showToast("Reminder Sent!"));

        db = new DatabaseHelper(this);
        transactionList = db.getTransactionsForCustomer(customerId);

        // Setup transactions and adapter
        setupRecyclerView();

        // --- UPDATED: Use the new method ---
        updateBalanceFromDatabase(); // set balance text from DB

        // YOU GET -> positive amount
        btnYouGet.setOnClickListener(v -> showAddTransactionDialog(true));

        // YOU GIVE -> negative amount
        btnYouGive.setOnClickListener(v -> showAddTransactionDialog(false));
    }

    // --- NEW: Step 2 ---
    // Add onResume to refresh data when returning to this activity
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list and balance in case we're returning
        // from another activity (like the recycle bin)
        transactionList.clear();
        transactionList.addAll(db.getTransactionsForCustomer(customerId));
        transactionAdapter.notifyDataSetChanged();

        // Use the new method to update balance from DB
        updateBalanceFromDatabase();
    }

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        // 'this' context now works as the listener because we implemented the interface
        transactionAdapter = new CustomerTransactionAdapter(this, transactionList);
        rvTransactions.setAdapter(transactionAdapter);
    }

    // --- NEW: Step 3 ---
    // This method is REQUIRED by the interface.
    // The adapter will call this automatically after a delete.
    @Override
    public void onTransactionDeleted() {
        // Now, just call your new, robust balance update logic!
        updateBalanceFromDatabase();
    }


    private void showAddTransactionDialog(boolean isCredit) {
        //new dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.get_give_screen);

        EditText etAmount = dialog.findViewById(R.id.gt_amount);
        EditText etDate = dialog.findViewById(R.id.gt_date);
        EditText etRemarks = dialog.findViewById(R.id.gt_remarks);
        Button btnAdd = dialog.findViewById(R.id.gt_add_entry);
        TextView tvTitle = dialog.findViewById(R.id.gt_YouGet);

        // adjust UI for You Give
        if (!isCredit) {
            tvTitle.setText("YOU GIVE");
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.red));
            btnAdd.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));
        } else {
            tvTitle.setText("YOU GET");
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.green));
            btnAdd.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
        }

        // Date picker
        etDate.setFocusable(false);
        etDate.setClickable(true);
        etDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int y = calendar.get(Calendar.YEAR);
            int m = calendar.get(Calendar.MONTH);
            int d = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dp = new DatePickerDialog(CustomerDetailActivity.this,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;
                        etDate.setText(selectedDate);
                    }, y, m, d);
            dp.show();
        });

        // Add entry click
        btnAdd.setOnClickListener(v -> {
            String amtText = etAmount.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String remarks = etRemarks.getText().toString().trim();

            if (amtText.isEmpty()) {
                showToast("Please enter amount");
                return;
            }
            if (date.isEmpty()) {
                showToast("Please choose date");
                return;
            }

            int amt;
            try {
                amt = Integer.parseInt(amtText);
            } catch (NumberFormatException e) {
                showToast("Invalid amount");
                return;
            }

            if (!isCredit) amt = -Math.abs(amt); // make negative for You Give

            //insert transaction
            long newTransactionId = db.insertTransaction(
                    customerId,
                    remarks.isEmpty() ? "Entry" : remarks,
                    amt,
                    date
            );

            if (newTransactionId == -1) {
                showToast("Failed to add transaction");
            } else {
                showToast("Transaction added successfully");
                // 👇 ADD THIS
                String smsType = isCredit ? "got" : "gave";
                sendTransactionSMS(customerPhone, customerName, Math.abs(amt), smsType);
            }

            // Create transaction and insert at top (using the new ID)
            TransactionModel newTxn = new TransactionModel(
                    (int) newTransactionId, // Use the ID returned from the DB
                    customerId,
                    remarks.isEmpty() ? "Entry" : remarks,
                    amt,
                    date
            );

            // --- UPDATED: Simplified logic ---

            // 1. Add to the local list for animation
            transactionList.add(0, newTxn);
            transactionAdapter.notifyItemInserted(0);
            rvTransactions.scrollToPosition(0);

            // 2. Refresh the balance from the DB (this updates UI and Customer table)
            updateBalanceFromDatabase();

            dialog.dismiss();
        });

        // Important: make the background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }

    // --- NEW: Step 4 ---
    // This is the new, robust method to update the balance
    // It reads from the DB, updates the Customer table, and updates the UI
    private void updateBalanceFromDatabase() {
        // 1. Get the new, correct balance from the DB
        currentBalance = db.getCustomerBalance(customerId);

        // 2. Update the balance in the CUSTOMERS table (so your main list is correct)
        db.updateCustomerAmount(customerId, currentBalance);

        // 3. Set the text and color on your TextView
        String formatted = "₹" + NumberFormat.getInstance(Locale.getDefault()).format(Math.abs(currentBalance));
        tvBalance.setText(formatted);
        tvBalance.setTextColor(Color.parseColor(currentBalance >= 0 ? "#28A745" : "#DC3545"));

        // 4. Set the result for the previous activity (MainActivity)
        // This ensures the main customer list also shows the new balance
        Intent resultIntent = new Intent();
        resultIntent.putExtra("customer_id", customerId);
        resultIntent.putExtra("new_amount", currentBalance);
        setResult(RESULT_OK, resultIntent);
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    //SMS feature
    private void sendTransactionSMS(String phoneNumber, String customerName, double amount, String type) {
        // Check if phone number exists
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "No phone number for this customer", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build the SMS message
        String message;
        if (type.equals("gave")) {
            message = "Dear " + customerName + ",\n"
                    + "Rs. " + amount + " credit added to your account.\n"
                    + "- OkRuppe App";
        } else {
            message = "Dear " + customerName + ",\n"
                    + "Rs. " + amount + " payment received. Thank you!\n"
                    + "- OkRuppe App";
        }

        // Check permission and send
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, "✅ SMS sent to " + customerName, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "❌ SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission granted! Please add transaction again.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "SMS Permission denied. SMS will not be sent.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
