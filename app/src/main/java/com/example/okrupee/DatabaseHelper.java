package com.example.okrupee;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "OkRupee.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String TABLE_TRANSACTIONS = "transactions";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "phone TEXT, " +
                "password TEXT)";
        db.execSQL(createUsersTable);

        //Customer table
        String createCustomersTable = "CREATE TABLE " + TABLE_CUSTOMERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "name TEXT, " +
                "phone TEXT, " +
                "amount TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))";
        db.execSQL(createCustomersTable);

        //Transaction table
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER, " +
                "amount REAL, " +
                "type TEXT, " +
                "remarks TEXT, " +
                "date TEXT, " +
                "is_deleted INTEGER DEFAULT 0, " +
                "FOREIGN KEY(customer_id) REFERENCES customers(id) ON DELETE CASCADE)";
        db.execSQL(createTransactionsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }

    // Add new user
    public long addUser(String username, String phone, String password) {
        if (isPhoneExists(phone)) {
            return -1; // Phone already exists
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("phone", phone);
        values.put("password", password);
        return db.insert(TABLE_USERS, null, values);
    }

    // Check if phone exists for signup
    public boolean isPhoneExists(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, "phone = ?", new String[]{phone}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // Login check
    public long checkUser(String phone, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{"id"}, "phone = ? AND password = ?", new String[]{phone, password}, null, null, null);
        if (cursor.moveToFirst()) {
            long userId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
            return userId; // return user id
        }
        cursor.close();
        return -1;
    }

    // GetUser name in header
    public Cursor getUserById(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT username, phone FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
    }

    //insetCustomer
    public long insertCustomer(long userId, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        values.put("amount", 0); // default value
        values.put("user_id", userId);

        long id = db.insert(TABLE_CUSTOMERS, null, values);
        db.close();
        return id; // returns row ID if success, -1 if error
    }


    //after logout then fetch old list
    public List<CustomerModel> getCustomersByUserId(int userId) {
        List<CustomerModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, name, phone, amount FROM customers WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)});
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String name = c.getString(c.getColumnIndexOrThrow("name"));
                String phone = c.getString(c.getColumnIndexOrThrow("phone"));
                String amount = c.getString(c.getColumnIndexOrThrow("amount"));
                CustomerModel cm = new CustomerModel(id, userId, name, phone, amount); // ctor you create
                list.add(cm);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    //Delete customer
    public void deleteCustomer(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("customers", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }


    //TransactionTable Data
    public List<TransactionModel> getTransactionsForCustomer(int customerId) {
        List<TransactionModel> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        //new
        Cursor cursor = db.rawQuery(
                "SELECT * FROM transactions WHERE customer_id = ? AND is_deleted = 0",
                new String[]{String.valueOf(customerId)}
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String remarks = cursor.getString(cursor.getColumnIndexOrThrow("remarks"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                transactions.add(new TransactionModel(id, customerId, remarks, (int) amount, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactions;
    }


    //InsertTransaction
    public long insertTransaction(int customerId, String remarks, int amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("customer_id", customerId);
        values.put("remarks", remarks);
        values.put("amount", amount);
        values.put("date", date);
        return db.insert("transactions", null, values);
    }


    //::::::::::::::::  Add update balcance in database :::::::::::::::::??

    // Update customer's total amount
    public int updateCustomerAmount(int customerId, double newAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", newAmount);
        return db.update("customers", values, "id=?", new String[]{String.valueOf(customerId)});
    }

    // Get sum of all transactions for a customer
    public double getCustomerBalance(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE customer_id=?", new String[]{String.valueOf(customerId)});
        // NEW QUERY
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE customer_id=? AND is_deleted = 0", new String[]{String.valueOf(customerId)});
        double balance = 0;
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(0);
        }
        cursor.close();
        return balance;
    }

    //profile update
    public boolean updateUser(int userId, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", name);
        values.put("phone", phone);

        int rows = db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    //delete Transaction
    public void deleteCustomerTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("transactions", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // ===================== View Report Summary ===================== //

    // Fetch all transactions for a specific USER, joining with customer name
    public List<TransactionModel> getAllTransactionsForUser(int userId) {
        List<TransactionModel> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT t.id, t.customer_id, t.amount, t.remarks, t.date, c.name " +
                "FROM " + TABLE_TRANSACTIONS + " t " +
                "JOIN " + TABLE_CUSTOMERS + " c ON t.customer_id = c.id " +
                "WHERE c.user_id = ? AND t.is_deleted = 0 " + // <-- FIXED HERE
                "ORDER BY t.id DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Read data from the cursor
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int customerId = cursor.getInt(cursor.getColumnIndexOrThrow("customer_id"));
                int amount = (int) cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));

                // Reads from "remarks" column in DB
                String remarks = cursor.getString(cursor.getColumnIndexOrThrow("remarks"));

                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                // Gets "name" from the joined customer table
                String customerName = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                // Creates the model, passing "remarks" as the "title"
                TransactionModel txn = new TransactionModel(id, customerId, remarks, amount, date);

                // --- THIS IS THE FIX ---
                // These methods add the extra data the adapter needs
                txn.setCustomerName(customerName);
                txn.setTimeAgo(getTimeAgo(date));

                if (amount > 0) {
                    txn.setType("You Got");
                } else {
                    txn.setType("You Gave");
                }

                transactions.add(txn);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return transactions;
    }

    // Helper method to convert date string to "time ago" text
    private String getTimeAgo(String dateString) {
        // For simplicity, just return dateString for now.
        // You can later convert it to "2 hours ago", "yesterday", etc.
        return dateString;
    }

    //....................................RECYCLE VIEW......................
    // Modify your delete to mark as deleted instead of permanent delete
    public void softDeleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_deleted", 1); // mark as deleted
        db.update("transactions", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Get all deleted transactions
    // In DatabaseHelper.java
    @SuppressLint("Range")
    public ArrayList<TransactionModel> getDeletedTransactions() {
        ArrayList<TransactionModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // NEW QUERY: Join transactions with customers to get customer name
        String query = "SELECT t.*, c.name FROM " + TABLE_TRANSACTIONS + " t " +
                "JOIN " + TABLE_CUSTOMERS + " c ON t.customer_id = c.id " +
                "WHERE t.is_deleted = 1";

        Cursor cursor = db.rawQuery(query, null); // Use the new query

        if (cursor.moveToFirst()) {
            do {
                TransactionModel model = new TransactionModel();

                // Read from 'transactions' table (alias 't')
                model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                model.setTitle(cursor.getString(cursor.getColumnIndex("remarks")));
                int amount = (int) cursor.getDouble(cursor.getColumnIndex("amount"));
                model.setAmount(amount);
                model.setDate(cursor.getString(cursor.getColumnIndex("date")));

                // *** NEW: Read customer name from the joined 'customers' table (alias 'c') ***
                String customerName = cursor.getString(cursor.getColumnIndex("name"));
                model.setCustomerName(customerName); // Assumes your TransactionModel has this setter

                // Set type based on amount
                if (amount > 0) {
                    model.setType("You Got");
                } else {
                    model.setType("You Gave");
                }

                list.add(model);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }



    // SMS feature

}
