package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500; // 2.5 seconds
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {

            // 🟠 Step 1: Check Internet Connection First
            if (!NetworkUtils.isConnected(SplashActivity.this)) {
                // No internet → go to NoInternetActivity
                startActivity(new Intent(SplashActivity.this, NoInternetActivity.class));
                finish();
                return;
            }

            // 🟢 Step 2: Continue with normal flow
            FirebaseUser currentUser = mAuth.getCurrentUser();
            Intent intent;

            if (currentUser != null) {
                // User is logged in → go to MainActivity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // User not logged in → go to AuthActivity
                intent = new Intent(SplashActivity.this, AuthActivity.class);
            }

            startActivity(intent);
            finish();

        }, SPLASH_DELAY);
    }
}
