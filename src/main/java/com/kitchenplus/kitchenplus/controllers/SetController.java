package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.data.models.Set;
import com.kitchenplus.kitchenplus.data.models.SetItem;
import com.kitchenplus.kitchenplus.data.services.ItemService;
import com.kitchenplus.kitchenplus.data.services.SetService;
import com.kitchenplus.kitchenplus.dtos.SetCreationDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sets")
public class SetController {

    private final SetService setService;
    private final ItemService itemService;

    public SetController(SetService setService, ItemService itemService) {
        this.setService = setService;
        this.itemService = itemService;
    }

    @GetMapping
    public String getAllSets(Model model) {
        model.addAttribute("sets", setService.getAll());
        return "set/setsList";
    }

    @GetMapping("/{id}")
    public String getSet(@PathVariable Long id, Model model) {
        Set set = setService.get(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid set ID"));

        double totalPrice = 0.0;
        if (set.getItems() != null) {
            totalPrice = set.getItems().stream()
                    .mapToDouble(setItem -> setItem.getItem().getPrice())
                    .sum();
        }

        model.addAttribute("set", set);
        model.addAttribute("totalPrice", totalPrice);
        return "set/viewSet";
    }

    @GetMapping("/new")
    public String createSet(@ModelAttribute("setCreationForm") SetCreationDto setCreationDto, Model model) {
        model.addAttribute("items", itemService.findAll());
        return "set/createSet";
    }

    @PostMapping("/new")
    public String saveSet(@Valid @ModelAttribute("setCreationForm") SetCreationDto setCreationDto,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("items", itemService.findAll());
            return "set/createSet";
        }

        Set set = new Set();
        set.setTitle(setCreationDto.title());

        for (Long itemId : setCreationDto.selectedItems()) {
            itemService.get(itemId).ifPresent(item -> {
                SetItem setItem = new SetItem();
                setItem.setItem(item);
                set.addItem(setItem);
            });
        }

        Set savedSet = setService.insert(set);
        redirectAttributes.addFlashAttribute("successMessage", "Set created successfully!");

        return "redirect:/sets/" + savedSet.getId();
    }
}