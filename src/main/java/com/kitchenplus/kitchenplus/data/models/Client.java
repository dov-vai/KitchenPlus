package com.kitchenplus.kitchenplus.data.models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("CLIENT")
public class Client extends User {
    @Column(nullable = false)
    @ColumnDefault("0")
    private int loyaltyPoints = 0;

    @Column(nullable = true)
    private LocalDateTime loyaltyPointsValidUntil;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean hasLoyaltyProgram  = false;

    @Column(nullable = true)
    private LocalDateTime loyaltyProgramValidUntil = LocalDateTime.now();
}
