package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText editName, editMobile, editEmail, editAdd;
    private Button btnUpdateProfile;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editName   = findViewById(R.id.editName);
        editMobile = findViewById(R.id.editMobile);
        editEmail  = findViewById(R.id.editEmail);
        editAdd    = findViewById(R.id.address_input);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        progressBar = findViewById(R.id.progressBar);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());


        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId != null) {
            fetchUserProfile();
        }

        btnUpdateProfile.setOnClickListener(v -> updateUserProfile());
    }

    // ✅ Show loading
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnUpdateProfile.setEnabled(false);
    }

    // ✅ Hide loading
    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnUpdateProfile.setEnabled(true);
    }

    // ✅ Fetch user profile from Firestore
    private void fetchUserProfile() {
        showLoading();
        DocumentReference userRef = db.collection("users").document(currentUserId);
        userRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        editName.setText(snapshot.getString("fullname"));
                        editMobile.setText(snapshot.getString("phone"));
                        editEmail.setText(snapshot.getString("email"));
                        editAdd.setText(snapshot.getString("address"));
                    }
                    hideLoading();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ Update user profile in Firestore
    private void updateUserProfile() {
        String name = editName.getText().toString().trim();
        String mobile = editMobile.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String address = editAdd.getText().toString().trim();

        if (name.isEmpty() || mobile.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();

        Map<String, Object> profile = new HashMap<>();
        profile.put("fullname", name);
        profile.put("phone", mobile);
        profile.put("email", email);
        profile.put("address", address);

        db.collection("users").document(currentUserId)
                .update(profile) // ✅ only updates these fields, keeps uid
                .addOnSuccessListener(aVoid -> {
                    hideLoading();
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }
}
