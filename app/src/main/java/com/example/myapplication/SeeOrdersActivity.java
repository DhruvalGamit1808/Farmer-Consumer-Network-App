package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

public class SeeOrdersActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmerLayout;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;
    private TextView noOrdersText;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_orders);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        noOrdersText = findViewById(R.id.noOrdersText);
        shimmerLayout = findViewById(R.id.shimmer_include);
        recyclerView = findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList, true);
        recyclerView.setAdapter(adapter);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this::loadOrders);
        swipeRefresh.setColorSchemeResources(R.color.ExamGreen);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        startShimmer();
        loadOrders();
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

    private void loadOrders() {
        orderList.clear();
        startShimmer();

        db.collection("orders")
                .whereEqualTo("sellerId", currentUserId)
                .whereEqualTo("status", "Pending") // ✅ only show pending orders
                .get()
                .addOnSuccessListener(snapshot -> {
                    orderList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Order order = mapOrder(doc);
                        orderList.add(order);
                    }

                    // sort by newest first (if timestamp stored later)
                    Collections.reverse(orderList);

                    adapter.notifyDataSetChanged();
                    stopShimmer();

                    if (orderList.isEmpty()) {
                        noOrdersText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        noOrdersText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    stopShimmer();
                    Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Order mapOrder(DocumentSnapshot doc) {
        Order order = new Order();
        order.setId(doc.getId());
        order.setUserId(doc.getString("buyerId"));
        order.setUserName(doc.getString("buyerName"));
        order.setUserMobile(doc.getString("buyerMobile"));
        order.setUserAddress(doc.getString("buyerAddress"));
        order.setProductName(doc.getString("productName"));
        order.setPrice(toInt(doc.get("price")));
        order.setQuantity(toInt(doc.get("quantity")));
        order.setStatus(doc.getString("status"));
        order.setSellerId(doc.getString("sellerId"));
        order.setSellerName(doc.getString("sellerName"));
        order.setSellerMobile(doc.getString("sellerMobile"));
        order.setVegetableId(doc.getString("vegetableId"));
        order.setImageName(doc.getString("imageName"));
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
