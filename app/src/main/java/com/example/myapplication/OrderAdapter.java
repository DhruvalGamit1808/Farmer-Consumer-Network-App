package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<Order> orderList;
    private boolean showSellerActions;

    public OrderAdapter(List<Order> orderList, boolean showSellerActions) {
        this.orderList = orderList;
        this.showSellerActions = showSellerActions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        Order order = orderList.get(position);

        h.productName.setText(order.getProductName());
        h.price.setText("₹" + order.getPrice());
        h.quantity.setText("Qty: " + order.getQuantity());

        // 👤 USER INFO
        if (showSellerActions) {
            h.userName.setText("Consumer: " + safe(order.getUserName()));
            h.userMobile.setText("Mobile: " + safe(order.getUserMobile()));
            h.userAddress.setText("Address: " + safe(order.getUserAddress()));
            h.userAddress.setVisibility(View.VISIBLE);
        } else {
            h.userName.setText("Farmer: " + safe(order.getSellerName()));
            h.userMobile.setText("Mobile: " + safe(order.getSellerMobile()));
            h.userAddress.setVisibility(View.GONE);
        }

        // 📅 DATE TIME
        h.orderDate.setText("Date: " + safe(order.getOrderDate()));
        h.orderTime.setText("Time: " + safe(order.getOrderTime()));

        // 🎨 STATUS
        if ("Delivered".equalsIgnoreCase(order.getStatus())) {
            h.status.setText("Delivered");
            h.status.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            h.status.setText("Pending");
            h.status.setTextColor(Color.parseColor("#FF9800"));
        }

        // 🖼 IMAGE
        Context context = h.itemView.getContext();
        String imgName = order.getImageName() != null
                ? order.getImageName()
                : order.getProductName().toLowerCase();

        int imageResId = context.getResources()
                .getIdentifier(imgName, "drawable", context.getPackageName());

        h.imageView.setImageResource(imageResId != 0 ? imageResId : R.drawable.tomato);

        // 🚀 SELLER ACTIONS
        if (showSellerActions && "Pending".equalsIgnoreCase(order.getStatus())) {

            h.startDeliveryButton.setVisibility(View.VISIBLE);
            h.markDeliveredButton.setVisibility(View.VISIBLE);

            // 🔥 START DELIVERY
            h.startDeliveryButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra("orderId", order.getId());

                context.startActivity(intent);
            });

            // ✅ MARK DELIVERED
            h.markDeliveredButton.setOnClickListener(v -> {

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Map<String, Object> data = orderToMap(order);

                db.collection("delivered")
                        .document(order.getId())
                        .set(data)
                        .addOnSuccessListener(unused -> {

                            db.collection("orders")
                                    .document(order.getId())
                                    .delete();

                            db.collection("tracking")
                                    .document(order.getId())
                                    .delete();

                            int pos = h.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                orderList.remove(pos);
                                notifyItemRemoved(pos);
                            }
                        });
            });

        } else {
            h.startDeliveryButton.setVisibility(View.GONE);
            h.markDeliveredButton.setVisibility(View.GONE);
        }

        // ❌ CANCEL
        if ("Pending".equalsIgnoreCase(order.getStatus())) {
            h.cancelButton.setVisibility(View.VISIBLE);

            h.cancelButton.setOnClickListener(v -> {
                FirebaseFirestore.getInstance()
                        .collection("orders")
                        .document(order.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            int pos = h.getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                orderList.remove(pos);
                                notifyItemRemoved(pos);
                            }
                        });
            });

        } else {
            h.cancelButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private String safe(String s) {
        return (s == null || s.isEmpty()) ? "N/A" : s;
    }

    private Map<String, Object> orderToMap(Order order) {

        Map<String, Object> map = new HashMap<>();

        map.put("buyerId", order.getUserId());
        map.put("buyerName", order.getUserName());
        map.put("buyerMobile", order.getUserMobile());
        map.put("buyerAddress", order.getUserAddress());

        map.put("sellerId", order.getSellerId());
        map.put("sellerName", order.getSellerName());
        map.put("sellerMobile", order.getSellerMobile());

        map.put("productName", order.getProductName());
        map.put("price", order.getPrice());
        map.put("quantity", order.getQuantity());
        map.put("status", "Delivered");

        map.put("vegetableId", order.getVegetableId());
        map.put("imageName", order.getImageName());
        map.put("orderDate", order.getOrderDate());
        map.put("orderTime", order.getOrderTime());

        return map;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView productName, price, quantity, status;
        TextView userName, userMobile, userAddress;
        TextView orderDate, orderTime;
        ImageView imageView;

        Button startDeliveryButton, markDeliveredButton, cancelButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.orderProductName);
            price = itemView.findViewById(R.id.orderPrice);
            quantity = itemView.findViewById(R.id.orderQuantity);
            status = itemView.findViewById(R.id.orderStatus);

            userName = itemView.findViewById(R.id.orderUserName);
            userMobile = itemView.findViewById(R.id.orderUserMobile);
            userAddress = itemView.findViewById(R.id.orderUserAddress);

            orderDate = itemView.findViewById(R.id.orderDate);
            orderTime = itemView.findViewById(R.id.orderTime);

            imageView = itemView.findViewById(R.id.orderImage);

            startDeliveryButton = itemView.findViewById(R.id.startDeliveryButton);
            markDeliveredButton = itemView.findViewById(R.id.markDeliveredButton);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}