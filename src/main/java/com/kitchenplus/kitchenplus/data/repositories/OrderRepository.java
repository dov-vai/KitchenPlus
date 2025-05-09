package com.kitchenplus.kitchenplus.data.repositories;

import com.kitchenplus.kitchenplus.data.models.Client;
import com.kitchenplus.kitchenplus.data.models.Order;
import com.kitchenplus.kitchenplus.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrdersByClient(Client user);
}
