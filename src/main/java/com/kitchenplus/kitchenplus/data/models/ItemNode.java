package com.kitchenplus.kitchenplus.data.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("ITEM")
public class ItemNode extends Node {
    @ManyToOne
    private SetItem item;

    private int angle;

    public SetItem getItem() {
        return item;
    }

    public void setItem(SetItem item) {
        this.item = item;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }
}
