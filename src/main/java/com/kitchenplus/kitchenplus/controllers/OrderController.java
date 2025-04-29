package com.kitchenplus.kitchenplus.controllers;
import com.kitchenplus.kitchenplus.data.enums.OrderStatus;
import com.kitchenplus.kitchenplus.data.models.*;
import com.kitchenplus.kitchenplus.utils.DistanceUtils;
import com.kitchenplus.kitchenplus.data.services.DeliveryAddressService;
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
import java.util.List;
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
    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @GetMapping("/all")
    public String showClientOrdersList(Model model) {
        model.addAttribute("orders", orderService.getAllClientOrdersList());
        return "ordersList";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Order order = orderService.getSelectedOrderInformation(id);
        model.addAttribute("order", order);
        model.addAttribute("status", order.getStatus().name());
        model.addAttribute("orderLines", order.getOrderLines());
        return "orderEditForm";
    }
    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        orderService.deleteOrder(id);
        redirectAttributes.addFlashAttribute("message", "Order deleted successfully!");
        return "redirect:/orders/all";
    }
    @PostMapping("/save")
    public String updateOrder(@ModelAttribute Order order, RedirectAttributes redirectAttributes) {
        Order order_saved = orderService.updateOrder(order);
        redirectAttributes.addFlashAttribute("message", "Order edited successfully!");
        return "redirect:/orders/all";
    }
    @PostMapping("/update/{id}")
    public String updateStatus(@PathVariable Long id, Model model) {
        OrderStatus order_status = orderService.getOrderStatus(id);
        model.addAttribute("status", order_status.name());
        Order order_saved = orderService.update(id);
        //PAKEISTIIIIIII I CLIENT:
        return "redirect:/orders/all";
    }
//fixxxxxxxxxxxxxxx:
    @GetMapping("/client")
    public String showOrdersList(@RequestParam("email") String email, Model model) {
        Optional<User> user_exist = userService.getUserByEmail(email);
        if (user_exist.isPresent()) {
            User user = user_exist.get();
            model.addAttribute("user", user_exist.get());
            List<Order> orders = orderService.getOrdersList(user);
            model.addAttribute("orders", orders);
            model.addAttribute("status", OrderStatus.values());
            return "placedOrdersList";
        }
        else {
            model.addAttribute("errorMessage", "User not found.");
            return "redirect:/orders/all";
        }

    }
    @GetMapping("/checkIfCancellable/{id}")
    public String checkIfCancellable(Model model,@PathVariable Long id) {
        Order order = orderService.getSelectedOrderInformation(id);
        if (order == null) {
            model.addAttribute("message", "Order not found!");
        }
        LocalDateTime date_placed = order.getDateOfPlacing();
        LocalDateTime now_time = LocalDateTime.now();
        Duration duration = Duration.between(date_placed, now_time);
        if (duration.toDays() > 1) {
            model.addAttribute("message", "Order can't be cancelled");
            return "redirect:/orders/client";
        }
        return "redirect:/orders/client";
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

    //--------------------------------------------------calculating shipping------------------------------------------------:
    @PostMapping("/address/check")
    @ResponseBody
    public boolean checkAddress(@RequestBody DeliveryAddress deliveryAddress) {
        try{
            deliveryAddressService.postAddress(deliveryAddress);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    //calculate only shipping:
    @PostMapping("/shipping/cost")
    public double calculateShippingCost(@RequestBody DeliveryAddress deliveryAddress){
        double shipping;
        DistanceUtils distanceUtils = new DistanceUtils();
        double distance_hervesine = distanceUtils.distanceHervesine(deliveryAddress.getLatitude(), deliveryAddress.getLongitude());
        int vertice_count = 2;
        int [][] edge = new int[vertice_count * (vertice_count - 1) / 2][3];
        edge[0] = new int[]{0, 1, (int) distance_hervesine};
        int[] weights = distanceUtils.BellmanFord(vertice_count, edge, 0);
        double wt = weights[1]/1000;
        if (wt > 8){
            shipping = wt * 5;
        }
        else {
            shipping = wt * 3;
        }
        return shipping;
    }
    @PostMapping("/place")
    public String showPlaceOrder(@RequestBody Order order) {
        orderService.insertOrder(order);
        return  "placedOrder";
    }

    @GetMapping("/confirm")
    public String showConfirmation(@ModelAttribute Order order, Model model) {
        model.addAttribute("order", order);
        model.addAttribute("orderLines", order.getOrderLines());
        return "orderConfirmation";
    }


}
