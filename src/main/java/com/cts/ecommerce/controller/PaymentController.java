package com.cts.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {

    @GetMapping("/payment")
    public String paymentPage() {
        return "payment"; // maps to templates/payment.html
    }
}


