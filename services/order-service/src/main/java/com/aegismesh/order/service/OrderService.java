package com.aegismesh.order.service;

import com.aegismesh.order.dto.CreateOrderRequest;
import com.aegismesh.order.dto.OrderResponse;
import com.aegismesh.order.entity.Order;
import com.aegismesh.order.entity.OrderStatus;
import com.aegismesh.order.exception.OrderNotFoundException;
import com.aegismesh.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.customerId())
                .pickupAddress(request.pickupAddress())
                .deliveryAddress(request.deliveryAddress())
                .packageDescription(request.packageDescription())
                .status(OrderStatus.CREATED)
                .build();

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getPickupAddress(),
                order.getDeliveryAddress(),
                order.getPackageDescription(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
