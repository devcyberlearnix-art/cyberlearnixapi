package com.example.admin.service;

import com.example.admin.client.AdminCartServiceClient;
import com.example.admin.client.AdminCartServiceClient.CartDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final AdminCartServiceClient cartServiceClient;

    public List<CartDTO> getAllCarts() {
        return cartServiceClient.getAllCarts();
    }

    public CartDTO getCartByUserId(String userId) {
        return cartServiceClient.getCartByUserId(userId);
    }
}
