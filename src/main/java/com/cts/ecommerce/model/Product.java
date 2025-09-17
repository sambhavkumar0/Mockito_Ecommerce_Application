package com.cts.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @NotBlank(message = "Product name is required")
  @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
  private String name;
  
  @NotBlank(message = "Product description is required")
  @Size(min = 10, max = 500, message = "Product description must be between 10 and 500 characters")
  private String description;
  
  @NotNull(message = "Product price is required")
  @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
  @DecimalMax(value = "999999.99", message = "Product price must be less than 1,000,000")
  private Double price;
  
  @NotNull(message = "Stock quantity is required")
  @Min(value = 0, message = "Stock quantity must be 0 or greater")
  @Max(value = 9999, message = "Stock quantity must be less than 10,000")
  private Integer stock; // available quantity
  
  private String imageUrl;
  
  @Column(nullable = false)
  private boolean active = true;

}
