package com.kitchenplus.kitchenplus.data.repositories;

import com.kitchenplus.kitchenplus.data.models.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
}
