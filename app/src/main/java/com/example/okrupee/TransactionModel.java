package com.example.okrupee;

public class TransactionModel {

    // --- Core Database Fields ---
    private int id;                // Transaction ID (Primary Key)
    private int customerId;        // Linked customer ID (Foreign Key)
    private String title;          // Description or note (from "remarks" column)
    private int amount;            // Positive = You Got, Negative = You Gave
    private String date;           // Date of transaction

    // --- Extra UI Fields (for Report Screen) ---
    private String customerName;   // Customer's name (for Report UI)
    private String type;           // "You Gave" or "You Got" (for Report UI)
    private String timeAgo;        // Display like "2 hours ago" (for Report UI)

    // Empty constructor (Good practice, helps with new objects)
    public TransactionModel() {
    }

    // Constructor with full details (used when fetching from DB)
    public TransactionModel(int id, int customerId, String title, int amount, String date) {
        this.id = id;
        this.customerId = customerId;
        this.title = title;
        this.amount = amount;
        this.date = date;
    }

    // Constructor used when inserting new transaction (without id)
    public TransactionModel(int customerId, String title, int amount, String date) {
        this.customerId = customerId;
        this.title = title;
        this.amount = amount;
        this.date = date;
    }

    // --- Core Getters ---

    public int getId() {
        return id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getTitle() {
        return title;
    }

    public int getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    // --- Getters and Setters for Report Screen ---
    // (These are the methods your DatabaseHelper needs)

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setDate(String date) { this.date = date; }
}