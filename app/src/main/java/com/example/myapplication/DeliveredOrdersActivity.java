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

public class DeliveredOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> deliveredList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    private ShimmerFrameLayout shimmerLayout;
    private SwipeRefreshLayout swipeRefresh;
    private TextView noOrdersText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivered_orders);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        shimmerLayout = findViewById(R.id.shimmer_include);
        recyclerView = findViewById(R.id.deliveredRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noOrdersText = findViewById(R.id.noOrdersText);

        // ✅ true = seller view → show buyer (consumer) details
        adapter = new OrderAdapter(deliveredList, true);
        recyclerView.setAdapter(adapter);

        swipeRefresh = findViewById(R.id.swipeRefreshDelivered);
        swipeRefresh.setColorSchemeResources(R.color.ExamGreen);
        swipeRefresh.setOnRefreshListener(this::loadDeliveredOrders);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        startShimmer();
        loadDeliveredOrders();
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

    private void loadDeliveredOrders() {
        deliveredList.clear();
        startShimmer();

        db.collection("delivered")
                .whereEqualTo("sellerId", currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        Order order = mapOrder(doc);
                        deliveredList.add(0, order); // newest on top
                    }

                    adapter.notifyDataSetChanged();
                    stopShimmer();

                    noOrdersText.setVisibility(deliveredList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(deliveredList.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    stopShimmer();
                    Toast.makeText(this, "Failed to load delivered orders", Toast.LENGTH_SHORT).show();
                });
    }

    private Order mapOrder(DocumentSnapshot doc) {
        Order order = new Order();
        order.setId(doc.getId());

        // ✅ Buyer (Consumer) info
        order.setUserId(doc.getString("buyerId"));
        order.setUserName(doc.getString("buyerName"));
        order.setUserMobile(doc.getString("buyerMobile"));
        order.setUserAddress(doc.getString("buyerAddress"));

        // ✅ Farmer (Seller) info (kept but unused in view)
        order.setSellerId(doc.getString("sellerId"));
        order.setSellerName(doc.getString("sellerName"));
        order.setSellerMobile(doc.getString("sellerMobile"));

        // Product info
        order.setProductName(doc.getString("productName"));
        order.setPrice(toInt(doc.get("price")));
        order.setQuantity(toInt(doc.get("quantity")));
        order.setStatus(doc.getString("status"));
        order.setVegetableId(doc.getString("vegetableId"));
        order.setImageName(doc.getString("imageName"));

        // Delivered date & time
        order.setOrderDate(doc.getString("orderDate"));
        order.setOrderTime(doc.getString("orderTime"));

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
