package com.kitchenplus.kitchenplus.data.repositories;
import com.kitchenplus.kitchenplus.data.models.Item;
import com.kitchenplus.kitchenplus.data.models.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
}
