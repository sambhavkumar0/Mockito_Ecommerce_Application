package com.cts.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cts.ecommerce.dto.CartItemResponse;
import com.cts.ecommerce.dto.CartResponse;
import com.cts.ecommerce.model.Cart;
import com.cts.ecommerce.model.CartItem;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalPrice", 
             expression = "java(cart.getItems().stream().mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity()).sum())")
    CartResponse toDto(Cart cart);

    @Mapping(target = "cartItemId", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "stock", source = "product.stock")
    CartItemResponse toDto(CartItem item);
}
