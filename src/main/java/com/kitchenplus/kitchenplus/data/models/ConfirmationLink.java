package com.kitchenplus.kitchenplus.data.models;

import java.time.LocalDateTime;

import com.kitchenplus.kitchenplus.data.enums.LinkType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ConfirmationLink {
    @Enumerated(EnumType.STRING)
    LinkType type;

    @Id
    String addressFragment;

    LocalDateTime validUntil;

    @ManyToOne
    private User user;

    public ConfirmationLink() {
    }

    public ConfirmationLink(LinkType type, String addressFragment, LocalDateTime validUntil, User user) {
        this.type = type;
        this.addressFragment = addressFragment;
        this.validUntil = validUntil;
        this.user = user;
    }

    public LinkType getType() {
        return type;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public User getUser() {
        return user;
    }

    public String getFullAddress() {
        // Would be better to use a config value, but for now, hardcoded
        return "http://localhost:8080/confirm/" + addressFragment;
    }
}
