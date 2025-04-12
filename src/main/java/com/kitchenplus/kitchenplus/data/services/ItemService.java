package com.kitchenplus.kitchenplus.data.services;


import com.kitchenplus.kitchenplus.data.models.Item;
import com.kitchenplus.kitchenplus.data.repositories.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Optional<Item> getItemById(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        item.ifPresent(i -> {
            i.setLastTimeViewed(LocalDateTime.now());
            itemRepository.save(i);
        });
        return item;
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
}
