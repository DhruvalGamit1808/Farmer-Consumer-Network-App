package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView tvTotal;
    Button btnPlaceOrder;
    SwipeRefreshLayout swipeRefresh;

    private FirebaseFirestore db;
    private String buyerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        NotificationHelper.createChannel(this);

        recyclerView = findViewById(R.id.cartRecyclerView);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        swipeRefresh = findViewById(R.id.swipe_refresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        buyerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadCart();

        btnPlaceOrder.setOnClickListener(v -> placeOrders());

        swipeRefresh.setOnRefreshListener(() -> {
            loadCart();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void loadCart() {
        CartAdapter adapter = new CartAdapter(CartManager.getCart(), this::updateTotal);
        recyclerView.setAdapter(adapter);
        updateTotal();
    }

    private void updateTotal() {
        tvTotal.setText("Total: ₹" + CartManager.getTotalPrice());
    }

    private void placeOrders() {

        if (CartManager.getCart().isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(buyerId).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Complete your profile first!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String buyerName = snapshot.getString("fullname");
                    String buyerPhone = snapshot.getString("phone");
                    String buyerAddress = snapshot.getString("address");

                    Double buyerLat = snapshot.getDouble("lat");
                    Double buyerLng = snapshot.getDouble("lng");

                    if (buyerLat == null || buyerLng == null) {
                        Toast.makeText(this, "Set location in Settings", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Date now = new Date();
                    String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(now);
                    String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now);

                    for (CartItem item : CartManager.getCart()) {

                        Vegetable veg = item.getVegetable();

                        Map<String, Object> order = new HashMap<>();

                        // Product
                        order.put("productName", veg.getName());
                        order.put("quantity", item.getQuantity());
                        order.put("price", veg.getPrice());
                        order.put("imageName", veg.getImageName());
                        order.put("vegetableId", veg.getId());

                        // Buyer
                        order.put("buyerId", buyerId);
                        order.put("buyerName", buyerName);
                        order.put("buyerMobile", buyerPhone);
                        order.put("buyerAddress", buyerAddress);
                        order.put("buyerLat", buyerLat);
                        order.put("buyerLng", buyerLng);

                        // Seller
                        String sellerId = veg.getOwnerId();
                        order.put("sellerId", sellerId);
                        order.put("sellerName", veg.getOwnerName());
                        order.put("sellerMobile", veg.getOwnerPhone());

                        // Status
                        order.put("status", "Pending");
                        order.put("orderDate", currentDate);
                        order.put("orderTime", currentTime);

                        // 🔥 SAVE ORDER
                        db.collection("orders").add(order)
                                .addOnSuccessListener(doc -> {

                                    String orderId = doc.getId();

                                    // Tracking
                                    Map<String, Object> tracking = new HashMap<>();
                                    tracking.put("orderId", orderId);
                                    tracking.put("lat", 0);
                                    tracking.put("lng", 0);

                                    db.collection("tracking")
                                            .document(orderId)
                                            .set(tracking);

                                    // Stock update
                                    int remainingQty = veg.getQuantity() - item.getQuantity();

                                    if (remainingQty > 0) {
                                        db.collection("vegetables")
                                                .document(veg.getId())
                                                .update("quantity", remainingQty);
                                    } else {
                                        db.collection("vegetables")
                                                .document(veg.getId())
                                                .delete();
                                    }

                                    // Notification
                                    sendNotificationToUser(
                                            db,
                                            sellerId,
                                            "New Order Received",
                                            "You got order for " + veg.getName()
                                    );
                                });
                    }

                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    CartManager.clearCart();
                    finish();
                });
    }

    private void sendNotificationToUser(FirebaseFirestore db, String userId, String title, String message) {

        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    String token = doc.getString("fcmToken");

                    if (token == null || token.isEmpty()) return;

                    Map<String, Object> notif = new HashMap<>();
                    notif.put("token", token);
                    notif.put("title", title);
                    notif.put("message", message);
                    notif.put("timestamp", System.currentTimeMillis());

                    db.collection("notifications_queue").add(notif);
                });
    }
}