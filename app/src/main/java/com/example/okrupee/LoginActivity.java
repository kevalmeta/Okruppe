package com.example.okrupee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText phoneInput, passwordInput;
    Button loginButton;
    TextView signupLink;
    DatabaseHelper db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneInput = findViewById(R.id.editTextUsername);
        passwordInput = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        signupLink = findViewById(R.id.textSignupLink);
        db = new DatabaseHelper(this);

        loginButton.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            long userId = db.checkUser(phone, password);
            if (userId != -1) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                // ✅ Save user session
                SharedPreferences.Editor editor = getSharedPreferences("user_session", MODE_PRIVATE).edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putInt("user_id", (int) userId);
                editor.apply();
                //Go to mainActivity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        signupLink.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });

    }
}
