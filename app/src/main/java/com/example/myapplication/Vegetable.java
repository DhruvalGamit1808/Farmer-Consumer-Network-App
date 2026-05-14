package com.example.myapplication;

public class Vegetable {
    private String id;
    private String name;
    private int price;
    private int quantity;

    // Farmer (owner) details
    private String ownerId;
    private String ownerName;
    private String ownerPhone;
    private String ownerAddress; // ✅ Added new field

    private String imageName;
    private int selectedQuantity = 1;

    public Vegetable() {}

    public Vegetable(String id, String name, int price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getSelectedQuantity() { return selectedQuantity; }
    public void setSelectedQuantity(int qty) { this.selectedQuantity = qty; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getOwnerAddress() { return ownerAddress; } // ✅ Getter
    public void setOwnerAddress(String ownerAddress) { this.ownerAddress = ownerAddress; } // ✅ Setter

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
}
