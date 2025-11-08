package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Plan;

import java.util.List;
import java.util.Optional;

public interface PlanService {
    List<Plan> getAllPlans();
    Optional<Plan> getPlanById(String id);
    Plan createPlan(String packageId, Plan plan);
    Plan updatePlan(Plan plan);
    boolean deletePlan(String id);
    void processPlan(String id);
    List<Plan> getPlansByPackageId(String packageId);
    Optional<Plan> getPlanWithOrderedQuantities(String id);
    Long calculateTotalPlanPrice(String planId);
}