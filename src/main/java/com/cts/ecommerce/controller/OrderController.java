package com.cts.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.cts.ecommerce.model.Order;
import com.cts.ecommerce.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public Order place(@AuthenticationPrincipal UserDetails ud) {
        return orderService.placeOrder(ud.getUsername()); // This is now the email
    }

    @GetMapping
    public List<Order> list(@AuthenticationPrincipal UserDetails ud) {
        return orderService.getOrders(ud.getUsername()); // This is now the email
    }

    @PostMapping("/{id}/cancel")
    public Order cancelOrder(@PathVariable Long id, @AuthenticationPrincipal UserDetails ud) {
        return orderService.cancelOrderWithValidation(id, ud.getUsername());
    }
}


