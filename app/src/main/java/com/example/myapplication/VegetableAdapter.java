package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class VegetableAdapter extends RecyclerView.Adapter<VegetableAdapter.VegViewHolder> {

    private final List<Vegetable> list;
    private final Context context;

    public VegetableAdapter(List<Vegetable> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public VegViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vegetable_item, parent, false);
        return new VegViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VegViewHolder h, int position) {
        Vegetable v = list.get(position);

        h.vegName.setText(v.getName());
        h.vegPrice.setText("₹" + v.getPrice());
        h.vegQuantity.setText("Qty: " + v.getQuantity());
        h.vegUserName.setText("Farmer: " + v.getOwnerName());
        h.vegUserMobile.setText("Mobile: " + v.getOwnerPhone());
        h.vegUserAddress.setText("Address: " + v.getOwnerAddress());

        h.selectedQty.setText("1");

        Context ctx = h.itemView.getContext();
        String imgName = v.getImageName() != null
                ? v.getImageName()
                : v.getName().toLowerCase();

        int resId = ctx.getResources()
                .getIdentifier(imgName, "drawable", ctx.getPackageName());

        h.vegImage.setImageResource(resId != 0 ? resId : R.drawable.tomato);

        String currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId != null && currentUserId.equals(v.getOwnerId())) {
            h.deleteButton.setVisibility(View.VISIBLE);
            h.buyButton.setVisibility(View.GONE);
        } else {
            h.deleteButton.setVisibility(View.GONE);
            h.buyButton.setVisibility(View.VISIBLE);
        }

        // 🔽 DECREASE QTY (minimum = 1)
        h.decreaseQty.setOnClickListener(view -> {
            int qty = Integer.parseInt(h.selectedQty.getText().toString());
            if (qty > 1) {
                h.selectedQty.setText(String.valueOf(qty - 1));
            }
        });

        // 🔼 INCREASE QTY (maximum = available stock)
        h.increaseQty.setOnClickListener(view -> {
            int qty = Integer.parseInt(h.selectedQty.getText().toString());

            if (qty < v.getQuantity()) {
                h.selectedQty.setText(String.valueOf(qty + 1));
            } else {
                Toast.makeText(context,
                        "Only " + v.getQuantity() + " available",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 🛒 BUY BUTTON
        h.buyButton.setOnClickListener(view -> {
            int selectedQty = Integer.parseInt(h.selectedQty.getText().toString());

            if (selectedQty > v.getQuantity()) {
                Toast.makeText(context,
                        "Not enough stock available",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            CartManager.addToCart(v, selectedQty);
            Toast.makeText(context,
                    selectedQty + " x " + v.getName() + " added to cart",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, CartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

        // 🗑 DELETE VEGETABLE (FARMER ONLY)
        h.deleteButton.setOnClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Vegetable")
                    .setMessage("Delete " + v.getName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("vegetables")
                                .document(v.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context,
                                            "Vegetable deleted",
                                            Toast.LENGTH_SHORT).show();
                                    list.remove(position);
                                    notifyItemRemoved(position);
                                });
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // 🔽 Default state: hidden
        h.detailsContainer.setVisibility(View.GONE);
        h.tvDetailsToggle.setText("View Seller Details ▼");

// 🔥 Toggle expand/collapse
        h.tvDetailsToggle.setOnClickListener(v1 -> {

            if (h.detailsContainer.getVisibility() == View.GONE) {
                h.detailsContainer.setVisibility(View.VISIBLE);
                h.tvDetailsToggle.setText("View Seller Details ▲");
            } else {
                h.detailsContainer.setVisibility(View.GONE);
                h.tvDetailsToggle.setText("View Seller Details ▼");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VegViewHolder extends RecyclerView.ViewHolder {

        TextView vegName, vegPrice, vegQuantity,
                vegUserName, vegUserMobile, vegUserAddress, selectedQty, tvDetailsToggle;

        Button increaseQty, decreaseQty, buyButton, deleteButton;
        ImageView vegImage;
        View detailsContainer;
        VegViewHolder(View itemView) {
            super(itemView);

            vegName = itemView.findViewById(R.id.vegName);
            vegPrice = itemView.findViewById(R.id.vegPrice);
            vegQuantity = itemView.findViewById(R.id.vegQuantity);
            vegUserName = itemView.findViewById(R.id.vegUserName);
            vegUserMobile = itemView.findViewById(R.id.vegUserMobile);
            vegUserAddress = itemView.findViewById(R.id.vegUserAddress);
            selectedQty = itemView.findViewById(R.id.selectedQty);

            increaseQty = itemView.findViewById(R.id.increaseQty);
            decreaseQty = itemView.findViewById(R.id.decreaseQty);
            buyButton = itemView.findViewById(R.id.buyButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            vegImage = itemView.findViewById(R.id.vegImage);

            tvDetailsToggle = itemView.findViewById(R.id.tvDetailsToggle);
            detailsContainer = itemView.findViewById(R.id.detailsContainer);
        }
    }
}
