package com.aegismesh.order.controller;

import com.aegismesh.order.dto.CreateOrderRequest;
import com.aegismesh.order.dto.OrderResponse;
import com.aegismesh.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return orderService.createOrder(request);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @PathVariable UUID orderId
    ) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/number/{orderNumber}")
    public OrderResponse getOrderByNumber(
            @PathVariable String orderNumber
    ) {
        return orderService.getOrderByNumber(orderNumber);
    }

    @PatchMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable UUID orderId
    ) {
        return orderService.cancelOrder(orderId);
    }
}