package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final List<CartItem> cart = new ArrayList<>();

    public static void addToCart(Vegetable vegetable, int quantity) {
        for (CartItem item : cart) {
            if (item.getVegetable().getId().equals(vegetable.getId())) {
                // update quantity
                cart.set(cart.indexOf(item), new CartItem(vegetable, item.getQuantity() + quantity));
                return;
            }
        }
        cart.add(new CartItem(vegetable, quantity));
    }

    public static List<CartItem> getCart() {
        return cart;
    }

    public static int getTotalPrice() {
        int total = 0;
        for (CartItem item : cart) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public static void clearCart() {
        cart.clear();
    }
}
