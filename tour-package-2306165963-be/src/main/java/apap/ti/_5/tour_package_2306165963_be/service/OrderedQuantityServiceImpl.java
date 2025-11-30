package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165963_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165963_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165963_be.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderedQuantityServiceImpl implements OrderedQuantityService {

    @Autowired
    private OrderedQuantityRepository orderedQuantityRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Override
    public List<OrderedQuantity> getAllOrderedQuantities() {
        return orderedQuantityRepository.findAll();
    }

    @Override
    public Optional<OrderedQuantity> getOrderedQuantityById(String id) {
        return orderedQuantityRepository.findById(UUID.fromString(id));
    }

    @Override
    public OrderedQuantity createOrderedQuantity(String planId, OrderedQuantity orderedQuantity) {
        Optional<Plan> planOptional = planRepository.findById(UUID.fromString(planId));
        if (planOptional.isEmpty()) {
            throw new IllegalArgumentException("Plan not found with ID: " + planId);
        }

        Plan plan = planOptional.get();

        // Check package status - only Pending packages can add ordered quantities
        Optional<Package> pkgOpt = packageRepository.findById(plan.getPackageId());
        if (pkgOpt.isEmpty()) {
            throw new IllegalArgumentException("Package not found");
        }

        Package pkg = pkgOpt.get();
        if (!"Pending".equals(pkg.getStatus())) {
            throw new IllegalStateException("Can only add ordered quantity to Pending packages");
        }

        String activityId = orderedQuantity.getActivityId();
        Optional<Activity> activityOptional = activityRepository.findById(activityId);
        if (activityOptional.isEmpty()) {
            throw new IllegalArgumentException("Activity not found with ID: " + activityId);
        }

        Activity activity = activityOptional.get();

        if (!activity.getActivityType().equals(plan.getActivityType())) {
            throw new IllegalArgumentException(
                    "Activity type (" + activity.getActivityType() +
                            ") does not match plan type (" + plan.getActivityType() + ")");
        }

        if (activity.getStartDate().isBefore(plan.getStartDate())) {
            throw new IllegalArgumentException(
                    "Activity StartDate must be same or after Plan StartDate");
        }

        if (activity.getEndDate().isAfter(plan.getEndDate())) {
            throw new IllegalArgumentException(
                    "Activity EndDate must be same or before Plan EndDate");
        }

        if (!activity.getStartLocation().equals(plan.getStartLocation()) ||
                !activity.getEndLocation().equals(plan.getEndLocation())) {
            throw new IllegalArgumentException(
                    "Activity StartLocation & EndLocation must match Plan locations");
        }

        // Check if activity is already in the plan
        List<OrderedQuantity> existingOQ = orderedQuantityRepository
                .findByPlanIdAndActivityId(UUID.fromString(planId), activityId);
        if (!existingOQ.isEmpty()) {
            throw new IllegalStateException("Activity is already added to this plan");
        }

        if (orderedQuantity.getOrderedQuota() > activity.getCapacity()) {
            throw new IllegalArgumentException(
                    "Ordered quota (" + orderedQuantity.getOrderedQuota() +
                            ") exceeds activity capacity (" + activity.getCapacity() + ")");
        }

        int totalOrderedQuantityAcrossPlan = orderedQuantityRepository.findByPlanId(UUID.fromString(planId)).stream()
                .mapToInt(OrderedQuantity::getOrderedQuota)
                .sum();

        if (totalOrderedQuantityAcrossPlan + orderedQuantity.getOrderedQuota() > pkg.getQuota()) {
            throw new IllegalArgumentException(
                    "Total ordered quantity across all plans cannot exceed Package Quota (" + pkg.getQuota() + ")");
        }

        orderedQuantity.setId(UUID.randomUUID());
        orderedQuantity.setPlanId(UUID.fromString(planId));
        orderedQuantity.setQuota(activity.getCapacity());
        orderedQuantity.setPrice(activity.getPrice());
        orderedQuantity.setActivityName(activity.getActivityName());
        orderedQuantity.setActivityItem(activity.getActivityItem());
        orderedQuantity.setStartDate(activity.getStartDate());
        orderedQuantity.setEndDate(activity.getEndDate());

        OrderedQuantity saved = orderedQuantityRepository.save(orderedQuantity);

        updatePlanStatusAndPrice(planId, pkg.getQuota());

        return saved;
    }

    @Override
    public OrderedQuantity updateOrderedQuantity(String id, Integer newQuota) {
        Optional<OrderedQuantity> existingOQ = orderedQuantityRepository.findById(UUID.fromString(id));
        if (existingOQ.isEmpty()) {
            throw new IllegalArgumentException("Ordered quantity not found with ID: " + id);
        }

        OrderedQuantity oq = existingOQ.get();

        Optional<Plan> planOptional = planRepository.findById(oq.getPlanId());
        if (planOptional.isEmpty()) {
            throw new IllegalArgumentException("Plan not found");
        }

        Plan plan = planOptional.get();

        // Check package status
        Optional<Package> pkgOpt = packageRepository.findById(plan.getPackageId());
        if (pkgOpt.isEmpty()) {
            throw new IllegalArgumentException("Package not found");
        }

        Package pkg = pkgOpt.get();
        if (!"Pending".equals(pkg.getStatus())) {
            throw new IllegalStateException("Can only update ordered quantity for Pending packages");
        }

        if (newQuota <= 0) {
            throw new IllegalArgumentException("Ordered quota must be greater than 0");
        }

        if (newQuota > oq.getQuota()) {
            throw new IllegalArgumentException(
                    "Ordered quota (" + newQuota +
                            ") exceeds activity capacity (" + oq.getQuota() + ")");
        }

        // Check total ordered quantity doesn't exceed package quota
        int currentTotal = orderedQuantityRepository.findByPlanId(oq.getPlanId()).stream()
                .filter(item -> !item.getId().equals(UUID.fromString(id))) // exclude current
                .mapToInt(OrderedQuantity::getOrderedQuota)
                .sum();

        if (currentTotal + newQuota > pkg.getQuota()) {
            throw new IllegalArgumentException(
                    "Total ordered quantity cannot exceed Package Quota (" + pkg.getQuota() + ")");
        }

        oq.setOrderedQuota(newQuota);
        OrderedQuantity saved = orderedQuantityRepository.save(oq);

        // Update plan status & price after updating
        updatePlanStatusAndPrice(oq.getPlanId().toString(), pkg.getQuota());

        return saved;
    }

    @Override
    public boolean deleteOrderedQuantity(String id) {
        Optional<OrderedQuantity> oqOptional = orderedQuantityRepository.findById(UUID.fromString(id));

        if (oqOptional.isEmpty()) {
            return false;
        }

        OrderedQuantity oq = oqOptional.get();

        Optional<Plan> planOptional = planRepository.findById(oq.getPlanId());
        if (planOptional.isEmpty()) {
            orderedQuantityRepository.deleteById(UUID.fromString(id));
            return true;
        }

        Plan plan = planOptional.get();

        // Check package status
        Optional<Package> pkgOpt = packageRepository.findById(plan.getPackageId());
        if (pkgOpt.isEmpty()) {
            throw new IllegalArgumentException("Package not found");
        }

        Package pkg = pkgOpt.get();
        if (!"Pending".equals(pkg.getStatus())) {
            throw new IllegalStateException("Can only delete ordered quantity from Pending packages");
        }

        orderedQuantityRepository.deleteById(UUID.fromString(id));

        // Update plan status & price after deleting
        updatePlanStatusAndPrice(oq.getPlanId().toString(), pkg.getQuota());

        return true;
    }

    @Override
    public List<OrderedQuantity> getOrderedQuantitiesByPlanId(String planId) {
        return orderedQuantityRepository.findByPlanId(UUID.fromString(planId));
    }

    @Override
    public Long calculateTotalPriceForPlan(String planId) {
        Long totalPrice = orderedQuantityRepository.sumTotalPriceByPlanId(UUID.fromString(planId));
        return totalPrice != null ? totalPrice : 0L;
    }

    private void updatePlanStatusAndPrice(String planId, int packageQuota) {
        Optional<Plan> planOpt = planRepository.findById(UUID.fromString(planId));
        if (planOpt.isEmpty()) {
            return;
        }

        Plan plan = planOpt.get();

        // Calculate total ordered quantity
        int totalOrderedQuantity = orderedQuantityRepository.findByPlanId(UUID.fromString(planId)).stream()
                .mapToInt(OrderedQuantity::getOrderedQuota)
                .sum();

        // Calculate total price
        Long totalPrice = calculateTotalPriceForPlan(planId);
        plan.setPrice(totalPrice);

        if (totalOrderedQuantity >= packageQuota) {
            plan.setStatus("Fulfilled");
        } else if (totalOrderedQuantity > 0) {
            plan.setStatus("Unfulfilled");
        } else {
            plan.setStatus("Unfulfilled"); // No ordered quantities yet
        }

        planRepository.save(plan);
    }
}