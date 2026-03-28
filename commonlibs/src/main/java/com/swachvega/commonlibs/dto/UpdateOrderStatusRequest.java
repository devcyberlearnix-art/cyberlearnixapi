package com.cyberlearnix.commonlibs.dto;

import com.cyberlearnix.commonlibs.entity.Order;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private Order.OrderStatus status;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}
