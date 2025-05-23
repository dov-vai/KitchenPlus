package com.kitchenplus.kitchenplus.data.models;


import jakarta.persistence.*;

import java.util.List;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MANAGER")
public class Manager extends User {
    @ManyToMany(mappedBy = "managers")
    private List<Order> orders;

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
