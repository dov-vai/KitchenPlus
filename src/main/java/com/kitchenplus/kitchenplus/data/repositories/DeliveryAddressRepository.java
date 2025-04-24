package com.kitchenplus.kitchenplus.data.repositories;

import com.kitchenplus.kitchenplus.data.models.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
}