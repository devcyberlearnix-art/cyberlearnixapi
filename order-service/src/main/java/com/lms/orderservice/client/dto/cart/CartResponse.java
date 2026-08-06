package com.lms.orderservice.client.dto.cart;

import java.util.List;

public class CartResponse {
    private List<CartItem> items;
    private Double totalCartPrice;

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Double getTotalCartPrice() {
        return totalCartPrice;
    }

    public void setTotalCartPrice(Double totalCartPrice) {
        this.totalCartPrice = totalCartPrice;
    }
}
