package com.kitchenplus.kitchenplus.data.repositories;

import com.kitchenplus.kitchenplus.data.models.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findAllBySetId(Long setId);
}
