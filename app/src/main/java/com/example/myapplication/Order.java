package com.example.myapplication;

public class Order {
    private String id;

    // 🔹 Buyer Info
    private String buyerId;
    private String buyerName;
    private String buyerMobile;
    private String buyerAddress;

    // 🔹 App aliases
    private String userId;
    private String userName;
    private String userMobile;
    private String userAddress;

    // 🔹 Product Info
    private String productName;
    private int price;
    private int quantity;
    private String status;

    // 🔹 Seller Info
    private String sellerId;
    private String sellerName;
    private String sellerMobile;

    private String farmerId; // old support
    private String vegetableId;
    private String imageName;

    // 🔹 Date Time
    private String orderDate;
    private String orderTime;

    // ✅ NEW: DELIVERY TRACKING FIELDS
    private boolean isOnDelivery;   // true when delivery started
    private double currentLat;     // delivery person current location
    private double currentLng;

    private double buyerLat;       // destination location
    private double buyerLng;

    public Order() {}

    // ---------------- EXISTING GETTERS ----------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId != null ? userId : buyerId; }
    public void setUserId(String userId) { this.userId = userId; this.buyerId = userId; }

    public String getUserName() { return userName != null ? userName : buyerName; }
    public void setUserName(String userName) { this.userName = userName; this.buyerName = userName; }

    public String getUserMobile() { return userMobile != null ? userMobile : buyerMobile; }
    public void setUserMobile(String userMobile) { this.userMobile = userMobile; this.buyerMobile = userMobile; }

    public String getUserAddress() { return userAddress != null ? userAddress : buyerAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; this.buyerAddress = userAddress; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerMobile() { return sellerMobile; }
    public void setSellerMobile(String sellerMobile) { this.sellerMobile = sellerMobile; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getVegetableId() { return vegetableId; }
    public void setVegetableId(String vegetableId) { this.vegetableId = vegetableId; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }

    // ---------------- NEW DELIVERY GETTERS ----------------

    public boolean isOnDelivery() { return isOnDelivery; }
    public void setOnDelivery(boolean onDelivery) { isOnDelivery = onDelivery; }

    public double getCurrentLat() { return currentLat; }
    public void setCurrentLat(double currentLat) { this.currentLat = currentLat; }

    public double getCurrentLng() { return currentLng; }
    public void setCurrentLng(double currentLng) { this.currentLng = currentLng; }

    public double getBuyerLat() { return buyerLat; }
    public void setBuyerLat(double buyerLat) { this.buyerLat = buyerLat; }

    public double getBuyerLng() { return buyerLng; }
    public void setBuyerLng(double buyerLng) { this.buyerLng = buyerLng; }
}