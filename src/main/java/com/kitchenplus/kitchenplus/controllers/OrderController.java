package com.kitchenplus.kitchenplus.controllers;
import com.kitchenplus.kitchenplus.data.enums.OrderStatus;
import com.kitchenplus.kitchenplus.data.models.*;
import com.kitchenplus.kitchenplus.data.repositories.ClientRepository;
import com.kitchenplus.kitchenplus.data.repositories.DeliveryAddressRepository;
import com.kitchenplus.kitchenplus.data.repositories.SessionRepository;
import com.kitchenplus.kitchenplus.data.services.*;
import com.kitchenplus.kitchenplus.utils.DistanceUtils;
import com.kitchenplus.kitchenplus.dtos.PaymentsDTO;
import com.stripe.model.Card;
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
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;
    @Autowired
    private ClientRepository clientRepository;

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
//---------------------------------------fix-----------------------------:
    @GetMapping("/client")
    public String showOrdersList(@CookieValue(value = "auth_token", required = false) String token, Model model) {
        Optional<Session> session = sessionRepository.findByToken(token);
        User user = session.get().getUser();
        Client client = (Client) user;
        List<Order> orders = client.getOrders();
        model.addAttribute("orders", orders);
        return "placedOrdersList";
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
    //---------------extend fix----------
    @GetMapping("/shipping")
    public String showShippingDetails(Model model) {
        model.addAttribute("deliveryAddress", new DeliveryAddress());
        return "shippingForm";
    }

    @GetMapping("/payment")
    public String showPayments(Model model) {
        model.addAttribute("paymentFormDTO", new PaymentsDTO());
        return "paymentsView";
    }
    /*
    @PostMapping("")
    public boolean checkPaymentValidation(Model model) {

    }
    */
    @GetMapping("/get/{id}")
    public String showOrderDetails(@CookieValue(value = "auth_token", required = false) String token,@PathVariable Long id, Model model) {
        Optional<Session> session = sessionRepository.findByToken(token);
        User user = session.get().getUser();
        Client client = (Client) user;
        Order order = orderService.getSelectedOrderInformation(id);
        DeliveryAddress address = deliveryAddressRepository.findDeliveryAddressByClient(client);
        model.addAttribute("order", order);
        model.addAttribute("deliveryAddress", address);
        model.addAttribute("orderLines", order.getOrderLines());
        return "orderForm";
    }
    //implement after address entity:------------------------

    //--------------------------------------------------calculating shipping------------------------------------------------:
    @PostMapping("/address/check")
    @ResponseBody
    public Map<String, Object> checkAddress(@RequestBody DeliveryAddress deliveryAddress, @CookieValue(value = "auth_token", required = false) String token) {
        HashMap<String, Object> result = new HashMap<>();
        try{
            Optional<Session> session = sessionRepository.findByToken(token);
            User user = session.get().getUser();
            Client client = (Client) user;
            deliveryAddress.setClient(client);
            deliveryAddressService.postAddress(deliveryAddress);
            double shippingCost = calculateShippingCost(deliveryAddress);
            result.put("success", true);
            result.put("shippingCost", shippingCost);
            return result;
        }
        catch (Exception e){
            e.printStackTrace();
            result.put("success", false);
            result.put("shippingCost", 0.0);
            return result;
        }
    }
    //-------------------------------------distance calculation---------------------
    private int[] setVertices(int V, int src){
        int[] dist = new int[V];
        Arrays.fill(dist, (int)1e8);
        dist[src] = 0;
        return dist;
    }
    private boolean checkDistance(int[]dist, int v, int u, int wt){
        return dist[u] != 1e8 && dist[u] + wt < dist[v];
    }
    private void updateShortestPath(int[] dist, int u, int v, int wt){
        dist[v] = dist[u] + wt;
    }

    private boolean checkVerticeWeights(int V, int[][] edges, int src, int[] dist){
        for (int i = 0; i < V; i++) {
            for (int[] edge : edges) {
                int u = edge[0];
                int v = edge[1];
                int wt = edge[2];
                if (checkDistance(dist, v, u, wt)) {
                    // neigiamas ciklas:
                    if (i == V - 1)
                        return false;
                    // atnaujint virsunes:
                    updateShortestPath(dist, u, v, wt);
                }
            }
        }
        return true;
    }
    public int[] checkNegativeWeights(int V, int[][] edges, int src) {
        int[] dist = setVertices(V, src);
        if (!checkVerticeWeights(V, edges, src, dist)) {
            return new int[]{-1};
        }
        return dist;
    }

    //calculate only shipping:
    public double calculateShippingCost(DeliveryAddress deliveryAddress){
        double shipping;
        DistanceUtils distanceUtils = new DistanceUtils();
        double distance_hervesine = distanceUtils.distanceHervesine(deliveryAddress.getLatitude(), deliveryAddress.getLongitude());
        int vertice_count = 2;
        int [][] edge = new int[vertice_count * (vertice_count - 1) / 2][3];
        int weight = Math.max(1, (int) Math.round(distance_hervesine));
        edge[0] = new int[]{0, 1, weight};
        int[] weights = checkNegativeWeights(vertice_count, edge, 0);
        double wt = (double) weights[1]/10;
        if (wt > 8){
            shipping = wt * 5;
        }
        else {
            shipping = wt * 3;
        }
        return shipping;
    }
    public void sumOfItem(Order order, double totalPrice, double shipping){
        order.setSumOfOrder((totalPrice + shipping) * 1.21);
    }
    @PostMapping("/place")
    public String showPlaceOrder(@CookieValue(value = "auth_token", required = false) String token, Model model) {
        Optional<Session> session = sessionRepository.findByToken(token);
        User user = session.get().getUser();
        var client = clientRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("Client not found"));
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findDeliveryAddressByClient(client);
        double shippingCost = calculateShippingCost(deliveryAddress);
        Long cartId = 1L;
        Order order = new Order();
        order.setClient(client);
        order.setDateOfPlacing(LocalDateTime.now());
        System.out.println("Shipping before adding: " + shippingCost);
        order.setShippingCost(shippingCost);
        order.setStatus(OrderStatus.IN_PROGRESS);

        // Calculate loyalty points
        // for select items in the shopping cart
        ShoppingCart shoppingCart = shoppingCartService.getCart(cartId);
        var filteredItems = shoppingCart.getItems().stream()
            .filter(item -> item.getTotalPrice() >= 10)
            .collect(Collectors.toList());
        var points = calculateLoyaltyPoints(client, filteredItems);
        client.addLoyaltyPoints(points);

        // not sure what to do with the following line as I don't see anywhere
        // where points are being used in the order according to lab 
        // order.setPointsApplied(0);

        List<OrderLine> orderLines = new ArrayList<>();
        sumOfItem(order, shoppingCart.getTotalPrice(), shippingCost);
        //order.setSumOfOrder(shoppingCart.getTotalPrice() + shippingCost);
        for (CartItem item: shoppingCart.getItems()){
            OrderLine orderLine = new OrderLine();
            orderLine.setItem(item.getItem());
            orderLine.setCount(item.getQuantity());
            orderLine.setUnitPrice(item.getItem().getPrice());
            orderLine.setOrder(order);
            orderLines.add(orderLine);
        }
        order.setOrderLines(orderLines);
        orderService.insertOrder(order);
        System.out.println("order summ: " + order.getSumOfOrder());
        model.addAttribute("order", order);
        return  "placedOrder";
    }

    /**
     * Calculates loyalty points for a client based on the items in their cart.
     * @param client
     * @param items
     */
    int calculateLoyaltyPoints(Client client, List<CartItem> items) {
        double itemPriceSum = items.stream()
            .reduce(0.0, (sum, item) -> sum + item.getTotalPrice(), Double::sum);
        double calculatedPoints = 0;

        if (itemPriceSum > 100) {
            calculatedPoints = itemPriceSum * 0.05;
        } else {
            calculatedPoints = itemPriceSum * 0.01;
        }

        if (client.getAcccountAgeInYears() > 2) {
            calculatedPoints += 1.5*itemPriceSum;
        }

        if (client.hasLoyaltyProgram()) {
            calculatedPoints += 1.5*itemPriceSum;
        }

        if (calculatedPoints > 100) {
            calculatedPoints = 100;
        }
        return (int) Math.ceil(calculatedPoints);
    }
}
