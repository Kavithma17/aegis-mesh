package com.aegismesh.order.service;
import com.aegismesh.order.dto.CreateOrderRequest;
import com.aegismesh.order.dto.OrderItemRequest;
import com.aegismesh.order.dto.OrderItemResponse;
import com.aegismesh.order.dto.OrderResponse;
import com.aegismesh.order.entity.Order;
import com.aegismesh.order.entity.OrderItem;
import com.aegismesh.order.entity.OrderStatus;
import com.aegismesh.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        BigDecimal totalAmount = calculateTotal(request.items());

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customerId(request.customerId())
                .deliveryAddress(request.deliveryAddress())
                .status(OrderStatus.CREATED)
                .totalAmount(totalAmount)
                .build();

        for (OrderItemRequest itemRequest : request.items()) {

            OrderItem orderItem = OrderItem.builder()
                    .sku(itemRequest.sku())
                    .productName(itemRequest.productName())
                    .quantity(itemRequest.quantity())
                    .unitPrice(
                            itemRequest.unitPrice()
                                    .setScale(2, RoundingMode.HALF_UP)
                    )
                    .build();

            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found: " + orderId
                ));

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found: " + orderNumber
                ));

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found: " + orderId
                ));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return toResponse(order);
        }

        if (order.getStatus() == OrderStatus.INVENTORY_RESERVED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order with reserved inventory cannot be cancelled yet"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);

        return toResponse(savedOrder);
    }

    private BigDecimal calculateTotal(List<OrderItemRequest> items) {

        return items.stream()
                .map(item ->
                        item.unitPrice()
                                .multiply(
                                        BigDecimal.valueOf(item.quantity())
                                )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String generateOrderNumber() {

        String orderNumber;

        do {
            orderNumber =
                    "AEG-" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 8)
                            .toUpperCase();

        } while (orderRepository.existsByOrderNumber(orderNumber));

        return orderNumber;
    }

    private OrderResponse toResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {

        return new OrderItemResponse(
                item.getId(),
                item.getSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}