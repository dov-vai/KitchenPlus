package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.data.models.Client;
import com.kitchenplus.kitchenplus.data.models.Set;
import com.kitchenplus.kitchenplus.data.models.SetItem;
import com.kitchenplus.kitchenplus.data.services.ItemService;
import com.kitchenplus.kitchenplus.data.services.SetService;
import com.kitchenplus.kitchenplus.data.services.UserService;
import com.kitchenplus.kitchenplus.dtos.SetCreationDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sets")
public class SetController {
    private final SetService setService;
    private final ItemService itemService;
    private final UserService userService;

    public SetController(SetService setService, ItemService itemService, UserService userService) {
        this.setService = setService;
        this.itemService = itemService;
        this.userService = userService;
    }

    @GetMapping
    public String showSetsList(Model model) {
        if (userService.getAuthUserAs(Client.class).isEmpty()){
            return "redirect:/login";
        }

        model.addAttribute("sets", setService.getAll());
        return "set/setsList";
    }

    @GetMapping("/{id}")
    public String showSetDetails(@PathVariable Long id, Model model) {
        if (userService.getAuthUserAs(Client.class).isEmpty()){
            return "redirect:/login";
        }

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
        return "set/setDetails";
    }

    @GetMapping("/new")
    public String showSetCreateForm(@ModelAttribute("setCreationForm") SetCreationDto setCreationDto, Model model) {
        if (userService.getAuthUserAs(Client.class).isEmpty()){
            return "redirect:/login";
        }

        model.addAttribute("items", itemService.findAll());
        return "set/setCreateForm";
    }

    @PostMapping("/new")
    public String addNewSet(@Valid @ModelAttribute("setCreationForm") SetCreationDto setCreationDto,
                            BindingResult bindingResult,
                            Model model) {
        var client = userService.getAuthUserAs(Client.class);

        if (client.isEmpty()){
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("items", itemService.findAll());
            return "set/setCreateForm";
        }

        Set set = new Set();
        set.setTitle(setCreationDto.title());
        set.setClient(client.get());

        for (Long itemId : setCreationDto.selectedItems()) {
            itemService.get(itemId).ifPresent(item -> {
                SetItem setItem = new SetItem();
                setItem.setItem(item);
                set.addItem(setItem);
            });
        }

        Set savedSet = setService.save(set);
        return "redirect:/sets/" + savedSet.getId();
    }
}