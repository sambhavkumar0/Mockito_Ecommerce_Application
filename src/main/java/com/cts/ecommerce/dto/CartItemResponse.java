package com.cts.ecommerce.dto;

import lombok.*;


@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long cartItemId;   // <-- CartItem.id
    private Long productId;    // Product.id
    private String productName;
    private int quantity;
    private double price;      // price of single product
    private int stock;         // available stock
}

