package com.cts.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(optional = true)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    private String productName;
    private int quantity;
    private double price; // price per unit at purchase time
}


