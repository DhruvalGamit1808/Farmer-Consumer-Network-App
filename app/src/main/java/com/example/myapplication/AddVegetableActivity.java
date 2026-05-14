package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddVegetableActivity extends AppCompatActivity {

    private Spinner vegetableSpinner;
    private EditText priceEditText, quantityEditText;
    private Button uploadButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vegetable);

        vegetableSpinner = findViewById(R.id.vegetable_spinner);
        priceEditText = findViewById(R.id.price_edit_text);
        quantityEditText = findViewById(R.id.quantity_edit_text);
        uploadButton = findViewById(R.id.upload_button);
        backButton = findViewById(R.id.back_button);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        backButton.setOnClickListener(v -> finish());

        String[] vegetables = {"Potato", "Tomato", "Onion", "Carrot", "Spinach"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                vegetables
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vegetableSpinner.setAdapter(adapter);

        uploadButton.setOnClickListener(v -> uploadVegetable());
    }

    private void uploadVegetable() {

        String name = vegetableSpinner.getSelectedItem().toString();
        String priceStr = priceEditText.getText().toString().trim();
        String quantityStr = quantityEditText.getText().toString().trim();

        if (TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(quantityStr)) {
            Toast.makeText(this, "Please enter price and quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int price, quantity;
        try {
            price = Integer.parseInt(priceStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Price and quantity must be numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Prevent crash if user is not logged in
        String uid = (auth.getCurrentUser() != null)
                ? auth.getCurrentUser().getUid()
                : "test-farmer";

        db.collection("users").document(uid).get()
                .addOnSuccessListener((DocumentSnapshot user) -> {

                    if (!user.exists()) {
                        Toast.makeText(this,
                                "Profile not found. Please complete your profile.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String ownerName = user.getString("fullname");
                    String ownerPhone = user.getString("phone");
                    String ownerAddress = user.getString("address");

                    Map<String, Object> veg = new HashMap<>();
                    veg.put("name", name);
                    veg.put("price", price);
                    veg.put("quantity", quantity);
                    veg.put("imageName", name.toLowerCase());

                    veg.put("farmerId", uid);
                    veg.put("farmerName", ownerName);
                    veg.put("farmerPhone", ownerPhone);
                    veg.put("farmerAddress", ownerAddress);

                    // ✅ One vegetable per farmer (no duplicates)
                    String docId = uid + "_" + name.toLowerCase();

                    db.collection("vegetables").document(docId).set(veg)
                            .addOnSuccessListener(doc -> {
                                Toast.makeText(this,
                                        "Vegetable added / updated successfully",
                                        Toast.LENGTH_SHORT).show();
                                priceEditText.setText("");
                                quantityEditText.setText("");
                                vegetableSpinner.setSelection(0);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load profile",
                                Toast.LENGTH_SHORT).show()
                );
    }
}
