package com.kitchenplus.kitchenplus.controllers;
import com.kitchenplus.kitchenplus.data.enums.OrderStatus;
import com.kitchenplus.kitchenplus.data.models.*;

import com.kitchenplus.kitchenplus.data.services.OrderLineService;
import com.kitchenplus.kitchenplus.data.services.OrderService;
import com.kitchenplus.kitchenplus.data.services.UserService;
import com.kitchenplus.kitchenplus.dtos.PaymentsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.stripe.model.PaymentMethod;
import com.stripe.exception.StripeException;
import com.stripe.Stripe;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderLineService orderLineService;

    @Autowired
    private UserService userService;


    @GetMapping("/all")
    public String showClientOrdersList(Model model) {
        model.addAttribute("orders", orderService.getAllClientOrdersList());
        return "ordersList";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Order order = orderService.getSelectedOrderInformation(id);
        model.addAttribute("order", order);
        model.addAttribute("orderLines", order.getOrderLines());
        return "orderEditForm";
    }
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        orderService.deleteOrder(id);
        redirectAttributes.addFlashAttribute("message", "Order deleted successfully!");
        return "redirect:/orders";
    }
    @PostMapping("/save")
    public String updateOrder(@ModelAttribute Order order, RedirectAttributes redirectAttributes) {
        Order order_saved = orderService.updateOrder(order);
        redirectAttributes.addFlashAttribute("message", "Order edited successfully!");
        return "redirect:/orders/all";
    }

    @GetMapping("/client")
    public String showOrdersList(@RequestParam("clientId") String email, Model model) {
        Optional<User> userOpt = userService.getUserByEmail(email);
        User user = userOpt.get();
        return "orderList";
    }
    @GetMapping("/checkIfCancellable/{orderId}")
    public String checkIfCancellable(Model model, Long order_id) {
        Order order = orderService.getSelectedOrderInformation(order_id);
        if (order == null) {
            model.addAttribute("message", "Order not found!");
        }
        LocalDateTime date_placed = order.getDateOfPlacing();
        LocalDateTime now_time = LocalDateTime.now();
        Duration duration = Duration.between(date_placed, now_time);
        if (duration.toDays() > 1) {
            model.addAttribute("message", "Order can't be cancelled");
        }
        OrderStatus status = order.getStatus();
        if (status != OrderStatus.COMPLETED){
            return "cancelForm";
        }
        else {
            return "ordersList";
        }

    }
    @GetMapping("/shipping")
    public String showShippingDetails() {
        return "shippingForm";
    }

    @GetMapping("/payment")
    public String showPaymentsForm(Model model) {
        model.addAttribute("paymentFormDTO", new PaymentsDTO());
        return "paymentsView";
    }
    /*
    @PostMapping("")
    public boolean checkPaymentValidation(Model model) {

    }
    */
    @GetMapping("/get/{id}")
    public String showOrderDetails(@PathVariable Long id, Model model) {
        Order order = orderService.getSelectedOrderInformation(id);
        model.addAttribute("order", order);
        model.addAttribute("orderLines", order.getOrderLines());
        return "orderForm";
    }
    //implement after address entity:------------------------
}
