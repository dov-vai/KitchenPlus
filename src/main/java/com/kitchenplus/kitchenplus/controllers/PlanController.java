package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.data.models.*;
import com.kitchenplus.kitchenplus.data.services.PlanService;
import com.kitchenplus.kitchenplus.data.services.SetService;
import com.kitchenplus.kitchenplus.dtos.*;
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
        var set = setService.get(setId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid set ID: " + setId));

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
        response.put("redirect", "/plans/edit/" + savedPlan.getId());

        return response;
    }

    @GetMapping("/view/{planId}")
    public String viewPlan(@PathVariable Long planId, Model model) {
        var plan = planService.get(planId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plan ID: " + planId));

        model.addAttribute("plan", plan);

        return "plan/viewPlan";
    }

    @GetMapping("/edit/{planId}")
    public String editPlan(@PathVariable Long planId, Model model) {
        var plan = planService.get(planId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plan ID: " + planId));

        List<WallNodeDto> wallNodes = plan.getNodes().stream()
                .filter(node -> node instanceof WallNode)
                .map(node -> new WallNodeDto(node.getId(), node.getX(), node.getY()))
                .toList();

        List<SpacerNodeDto> spacerNodes = plan.getNodes().stream()
                .filter(node -> node instanceof SpacerNode)
                .map(node -> (SpacerNode) node)
                .map(node -> new SpacerNodeDto(node.getX(), node.getY(), node.getAngle(), node.getWidth(), node.getHeight()))
                .toList();

        List<ItemNodeDto> itemNodes = plan.getNodes().stream()
                .filter(node -> node instanceof ItemNode)
                .map(node -> (ItemNode) node)
                .map(node -> new ItemNodeDto(node.getItem().getId(), node.getX(), node.getY(), node.getAngle()))
                .toList();

        List<SetItemDto> setItems = plan.getSet().getItems().stream()
                .map(setItem -> {
                    var item = setItem.getItem();
                    return new SetItemDto(setItem.getId(), item.getId(), item.getName(), item.getDescription(), item.getPrice(), item.getHeight(), item.getWidth(), item.getFirstImageLink());
                }).toList();

        model.addAttribute("plan", plan);
        model.addAttribute("spacerNodes", spacerNodes);
        model.addAttribute("itemNodes", itemNodes);
        model.addAttribute("wallNodes", wallNodes);
        model.addAttribute("setItems", setItems);

        return "plan/editPlan";
    }

    @PostMapping("/edit/{planId}")
    @ResponseBody
    public Map<String, Object> editPlan(@PathVariable Long planId, @RequestBody PlanDataDto planDataDto) {
        var plan = planService.get(planId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plan ID: " + planId));

        // clear for updates
        plan.clearSpacerNodes();
        plan.clearItemNodes();

        for (SpacerNodeDto spacer : planDataDto.spacers()) {
            if (spacer == null) {
                continue;
            }

            SpacerNode node = new SpacerNode();
            node.setX(spacer.x());
            node.setY(spacer.y());
            node.setAngle(spacer.angle());
            node.setHeight(spacer.height());
            node.setWidth(spacer.width());
            plan.addNode(node);
        }

        for (ItemNodeDto item : planDataDto.items()) {
            if (item == null) {
                continue;
            }

            ItemNode node = new ItemNode();
            SetItem setItem = setService.getSetItem(item.id())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid set item ID: " + item.id()));
            node.setItem(setItem);
            node.setX(item.x());
            node.setY(item.y());
            node.setAngle(item.angle());
            plan.addNode(node);
        }

        planService.save(plan);

        Map<String, Object> response = new HashMap<>();
        response.put("redirect", "/plans/view/" + planId);

        return response;
    }
}
