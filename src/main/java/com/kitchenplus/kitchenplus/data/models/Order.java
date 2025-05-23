package com.kitchenplus.kitchenplus.data.models;
import com.kitchenplus.kitchenplus.data.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "\"order\"")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dateOfPlacing;
    @Column(nullable = false)
    private Double shippingCost;
    @Column(nullable = false)
    private Double sumOfOrder;
    @Column(nullable = true)
    private Double pointsApplied;
    @Column(nullable = true)
    private LocalDateTime dateOfCompleted;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    @ManyToMany
    @JoinTable(
            name = "order_manager",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "manager_id")
    )
    private List<Manager> managers;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new ArrayList<>();
    public List<OrderLine> getOrderLines() {
        return orderLines;
    }
    public void setOrderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
    }
    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }

    public List<Manager> getManagers() {
        return managers;
    }
    public void setManagers(List<Manager> managers) {
        this.managers = managers;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getDateOfPlacing() {
        return dateOfPlacing;
    }
    public void setDateOfPlacing(LocalDateTime dateOfPlacing) {
        this.dateOfPlacing = dateOfPlacing;
    }
    public double getShippingCost() {
        return shippingCost;
    }
    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }
    public double getSumOfOrder() {
        return sumOfOrder;
    }
    public void setSumOfOrder(double sumOfOrder) {
        this.sumOfOrder = sumOfOrder;
    }
    public double getPointsApplied() {
        if (pointsApplied == null) {
            return 0;
        }
        return pointsApplied;
    }
    public void setPointsApplied(double pointsApplied) {
        this.pointsApplied = pointsApplied;
    }
    public LocalDateTime getDateOfCompleted() {
        return dateOfCompleted;
    }
    public void setDateOfCompleted(LocalDateTime dateOfCompleted) {
        this.dateOfCompleted = dateOfCompleted;
    }
    public OrderStatus getStatus() {
        return status;
    }
    public void setStatus(OrderStatus status) {
        this.status = status;
    }


}
