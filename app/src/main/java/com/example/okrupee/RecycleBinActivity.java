package com.example.okrupee;

import android.os.Bundle;
import android.view.View; // Import View
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.appbar.MaterialToolbar; // Import Toolbar

import java.util.ArrayList;

public class RecycleBinActivity extends AppCompatActivity {

    private RecyclerView rvRecycleBin;

    // --- Use the new RecycleBinAdapter ---
    private RecycleBinAdapter recycleBinAdapter;

    private ArrayList<TransactionModel> deletedTransactions;
    private DatabaseHelper db;

    // --- Declarations for Toolbar and Empty View ---
    private MaterialToolbar toolbar;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_bin);

        // --- Find all the new views ---
        toolbar = findViewById(R.id.toolbarRecycleBin);
        rvRecycleBin = findViewById(R.id.rvRecycleBin);
        emptyView = findViewById(R.id.emptyView); // <-- Find the "empty" layout

        // --- Set up the Toolbar's back button ---
        toolbar.setNavigationOnClickListener(v -> {
            finish(); // Closes the activity and goes back
        });

        // Setup RecyclerView
        rvRecycleBin.setLayoutManager(new LinearLayoutManager(this));

        db = new DatabaseHelper(this);

        // Load deleted transactions from DB
        // (This now includes the customer name, thanks to your DB helper update)
        deletedTransactions = db.getDeletedTransactions();

        // --- Initialize the NEW adapter ---
        recycleBinAdapter = new RecycleBinAdapter(this, deletedTransactions);

        // --- Set the NEW adapter on the RecyclerView ---
        rvRecycleBin.setAdapter(recycleBinAdapter);

        // --- Check if the list is empty and show the correct view ---
        checkIfListIsEmpty();
    }

    /**
     * Helper method to show/hide the empty view
     */
    private void checkIfListIsEmpty() {
        if (deletedTransactions.isEmpty()) {
            // If list is empty, hide RecyclerView and show empty message
            rvRecycleBin.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            // If list has items, show RecyclerView and hide empty message
            rvRecycleBin.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}

