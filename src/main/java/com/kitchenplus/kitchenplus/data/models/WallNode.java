package com.kitchenplus.kitchenplus.data.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
@DiscriminatorValue("WALL")
public class WallNode extends Node {
    @OneToOne
    private WallNode next;

    public WallNode getNext() {
        return next;
    }

    public void setNext(WallNode next) {
        this.next = next;
    }
}
