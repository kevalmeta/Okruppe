package com.example.okrupee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay to simulate splash screen effect
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check login status
                SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                boolean isLoggedIn = prefs.getBoolean("isLoggedIn", true);

                if (isLoggedIn) {
                    // User is already logged in
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    // First time login
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish(); // Close splash screen
            }
        }, 3000); // 2 seconds delay
    }
}

