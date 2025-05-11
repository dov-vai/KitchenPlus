package com.kitchenplus.kitchenplus.data.services;

import com.kitchenplus.kitchenplus.data.enums.OrderStatus;
import com.kitchenplus.kitchenplus.data.models.*;
import com.kitchenplus.kitchenplus.data.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    public List<Order> getOrdersList(Client user) {
        List<Order> orders_list = orderRepository.findOrdersByClient(user);
        return orders_list;
    }
    public LocalDateTime getDateOfOrderPlacing(Long order_id){
        Order order = orderRepository.findById(order_id).orElse(null);
        if(order != null){
            return order.getDateOfPlacing();
        }
        return null;
    }
    public  Order getSelectedOrderInformation(Long orderId){
        return orderRepository.findById(orderId).orElse(null);
    }
    public List<Order>getAllClientOrdersList(){
        List<Order> ordersList = orderRepository.findAll();
        System.out.println("Orders found: " + ordersList);
        return orderRepository.findAll();
    }
    public void insertOrder(Order order){
        order.setDateOfCompleted(null);
        orderRepository.save(order);
    }
    public Order updateOrder(Order order){
        Order order_exist = orderRepository.findById(order.getId()).orElseThrow(()->new RuntimeException("Order not found"));
        order_exist.setDateOfPlacing(order.getDateOfPlacing());
        order_exist.setDateOfCompleted(order.getDateOfCompleted());
        order_exist.setStatus(order.getStatus());
        order_exist.setPointsApplied(order.getPointsApplied());
        order_exist.setShippingCost(order.getShippingCost());
        order_exist.setSumOfOrder(order.getSumOfOrder());
        return orderRepository.save(order_exist);
    }
    public Order update(Long order_id){
        Order order_exist = getSelectedOrderInformation(order_id);
        order_exist.setStatus(OrderStatus.CANCELED);
        return orderRepository.save(order_exist);
    }
    public Order getClientOrderDetails(Long client_id, Long order_id){
        Order order = orderRepository.findById(order_id).orElse(null);
        return order;

    }
    public void deleteOrder(Long orderId){
        orderRepository.deleteById(orderId);
    }
    public OrderStatus getOrderStatus(Long orderId){
        Order order = orderRepository.findById(orderId).orElse(null);
        if(order != null){
            return order.getStatus();
        }
        return null;
    }

}
