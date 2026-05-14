package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class NoInternetActivity extends AppCompatActivity {
    Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_internet);

        btnRetry = findViewById(R.id.btnRetry);

        btnRetry.setOnClickListener(v -> {
            if (NetworkUtils.isConnected(NoInternetActivity.this)) {
                // ✅ If internet is back, return to main app screen
                startActivity(new Intent(NoInternetActivity.this, SplashActivity.class));
                finish();
            } else {
                // 🔁 Stay on same screen if still offline
                recreate();
            }
        });
    }
}
