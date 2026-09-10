package com.aegismesh.order.repository;

import com.aegismesh.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;



import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
//JpaRepository<Order, UUID> here we can get save() findById() findAll() delete() count()

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);
}