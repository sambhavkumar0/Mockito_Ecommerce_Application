package com.cts.ecommerce.model;

public enum OrderStatus {
    PLACED("Placed"),
    CANCELLED("Cancelled"),
    DELIVERED("Delivered");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
