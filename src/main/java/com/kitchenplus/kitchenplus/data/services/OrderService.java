package com.kitchenplus.kitchenplus.data.services;

import com.kitchenplus.kitchenplus.data.enums.OrderStatus;
import com.kitchenplus.kitchenplus.data.models.Item;
import com.kitchenplus.kitchenplus.data.models.Order;
import com.kitchenplus.kitchenplus.data.models.OrderLine;
import com.kitchenplus.kitchenplus.data.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    //fix:
    public List<Order> getOrdersList(){

        return orderRepository.findAll();
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
        return orderRepository.findAll();
    }
    public void insertOrder(Order order){
        double whole_sum = 0;
        order.setDateOfPlacing(LocalDateTime.now());
        if(order.getStatus() == null){
            order.setStatus(OrderStatus.IN_PROGRESS);
        }
        order.setDateOfCompleted(null);
        if(order.getOrderLines() != null){
            for(OrderLine orderLine : order.getOrderLines()){
                orderLine.setOrder(order);
                Item item = orderLine.getItem();
                int count = orderLine.getCount();
                double price = item.getPrice() * item.getMultiplier();
                orderLine.setUnitPrice(price);
                whole_sum += price * count;
            }

        }
        //points applied:
        order.setSumOfOrder(whole_sum + order.getShippingCost());
        orderRepository.save(order);
    }
    public Order updateOrder(Order order){
        return orderRepository.save(order);
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
