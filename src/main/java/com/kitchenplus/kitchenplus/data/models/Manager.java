package com.kitchenplus.kitchenplus.data.models;


import jakarta.persistence.*;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

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
