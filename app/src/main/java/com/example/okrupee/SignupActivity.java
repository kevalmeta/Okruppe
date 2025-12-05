package com.example.okrupee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    EditText usernameInput, phoneInput, passwordInput;
    Button signupButton;
    TextView loginLink;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameInput = findViewById(R.id.editTextUsername);
        phoneInput = findViewById(R.id.editTextNumber);
        passwordInput = findViewById(R.id.editTextPassword);
        signupButton = findViewById(R.id.buttonSignup);
        loginLink = findViewById(R.id.textLoginLink);
        db = new DatabaseHelper(this);

        signupButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                long result = db.addUser(username, phone, password);
                if (result != -1) {
                    Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Phone already registered", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
