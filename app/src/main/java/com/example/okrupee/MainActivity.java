package com.example.okrupee;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

// --- MainActivity now implements the adapter's listener ---
public class MainActivity extends AppCompatActivity {
    // === UI Components ===
    private RecyclerView recyclerViewCustomers;
    private CustomerAdapter customerAdapter;
    public ArrayList<CustomerModel> customersList;      // Master list (from DB)
    private ArrayList<CustomerModel> filteredList;       // Filtered list (for search/filter)

    private androidx.appcompat.widget.SearchView searchView; // For searching customers
    private LinearLayout btnFilter;                         // For filter button
    private Button btnAddCustomer;
    private TextView tvYourName, tvGetAmount, tvGiveAmount;
    private TextView editTextUsername, headerUsername, headerUserMobile, btnViewReportsHeader;
    private ImageButton btnDrawerToggle;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private View headerView;

    // === Database and Logic ===
    private DatabaseHelper db;
    private long loggedInUserId;

    // === Activity Result Launcher ===
    private ActivityResultLauncher<Intent> customerDetailLauncher;

    // === Filter State ===
    private String currentSearchQuery = "";
    private String currentFilterOption = "All"; // Default filter
    private final String[] filterOptions = {"All", "You Get", "You Gave"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        // Initialize views
        recyclerViewCustomers = findViewById(R.id.rv_customer_list);
        btnAddCustomer = findViewById(R.id.btn_add_customer);
        btnViewReportsHeader = findViewById(R.id.btn_view_reports_header);

        tvGetAmount = findViewById(R.id.tv_amount_get);
        tvGiveAmount = findViewById(R.id.tv_amount_give);
        editTextUsername = findViewById(R.id.editTextUsername);

        // --- NEW: Find Search and Filter Views ---
        searchView = findViewById(R.id.search_view_darshboard);
        searchView.setQueryHint("Search Customer");

        btnFilter = findViewById(R.id.btn_filter);

        db = new DatabaseHelper(this);

        //ViewReport Button
        btnViewReportsHeader.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewReportActivity.class);
            // --- NEW: Pass the User ID to the Report Activity ---
            intent.putExtra("USER_ID", loggedInUserId);
            startActivity(intent);
        });

        // --- MODIFIED: Simplified launcher to just reload all data ---
        customerDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Customer data was changed, reload everything
                        loadCustomersForUser((int) loggedInUserId);
                    }
                }
        );

        // Get logged in user ID
        loggedInUserId = getIntent().getLongExtra("loggedInUserId", -1);
        if (loggedInUserId == -1) {
            // Try from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
            loggedInUserId = prefs.getInt("user_id", -1);
        }

        if (loggedInUserId == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Save user id in prefs for next time
        SharedPreferences.Editor editor = getSharedPreferences("user_session", MODE_PRIVATE).edit();
        editor.putInt("user_id", (int) loggedInUserId);
        editor.apply();

        //::::::: Drawer layout with navigation :::::::
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        headerView = navigationView.getHeaderView(0);
        btnDrawerToggle = findViewById(R.id.iv_menu_icon);

        headerUsername = headerView.findViewById(R.id.editTextUsername);
        headerUserMobile = headerView.findViewById(R.id.userMobile);
        showUserDetails();

        // 1. Initialize lists
        customersList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // 2. Set adapter
        // --- MODIFIED: Pass a listener that reloads data on deletion ---
        customerAdapter = new CustomerAdapter(this, filteredList, customerDetailLauncher,
                () -> loadCustomersForUser((int) loggedInUserId)); // This lambda reloads data on delete

        recyclerViewCustomers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCustomers.setAdapter(customerAdapter);

        // 3. Load data from DB (this will also call filterCustomersList)
        loadCustomersForUser((int) loggedInUserId);
        // updateTotalCards(); // No longer needed here, loadCustomersForUser does it

        // --- NEW: Setup Listeners for Search and Filter ---
        setupSearchListener();
        setupFilterListener();

        //Add customer button
        btnAddCustomer.setOnClickListener(v -> {
            //::::::: New Logic of button ::::::////
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_add_customer);

            // Make background transparent so corners show
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            // Get dialog views
            EditText editName = dialog.findViewById(R.id.edit_customer_name);
            EditText editNumber = dialog.findViewById(R.id.edit_customer_number);
            Button btnCancel = dialog.findViewById(R.id.btn_cancel);
            Button btnSave = dialog.findViewById(R.id.btn_save);

            // Cancel button
            btnCancel.setOnClickListener(c -> dialog.dismiss());

            // Save button
            btnSave.setOnClickListener(s -> {
                String name = editName.getText().toString().trim();
                String number = editNumber.getText().toString().trim();

                if (name.isEmpty()) {
                    editName.setError("Enter customer name");
                    return;
                }
                if (number.isEmpty() || number.length() != 10) {
                    editNumber.setError("Enter valid 10-digit number");
                    return;
                }

                // Insert into DB
                long insertedId = db.insertCustomer(loggedInUserId,name,number);

                if (insertedId != -1) {
                    CustomerModel newCustomer = new CustomerModel(
                            (int) insertedId,
                            loggedInUserId,
                            name,
                            number,
                            "0" // --- NEW: Start with amount 0 ---
                    );
                    // --- MODIFIED: Add to master list, then re-filter ---
                    customersList.add(0, newCustomer);
                    filterCustomersList(); // This refreshes the UI correctly

                    recyclerViewCustomers.scrollToPosition(0); // optional, scroll to top
                    // updateTotalCards(); // Not needed, filterCustomersList can do it
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Error adding customer", Toast.LENGTH_SHORT).show();
                }
            });

            // Show dialog
            dialog.show();
        });

        //drawerbutton
        btnDrawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });

        //navigationView
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if(itemId==R.id.nav_home){
                    Toast.makeText(MainActivity.this,"Home",Toast.LENGTH_SHORT).show();
                }
                if(itemId==R.id.nav_profile){
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    //Toast.makeText(MainActivity.this,"Profile",Toast.LENGTH_SHORT).show();
                }
                if(itemId==R.id.nav_settings){
                    Toast.makeText(MainActivity.this,"Settings",Toast.LENGTH_SHORT).show();
                }
                if(itemId==R.id.nav_recycle){
                    Toast.makeText(MainActivity.this,"Recyclebin", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, RecycleBinActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                if(itemId==R.id.nav_terms){
                    Intent intent = new Intent(MainActivity.this, TermsActivity.class);
                    startActivity(intent);
                }
                if(itemId==R.id.nav_contact){
                    Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                    startActivity(intent);
                }
                if(itemId==R.id.nav_logout){
                    Logout();
                    Toast.makeText(MainActivity.this,"Logout",Toast.LENGTH_SHORT).show();
                }

                drawerLayout.closeDrawers(); // Close drawer after selection
                return true;
            }
        });
    }

    //logout button
    private void Logout() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void showUserDetails() {
        Cursor cursor = db.getUserById(loggedInUserId);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex("username"));
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex("phone"));
            editTextUsername.setText(username);
            headerUsername.setText(username);
            headerUserMobile.setText("+91 " + phone);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * MODIFIED: Loads customers AND applies filters.
     */
    private void loadCustomersForUser(int userId) {
        List<CustomerModel> dbList = db.getCustomersByUserId(userId);

        customersList.clear();
        customersList.addAll(dbList);

        // --- NEW: Call filter method instead of just copying list ---
        filterCustomersList();
        updateTotalCards(); // Update cards based on the full list
    }

    // update of total balance of card
    void updateTotalCards() {
        double totalGet = 0;
        double totalGive = 0;

        // --- IMPORTANT: Always calculate from the MASTER list ---
        for (CustomerModel c : customersList) {
            double amt = 0;
            try {
                amt = Double.parseDouble(c.getAmount());
            } catch (NumberFormatException e) {
                amt = 0;
            }

            if (amt > 0) { // Changed from >= 0 to > 0
                totalGet += amt;  // sum of positive
            } else if (amt < 0) { // Changed from else to else if
                totalGive += Math.abs(amt); // sum of negative
            }
        }

        tvGetAmount.setText("₹" + totalGet);
        tvGiveAmount.setText("₹" + totalGive);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUserDetails();   // reload user info
        // --- Reload data in case it changed ---
        loadCustomersForUser((int) loggedInUserId);
    }

    // =================================================================
    // --- NEW METHODS FOR SEARCH AND FILTER ---
    // =================================================================

    /**
     * Sets up the listener for the search bar.
     */
    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // User typed, update the query and re-filter
                currentSearchQuery = newText;
                filterCustomersList();
                return true;
            }
        });
    }

    /**
     * Sets up the click listener for the filter button.
     * This will show a simple dialog to choose a filter.
     */
    private void setupFilterListener() {
        btnFilter.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Sort By");

            // Find the index of the currently selected option
            int checkedItem = 0; // Default to "All"
            for (int i = 0; i < filterOptions.length; i++) {
                if (filterOptions[i].equals(currentFilterOption)) {
                    checkedItem = i;
                    break;
                }
            }

            builder.setSingleChoiceItems(filterOptions, checkedItem, (dialog, which) -> {
                // User selected a new filter
                currentFilterOption = filterOptions[which];
                filterCustomersList();
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    /**
     * This is the main filter function. It repopulates the
     * filteredList based on the master customersList.
     */
    private void filterCustomersList() {
        filteredList.clear();
        String query = currentSearchQuery.toLowerCase().trim();

        // Loop through the MASTER list
        for (CustomerModel customer : customersList) {

            // --- 1. Check Search Filter ---
            boolean matchesQuery = false;
            if (query.isEmpty()) {
                matchesQuery = true; // Show all if query is empty
            } else if (customer.getName().toLowerCase().contains(query)) {
                matchesQuery = true; // Check name
            } else if (customer.getPhone().contains(query)) {
                matchesQuery = true; // Check phone number
            }

            if (!matchesQuery) {
                continue; // Skip this customer, it doesn't match the search
            }

            // --- 2. Check "All", "You Get", "You Gave" Filter ---
            if (currentFilterOption.equals("All")) {
                filteredList.add(customer);
                continue; // Add customer and move to the next one
            }

            // Parse the amount string to check its value
            double amount = 0;
            try {
                // customer.getAmount() is a String (e.g., "-500" or "1200")
                amount = Double.parseDouble(customer.getAmount());
            } catch (NumberFormatException e) {
                amount = 0; // Treat invalid or empty amounts as 0
            }

            if (currentFilterOption.equals("You Get") && amount > 0) {
                filteredList.add(customer);
            } else if (currentFilterOption.equals("You Gave") && amount < 0) {
                filteredList.add(customer);
            }
        }

        // --- 3. Update the Adapter ---
        customerAdapter.notifyDataSetChanged();
    }
}