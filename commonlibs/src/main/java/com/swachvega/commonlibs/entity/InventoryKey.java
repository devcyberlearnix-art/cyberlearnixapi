package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class InventoryKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "product_id")
    private Long productId;
}
