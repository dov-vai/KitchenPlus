package com.kitchenplus.kitchenplus.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private double price;
    private int height;
    private int width;
    private int length;
    @NotNull
    @Min(value = 1, message = "Amount must be greater than 0")
    private int amount;
    private double multiplier;
    private LocalDateTime lastTimeViewed;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    public Item() {
        this.lastTimeViewed = LocalDateTime.now();
    }

    public Item(String name, String description, double price, int height, int width,
                int length, int amount) {
        this();
        this.name = name;
        this.description = description;
        this.price = price;
        this.height = height;
        this.width = width;
        this.length = length;
        this.amount = amount;
        this.multiplier = 1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public LocalDateTime getLastTimeViewed() {
        return lastTimeViewed;
    }

    public void setLastTimeViewed(LocalDateTime lastTimeViewed) {
        this.lastTimeViewed = lastTimeViewed;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public void addImage(Image image) {
        images.add(image);
        image.setItem(this);
    }

    public void removeImage(Image image) {
        images.remove(image);
        image.setItem(null);
    }

    public String getFirstImageLink() {
        if (images != null && !images.isEmpty()) {
            return images.getFirst().getLink();
        }
        return null;
    }
}