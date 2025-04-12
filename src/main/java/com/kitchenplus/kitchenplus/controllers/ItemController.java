package com.kitchenplus.kitchenplus.controllers;


import com.kitchenplus.kitchenplus.data.models.Image;
import com.kitchenplus.kitchenplus.data.models.Item;
import com.kitchenplus.kitchenplus.data.services.ImageService;
import com.kitchenplus.kitchenplus.data.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @Autowired
    private ImageService imageService;

    @GetMapping
    public String listItems(Model model) {
        model.addAttribute("items", itemService.getAllItems());
        return "itemList";
    }

    @GetMapping("/new")
    public String showItemForm(Model model) {
        model.addAttribute("item", new Item());
        model.addAttribute("image", new Image());
        return "itemForm";
    }

    @PostMapping("/save")
    public String saveItem(@ModelAttribute Item item,
                           @RequestParam(value = "imageUrls", required = false) List<String> imageUrls,
                           RedirectAttributes redirectAttributes) {
        Item savedItem = itemService.saveItem(item);

        savedItem.getImages().clear();

        if (imageUrls != null) {
            for (String url : imageUrls) {
                if (url != null && !url.trim().isEmpty()) {
                    Image image = new Image(url.trim(), savedItem);
                    imageService.saveImage(image);
                }
            }
        }

        redirectAttributes.addFlashAttribute("message", "Item saved successfully!");
        return "redirect:/items";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item ID"));

        List<String> imageUrls = item.getImages().stream()
                .map(Image::getLink)
                .collect(Collectors.toList());

        model.addAttribute("item", item);
        model.addAttribute("imageUrls", imageUrls);
        return "itemForm";
    }

    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        itemService.deleteItem(id);
        redirectAttributes.addFlashAttribute("message", "Item deleted successfully!");
        return "redirect:/items";
    }
}