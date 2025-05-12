package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.data.models.Item;
import com.kitchenplus.kitchenplus.data.services.ItemService;
import com.kitchenplus.kitchenplus.data.services.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService cartService;

    @Autowired
    private ItemService itemService;

    private final Long sessionCartId = 1L; // For demo, use session/user-specific logic later

    @GetMapping
    public String showCart(Model model) {
        model.addAttribute("cart", cartService.getCart(sessionCartId));
        return "cart";
    }

    @PostMapping("/add/{itemId}")
    public String addItem(@PathVariable Long itemId, @RequestParam(defaultValue = "1") int quantity) {
        Item item = itemService.get(itemId).orElseThrow();
        cartService.addItemToCart(sessionCartId, item, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Long itemId) {
        cartService.removeItemFromCart(sessionCartId, itemId);
        return "redirect:/cart";
    }

    @GetMapping("/confirm")
    public String showConfirmation(Model model) {
        model.addAttribute("cart", cartService.getCart(sessionCartId));
        return "orderConfirmation";
    }

}
