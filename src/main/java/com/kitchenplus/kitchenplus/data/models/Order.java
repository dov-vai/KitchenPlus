package com.kitchenplus.kitchenplus.data.models;
import com.kitchenplus.kitchenplus.data.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dateOfPlacing;
    private double shippingCost = 0.0;
    private double sumOfOrder = 0.0;
    private double pointsApplied;
    private LocalDateTime dateOfCompleted;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines = new ArrayList<>();

    //for client association:



    public List<OrderLine> getOrderLines() {
        return orderLines;
    }
    public void setOrderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
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
