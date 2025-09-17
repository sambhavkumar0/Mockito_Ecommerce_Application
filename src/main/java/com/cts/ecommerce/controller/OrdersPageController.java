package com.cts.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.cts.ecommerce.service.OrderService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class OrdersPageController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public String orders(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        // Principal.getName() now returns the email, but orderService expects username
        String email = principal.getName();
        // For now, we'll use the email as the identifier in order service
        model.addAttribute("orders", orderService.getOrders(email));
        return "orders";
    }
}


