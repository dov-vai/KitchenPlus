package com.kitchenplus.kitchenplus.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "address_id")
public class DeliveryAddress extends Address{
    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;
    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }
}
