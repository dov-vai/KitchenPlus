package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.data.models.Node;
import com.kitchenplus.kitchenplus.data.models.Plan;
import com.kitchenplus.kitchenplus.data.models.WallNode;
import com.kitchenplus.kitchenplus.data.services.PlanService;
import com.kitchenplus.kitchenplus.data.services.SetService;
import com.kitchenplus.kitchenplus.dtos.ContourDataDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/new/{setId}")
    public String createPlan(@PathVariable Long setId, Model model) {
        model.addAttribute("setId", setId);
        return "plan/createPlan";
    }

    @PostMapping("/new/{setId}")
    @ResponseBody
    public Map<String, Object> createPlan(@PathVariable Long setId, @RequestBody ContourDataDto contourDataDto) {
        var set = setService.get(setId).orElseThrow(() -> new IllegalArgumentException("Invalid set ID: " + setId));

        Plan plan = new Plan();
        plan.setSet(set);

        WallNode prevWallNode = null;
        for (Node node : contourDataDto.nodes()) {
            WallNode wallNode = new WallNode();
            wallNode.setPlan(plan);
            wallNode.setX(node.getX());
            wallNode.setY(node.getY());

            if (prevWallNode != null) {
                prevWallNode.setNext(wallNode);
            }

            prevWallNode = wallNode;
            plan.addNode(wallNode);
        }

        Plan savedPlan = planService.save(plan);

        Map<String, Object> response = new HashMap<>();
        response.put("redirect", "/plans/view/" + savedPlan.getId());

        return response;
    }

    @GetMapping("/view/{planId}")
    public String viewPlan(@PathVariable Long planId, Model model) {
        return "plan/viewPlan";
    }

    @GetMapping("/edit/{planId}")
    public String editPlan(@PathVariable Long planId, Model model) {
        return "plan/editPlan";
    }
}
