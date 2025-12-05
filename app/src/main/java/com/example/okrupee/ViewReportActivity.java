package com.example.okrupee;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ViewReportActivity extends AppCompatActivity {

    // Views from your XML
    private ImageView btnBack;
    private TextView tvStartDate, tvEndDate;
    private SearchView searchView;
    private Spinner spinnerFilter;
    private TextView tvNetBalance, tvTotalEntries, tvYouGave, tvYouGot;
    private RecyclerView recyclerViewReport;
    private Button btnDownload;

    // Data
    private DatabaseHelper db;
    private List<TransactionModel> allTransactionsList;
    private List<TransactionModel> filteredTransactionsList;
    private ReportAdapter reportAdapter;
    private Calendar startDate, endDate;
    private SimpleDateFormat viewDateFormat; // For displaying "dd MMM yy"
    private SimpleDateFormat parseDateFormat; // For parsing "dd-MM-yyyy" from DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout file you created
        setContentView(R.layout.activity_view_report);

        // Initialize DB
        db = new DatabaseHelper(this);

        // Initialize Date Formats
        viewDateFormat = new SimpleDateFormat("dd MMM yy", Locale.getDefault());
        parseDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        // Find all views
        findViews();

        // Initialize lists
        allTransactionsList = new ArrayList<>();
        filteredTransactionsList = new ArrayList<>();

        // Setup
        setupRecyclerView();
        setupDatePickers();
        setupSpinner();
        setupSearchView();
        setupClickListeners();

        // Load data
        loadAllUserTransactions();

        // Initial filter and display
        filterAndDisplayTransactions();
    }

    private void findViews() {
        btnBack = findViewById(R.id.btn_back);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        searchView = findViewById(R.id.search_view);
        spinnerFilter = findViewById(R.id.spinner_filter);
        tvNetBalance = findViewById(R.id.tv_net_balance);
        tvTotalEntries = findViewById(R.id.tv_total_entries);
        tvYouGave = findViewById(R.id.tv_you_gave);
        tvYouGot = findViewById(R.id.tv_you_got);
        recyclerViewReport = findViewById(R.id.recycler_view_report);
        btnDownload = findViewById(R.id.btn_download);
    }

    private void setupRecyclerView() {
        recyclerViewReport.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new ReportAdapter(this, filteredTransactionsList);
        recyclerViewReport.setAdapter(reportAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDownload.setOnClickListener(v -> {
            // TODO: Add PDF generation logic here
            Toast.makeText(this, "Download feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDatePickers() {
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();

        // Set default dates (e.g., start of the month to today)
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        updateDateTextViews();

        tvStartDate.setOnClickListener(v -> showDatePicker(true));
        tvEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar cal = isStartDate ? startDate : endDate;
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    if (isStartDate) {
                        startDate.set(year, month, dayOfMonth);
                    } else {
                        endDate.set(year, month, dayOfMonth);
                    }
                    updateDateTextViews();
                    filterAndDisplayTransactions(); // Re-filter on date change
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateDateTextViews() {
        tvStartDate.setText(viewDateFormat.format(startDate.getTime()).toUpperCase());
        tvEndDate.setText(viewDateFormat.format(endDate.getTime()).toUpperCase());
    }

    private void setupSpinner() {
        // The ArrayAdapter will use the array you defined in strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.report_filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplayTransactions(); // Re-filter on selection
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAndDisplayTransactions(); // Re-filter on text change
                return true;
            }
        });
    }

    private void loadAllUserTransactions() {
        // TODO: You need a way to get the logged-in user's ID
        // I'll assume you have a SessionManager or SharedPreferences
        // For this example, I will hardcode userId = 1.
        // Replace '1' with your method of getting the user ID.
        int loggedInUserId = 1;

        allTransactionsList.clear();
        // We will use a NEW method in DatabaseHelper to get all transactions
        allTransactionsList.addAll(db.getAllTransactionsForUser(loggedInUserId));
    }

    /**
     * This is the main logic hub. It filters the master list
     * into the displayed list based on all filters.
     */
    private void filterAndDisplayTransactions() {
        String query = searchView.getQuery().toString().toLowerCase();
        String filterType = spinnerFilter.getSelectedItem().toString();

        filteredTransactionsList.clear();

        // Set start date to the beginning of the day (00:00:00)
        Calendar startDateStartOfDay = (Calendar) startDate.clone();
        startDateStartOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startDateStartOfDay.set(Calendar.MINUTE, 0);
        startDateStartOfDay.set(Calendar.SECOND, 0);

        // Set end date to the end of the day (23:59:59)
        Calendar endDateEndOfDay = (Calendar) endDate.clone();
        endDateEndOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endDateEndOfDay.set(Calendar.MINUTE, 59);
        endDateEndOfDay.set(Calendar.SECOND, 59);

        for (TransactionModel txn : allTransactionsList) {
            // 1. Check Date
            Calendar txnDate = parseDateString(txn.getDate());
            if (txnDate == null) continue; // Skip invalid dates

            if (txnDate.before(startDateStartOfDay) || txnDate.after(endDateEndOfDay)) {
                continue; // Date is out of range
            }

            // 2. Check Filter Type ("All", "You Got", "You Gave")
            if (filterType.equals("You Got") && txn.getAmount() < 0) {
                continue; // Skip "Gave" transactions
            }
            if (filterType.equals("You Gave") && txn.getAmount() > 0) {
                continue; // Skip "Got" transactions
            }

            // 3. Check Search Query
            boolean nameMatch = txn.getCustomerName() != null && txn.getCustomerName().toLowerCase().contains(query);
            boolean remarksMatch = txn.getTitle() != null && txn.getTitle().toLowerCase().contains(query);

            if (!query.isEmpty() && !nameMatch && !remarksMatch) {
                continue; // Search query doesn't match name or remarks
            }

            // If all checks pass, add to the filtered list
            filteredTransactionsList.add(txn);
        }

        // Update the list and the summary cards
        reportAdapter.notifyDataSetChanged();
        updateSummary();
    }

    private void updateSummary() {
        long totalGot = 0;
        long totalGave = 0;

        for (TransactionModel txn : filteredTransactionsList) {
            if (txn.getAmount() > 0) {
                totalGot += txn.getAmount();
            } else {
                totalGave += txn.getAmount(); // This is a negative number
            }
        }

        long netBalance = totalGot + totalGave;
        NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());

        // Set Total Entries
        tvTotalEntries.setText(filteredTransactionsList.size() + " Entries");

        // Set You Got
        tvYouGot.setText("₹" + formatter.format(totalGot));

        // Set You Gave (use Math.abs to show a positive number)
        tvYouGave.setText("₹" + formatter.format(Math.abs(totalGave)));

        // Set Net Balance and color
        tvNetBalance.setText("₹" + formatter.format(netBalance));
        if (netBalance >= 0) {
            tvNetBalance.setTextColor(Color.parseColor("#388E3C")); // Green
        } else {
            tvNetBalance.setTextColor(Color.parseColor("#D32F2F")); // Red
        }
    }

    /**
     * Helper to parse date strings from the database ("dd-MM-yyyy")
     */
    private Calendar parseDateString(String dateStr) {
        try {
            Date date = parseDateFormat.parse(dateStr);
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return cal;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}