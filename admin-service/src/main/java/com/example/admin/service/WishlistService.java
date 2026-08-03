package com.example.admin.service;

import com.example.admin.client.AdminWishlistServiceClient;
import com.example.admin.client.AdminWishlistServiceClient.WishlistDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final AdminWishlistServiceClient wishlistServiceClient;

    public List<WishlistDTO> getAllWishlists() {
        return wishlistServiceClient.getAllWishlists();
    }

    public WishlistDTO getWishlistByUserId(String userId) {
        return wishlistServiceClient.getWishlistByUserId(userId);
    }
}
