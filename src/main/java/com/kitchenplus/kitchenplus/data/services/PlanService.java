package com.kitchenplus.kitchenplus.data.services;

import com.kitchenplus.kitchenplus.data.models.Plan;
import com.kitchenplus.kitchenplus.data.repositories.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanService {
    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public Plan save(Plan plan) {
        return planRepository.save(plan);
    }

    public Optional<Plan> getById(Long id) {
        return planRepository.findById(id);
    }

    public List<Plan> getAllBySetId(Long setId) {
        return planRepository.findAllBySetId(setId);
    }
}
