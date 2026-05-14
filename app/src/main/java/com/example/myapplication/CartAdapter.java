package com.example.myapplication;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartList;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onQuantityChanged();
    }

    public CartAdapter(List<CartItem> cartList, OnCartChangeListener listener) {
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);

        holder.tvName.setText(item.getVegetable().getName());
        holder.tvPrice.setText("₹" + item.getVegetable().getPrice());

        // ✅ Show current qty
        holder.etQuantity.setText(String.valueOf(item.getQuantity()));

        // ✅ When user edits quantity manually
        holder.etQuantity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    int newQty = Integer.parseInt(s.toString());
                    item.setQuantity(newQty);
                    if (listener != null) listener.onQuantityChanged();
                }
            }
        });

        // ✅ Plus button
        holder.btnPlus.setOnClickListener(v -> {
            int qty = item.getQuantity() + 1;
            item.setQuantity(qty);
            holder.etQuantity.setText(String.valueOf(qty));
            if (listener != null) listener.onQuantityChanged();
        });

        // ✅ Minus button
        holder.btnMinus.setOnClickListener(v -> {
            int qty = item.getQuantity();
            qty--; // Decrease quantity by 1

            if (qty <= 0) {
                // Remove item from cart
                cartList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartList.size());
            } else {
                // Update quantity normally
                item.setQuantity(qty);
                holder.etQuantity.setText(String.valueOf(qty));
            }

            if (listener != null) listener.onQuantityChanged();
        });

    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        EditText etQuantity;
        Button btnPlus, btnMinus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}
