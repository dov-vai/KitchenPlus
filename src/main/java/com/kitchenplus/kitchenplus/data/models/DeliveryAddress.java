package com.kitchenplus.kitchenplus.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "address_id")
public class DeliveryAddress extends Address{

}
