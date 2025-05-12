package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.data.models.Plan;
import com.kitchenplus.kitchenplus.data.services.PlanService;
import com.kitchenplus.kitchenplus.data.services.SetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/plans")
public class PlanController {
    private final PlanService planService;
    private final SetService setService;

    public PlanController(PlanService planService, SetService setService) {
        this.planService = planService;
        this.setService = setService;
    }

    @GetMapping("/{setId}")
    public String getSetPlans(@PathVariable Long setId, Model model) {
        setService.get(setId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid set ID: " + setId));

        List<Plan> plans = planService.getAllBySetId(setId);

        model.addAttribute("plans", plans);
        model.addAttribute("setId", setId);
        return "/plan/plansList";
    }
}
