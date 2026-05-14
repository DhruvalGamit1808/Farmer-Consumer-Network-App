package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout changeAddressOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        changeAddressOption = findViewById(R.id.option_change_address);

        // 👉 Open Change Address screen
        changeAddressOption.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, ChangeAddressActivity.class));
        });
    }
}