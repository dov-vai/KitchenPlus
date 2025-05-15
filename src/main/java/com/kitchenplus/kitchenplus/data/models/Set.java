package com.kitchenplus.kitchenplus.data.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SETS")
public class Set {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    private Client client;

    @OneToMany(mappedBy = "set", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SetItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<SetItem> getItems() {
        return items;
    }

    public void setItems(List<SetItem> items) {
        this.items = items;
    }

    public void addItem(SetItem item) {
        item.setSet(this);
        items.add(item);
    }
}
