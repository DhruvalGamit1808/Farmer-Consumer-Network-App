package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    private EditText fullnameInput, phoneInput, addressInput, emailInput, passwordInput;
    private Button authButton, toggleMode;
    private ProgressBar progressBar;
    private boolean isLoginMode = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(AuthActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fullnameInput = findViewById(R.id.fullname_input);
        phoneInput = findViewById(R.id.phone_input);
        addressInput = findViewById(R.id.address_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        authButton = findViewById(R.id.auth_button);
        toggleMode = findViewById(R.id.toggle_mode);
        progressBar = findViewById(R.id.progressBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        authButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                loginUser(email, password);
            } else {
                registerUser();
            }
        });

        toggleMode.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;

            if (isLoginMode) {
                authButton.setText("Login");
                toggleMode.setText("Switch to Register");
                fullnameInput.setVisibility(View.GONE);
                phoneInput.setVisibility(View.GONE);
                addressInput.setVisibility(View.GONE);
            } else {
                authButton.setText("Register");
                toggleMode.setText("Switch to Login");
                fullnameInput.setVisibility(View.VISIBLE);
                phoneInput.setVisibility(View.VISIBLE);
                addressInput.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        authButton.setEnabled(false);
        toggleMode.setEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        authButton.setEnabled(true);
        toggleMode.setEnabled(true);
    }

    // 🔥 REGISTER USER (UPDATED WITH FCM TOKEN)
    private void registerUser() {

        String fullname = fullnameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (fullname.isEmpty() || phone.isEmpty() || address.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = authResult.getUser().getUid();

                    // 🔥 GET FCM TOKEN FIRST
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {

                                String token = task.isSuccessful() ? task.getResult() : "";

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("uid", uid);
                                userData.put("fullname", fullname);
                                userData.put("phone", phone);
                                userData.put("address", address);
                                userData.put("email", email);
                                userData.put("fcmToken", token); // 🔥 IMPORTANT

                                db.collection("users").document(uid)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            hideLoading();
                                            Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                                            startActivity(new Intent(AuthActivity.this, MainActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            hideLoading();
                                            Toast.makeText(this, "Error saving details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            });

                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loginUser(String email, String password) {
        showLoading();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    hideLoading();
                    startActivity(new Intent(AuthActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Invalid credentials: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}