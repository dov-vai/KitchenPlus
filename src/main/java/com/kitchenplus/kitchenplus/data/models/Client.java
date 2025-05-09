package com.kitchenplus.kitchenplus.data.models;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@DiscriminatorValue("CLIENT")
public class Client extends User {
    @Column(nullable = false)
    @ColumnDefault("0")
    private int loyaltyPoints = 0;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    public List<Order> getOrders() {
        return orders;
    }
    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL)
    private DeliveryAddress deliveryAddress;
    @Column(nullable = true)
    private LocalDateTime loyaltyPointsValidUntil;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean hasLoyaltyProgram  = false;

    @Column(nullable = true)
    private LocalDateTime loyaltyProgramValidUntil;
}
