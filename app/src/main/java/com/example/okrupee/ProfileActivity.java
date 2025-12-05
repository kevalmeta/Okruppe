package com.example.okrupee;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    EditText etName, etPhone;
    Button btnSave;
    ImageButton btnBack;
    DatabaseHelper db;
    long loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);

        // Get logged in user id from prefs
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        loggedInUserId = prefs.getInt("user_id", -1);

        etName = findViewById(R.id.et_profile_name);
        etPhone = findViewById(R.id.et_profile_phone);
        btnSave = findViewById(R.id.btn_profile_save);
        btnBack = findViewById(R.id.btn_profile_back);

        // Load user info
        loadUserDetails();

        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Save changes
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Enter name");
                return;
            }
            if (phone.isEmpty() || phone.length() != 10) {
                etPhone.setError("Enter valid 10-digit number");
                return;
            }

            boolean updated = db.updateUser((int) loggedInUserId, name, phone);
            if (updated) {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserDetails() {
        Cursor cursor = db.getUserById(loggedInUserId);
        if (cursor != null && cursor.moveToFirst()) {
            etName.setText(cursor.getString(0));
            etPhone.setText(cursor.getString(1));
            cursor.close();
        }
    }
}
