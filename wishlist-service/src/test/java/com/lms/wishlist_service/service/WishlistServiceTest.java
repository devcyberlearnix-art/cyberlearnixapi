package com.lms.wishlist_service.service;

import com.lms.wishlist_service.client.CourseClient;
import com.lms.wishlist_service.exception.WishlistException;
import com.lms.wishlist_service.repository.WishlistRepository;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository repository;

    @Mock
    private CourseClient courseClient;

    @InjectMocks
    private WishlistService service;

    @Test
    void addToWishlist_shouldRejectUnknownCourse() {
        Request request = Request.create(Request.HttpMethod.GET, "/courses/999", Collections.emptyMap(), null,
                StandardCharsets.UTF_8);
        when(courseClient.getCourseById("999"))
                .thenThrow(new FeignException.NotFound("not found", request, null, null));

        WishlistException exception = assertThrows(WishlistException.class,
                () -> service.addToWishlist("user-1", "999"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
