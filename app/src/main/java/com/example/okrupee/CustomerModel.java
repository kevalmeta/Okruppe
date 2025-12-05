package com.example.okrupee;

public class CustomerModel {
    private int id;
    private long userId;
    private String name;
    private String phone;
    private String amount; // Store as String if UI displays it directly

    // Constructor for new customer with default amount
    public CustomerModel(int id, long userId, String name, String phone) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.amount = "0"; // default amount
    }

    // Constructor for when fetching from DB with amount
    public CustomerModel(int id, long userId, String name, String phone, String amount) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.amount = amount;
    }

    // Getters
    public int getId() { return id; }
    public long getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAmount() { return amount; }

    public void setAmount(String amount) {
        this.amount = amount;
    }

}


