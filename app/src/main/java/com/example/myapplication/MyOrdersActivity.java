package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmerLayout;
    private OrderAdapter adapter;
    private List<Order> myOrdersList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;
    private SwipeRefreshLayout swipeRefresh;
    private TextView noOrdersText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        noOrdersText = findViewById(R.id.noOrdersText);
        shimmerLayout = findViewById(R.id.shimmer_include);
        recyclerView = findViewById(R.id.myOrdersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrderAdapter(myOrdersList, false);

        recyclerView.setAdapter(adapter);

        swipeRefresh = findViewById(R.id.swipeRefreshMyOrders);
        swipeRefresh.setColorSchemeResources(R.color.ExamGreen);
        swipeRefresh.setOnRefreshListener(this::loadMyOrders);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        startShimmer();
        loadMyOrders();
    }

    private void startShimmer() {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);
    }

    private void stopShimmer() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
    }

    private void loadMyOrders() {
        myOrdersList.clear();
        startShimmer();

        final int[] pendingOps = {0};
        final List<Order> pendingOrders = new ArrayList<>();
        final List<Order> deliveredOrders = new ArrayList<>();

        Runnable checkOrders = () -> {
            if (pendingOps[0] == 0) {
                myOrdersList.clear();
                myOrdersList.addAll(pendingOrders);   // latest first
                myOrdersList.addAll(deliveredOrders); // history
                adapter.notifyDataSetChanged();
                stopShimmer();

                noOrdersText.setVisibility(myOrdersList.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(myOrdersList.isEmpty() ? View.GONE : View.VISIBLE);
            }
        };

        // 🔹 Pending orders
        db.collection("orders")
                .whereEqualTo("buyerId", currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    pendingOps[0] += snapshot.size();
                    if (snapshot.isEmpty()) checkOrders.run();

                    for (DocumentSnapshot doc : snapshot) {
                        String sellerId = doc.getString("sellerId");

                        // Fetch farmer details
                        db.collection("users").document(sellerId)
                                .get()
                                .addOnSuccessListener(sellerDoc -> {
                                    Order order = mapOrder(doc, "Pending"); // map order first

                                    if (sellerDoc.exists()) {
                                        order.setSellerName(sellerDoc.getString("fullname"));  // overwrite consumer fallback
                                        order.setSellerMobile(sellerDoc.getString("phone"));
                                      //  order.setSellerAddress(sellerDoc.getString("address")); // optional
                                    }

                                    pendingOrders.add(0, order);
                                    pendingOps[0]--;
                                    checkOrders.run();
                                });

                    }
                })
                .addOnFailureListener(e -> {
                    checkOrders.run();
                    Toast.makeText(this, "Failed to load pending orders", Toast.LENGTH_SHORT).show();
                });

        // 🔹 Delivered orders
        db.collection("delivered")
                .whereEqualTo("buyerId", currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    pendingOps[0] += snapshot.size();
                    if (snapshot.isEmpty()) checkOrders.run();

                    for (DocumentSnapshot doc : snapshot) {
                        String sellerId = doc.getString("sellerId");

                        db.collection("users").document(sellerId)
                                .get()
                                .addOnSuccessListener(sellerDoc -> {
                                    Order order = mapOrder(doc, "Delivered");
                                    if (sellerDoc.exists()) {
                                        order.setSellerName(sellerDoc.getString("fullname"));
                                        order.setSellerMobile(sellerDoc.getString("phone"));
                                       // order.setSellerAddress(sellerDoc.getString("address")); // optional
                                    }
                                    deliveredOrders.add(order);
                                    pendingOps[0]--;
                                    checkOrders.run();
                                })
                                .addOnFailureListener(e -> {
                                    Order order = mapOrder(doc, "Delivered");
                                    deliveredOrders.add(order);
                                    pendingOps[0]--;
                                    checkOrders.run();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    checkOrders.run();
                    Toast.makeText(this, "Failed to load delivered orders", Toast.LENGTH_SHORT).show();
                });
    }

    private Order mapOrder(DocumentSnapshot doc, String defaultStatus) {
        Order order = new Order();
        order.setId(doc.getId());
        order.setUserId(doc.getString("buyerId"));
        order.setUserName(doc.getString("buyerName"));
        order.setUserMobile(doc.getString("buyerMobile"));
        order.setUserAddress(doc.getString("buyerAddress"));
        order.setProductName(doc.getString("productName"));
        order.setPrice(toInt(doc.get("price")));
        order.setQuantity(toInt(doc.get("quantity")));
        order.setStatus(doc.getString("status") != null ? doc.getString("status") : defaultStatus);
        order.setSellerId(doc.getString("sellerId"));
        order.setVegetableId(doc.getString("vegetableId"));
        order.setImageName(doc.getString("imageName"));

        // Date & time
        order.setOrderDate(doc.getString("orderDate"));
        order.setOrderTime(doc.getString("orderTime"));

        // ❌ Do NOT overwrite sellerName/sellerMobile here
        // order.setSellerName(doc.getString("sellerName"));
        // order.setSellerMobile(doc.getString("sellerMobile"));

        return order;
    }


    private int toInt(Object o) {
        if (o instanceof Long) return ((Long) o).intValue();
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof String) {
            try { return Integer.parseInt((String) o); } catch (Exception ignored) {}
        }
        return 0;
    }
}
