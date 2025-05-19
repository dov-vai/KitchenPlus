package com.kitchenplus.kitchenplus.controllers;

import com.kitchenplus.kitchenplus.data.models.*;
import com.kitchenplus.kitchenplus.data.services.PlanService;
import com.kitchenplus.kitchenplus.data.services.SetService;
import com.kitchenplus.kitchenplus.data.services.UserService;
import com.kitchenplus.kitchenplus.dtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/plans")
public class PlanController {
    private final PlanService planService;
    private final SetService setService;
    private final UserService userService;

    public PlanController(PlanService planService, SetService setService, UserService userService) {
        this.planService = planService;
        this.setService = setService;
        this.userService = userService;
    }

    @GetMapping("/{setId}")
    public String showPlansList(@PathVariable Long setId, Model model) {
        if (userService.getAuthUserAs(Client.class).isEmpty()){
            return "redirect:/login";
        }

        setService.get(setId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid set ID: " + setId));

        List<Plan> plans = planService.getAllBySetId(setId);

        model.addAttribute("plans", plans);
        model.addAttribute("setId", setId);
        return "/plan/plansList";
    }

    @GetMapping("/new/{setId}")
    public String showPlanCreation(@PathVariable Long setId, Model model) {
        if (userService.getAuthUserAs(Client.class).isEmpty()){
            return "redirect:/login";
        }

        model.addAttribute("setId", setId);
        return "/plan/planCreatePage";
    }

    @PostMapping("/new/{setId}")
    @ResponseBody
    public ResponseEntity<?> addPlan(@PathVariable Long setId, @RequestBody ContourDataDto contourDataDto) {
        if (userService.getAuthUserAs(Client.class).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized access"));
        }

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

        return ResponseEntity.ok(Map.of("redirect", "/plans/edit/" + savedPlan.getId()));
    }

    @GetMapping("/view/{planId}")
    public String showPlan(@PathVariable Long planId, Model model) {
        if (userService.getAuthUserAs(Client.class).isEmpty()){
            return "redirect:/login";
        }

        var plan = planService.get(planId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plan ID: " + planId));

        model.addAttribute("plan", plan);

        populatePlanModel(plan, model);
        return "plan/planPage";
    }

    @GetMapping("/edit/{planId}")
    public String showPlanEdit(@PathVariable Long planId, Model model) {
        if (userService.getAuthUserAs(Client.class).isEmpty()){
            return "redirect:/login";
        }

        var plan = planService.get(planId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plan ID: " + planId));

        model.addAttribute("plan", plan);

        populatePlanModel(plan, model);
        return "plan/planEditPage";
    }

    @PostMapping("/edit/{planId}")
    @ResponseBody
    public ResponseEntity<?> updatePlan(@PathVariable Long planId, @RequestBody PlanDataDto planDataDto) {
        if (userService.getAuthUserAs(Client.class).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized access"));
        }

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

        return ResponseEntity.ok(Map.of("redirect", "/plans/view/" + planId));
    }

    private void populatePlanModel(Plan plan, Model model) {
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

        model.addAttribute("spacerNodes", spacerNodes);
        model.addAttribute("itemNodes", itemNodes);
        model.addAttribute("wallNodes", wallNodes);
        model.addAttribute("setItems", setItems);
    }
}
