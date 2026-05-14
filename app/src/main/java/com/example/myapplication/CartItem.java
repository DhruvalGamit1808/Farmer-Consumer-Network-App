package com.example.myapplication;

public class CartItem {
    private Vegetable vegetable;
    private int quantity;

    public CartItem(Vegetable vegetable, int quantity) {
        this.vegetable = vegetable;
        this.quantity = quantity;
    }

    public Vegetable getVegetable() {
        return vegetable;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // ✅ Add this method
    public int getTotalPrice() {
        return vegetable.getPrice() * quantity;
    }
}
