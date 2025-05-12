package com.kitchenplus.kitchenplus.data.services;

import com.kitchenplus.kitchenplus.data.models.CartItem;
import com.kitchenplus.kitchenplus.data.models.Item;
import com.kitchenplus.kitchenplus.data.models.ShoppingCart;
import com.kitchenplus.kitchenplus.data.repositories.CartItemRepository;
import com.kitchenplus.kitchenplus.data.repositories.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kitchenplus.kitchenplus.data.repositories.ItemRepository;


import java.util.Optional;

@Service
public class ShoppingCartService {
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    public ShoppingCart getCart(Long cartId) {
        return shoppingCartRepository.findById(cartId).orElseGet(() -> {
            ShoppingCart newCart = new ShoppingCart();
            return shoppingCartRepository.save(newCart);
        });
    }

    public ShoppingCart addItemToCart(Long cartId, Item item, int quantity) {
        ShoppingCart cart = getCart(cartId);
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(ci -> ci.getItem().getId().equals(item.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setItem(item);
            cartItem.setQuantity(quantity);
            cart.addItem(cartItem);
        }

        return shoppingCartRepository.save(cart);
    }

    public void removeItemFromCart(Long cartId, Long itemId) {
        ShoppingCart cart = getCart(cartId);
        cart.getItems().removeIf(ci -> ci.getItem().getId().equals(itemId));
        shoppingCartRepository.save(cart);
    }
}

