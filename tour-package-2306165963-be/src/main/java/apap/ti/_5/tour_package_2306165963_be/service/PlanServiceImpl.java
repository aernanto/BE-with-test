package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
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
public class PlanServiceImpl implements PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private OrderedQuantityRepository orderedQuantityRepository;

    @Override
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Override
    public Optional<Plan> getPlanById(String id) {
        return planRepository.findById(UUID.fromString(id));
    }

    @Override
    public Optional<Plan> getPlanWithOrderedQuantities(String id) {
        return planRepository.findByIdWithOrderedQuantities(UUID.fromString(id));
    }

    @Override
    public Plan createPlan(String packageId, Plan plan) {
        Optional<Package> packageOptional = packageRepository.findById(packageId);
        if (packageOptional.isEmpty()) {
            throw new IllegalArgumentException("Package not found with ID: " + packageId);
        }

        Package packageEntity = packageOptional.get();

        // Can only create plan for Pending packages
        if (!"Pending".equals(packageEntity.getStatus())) {
            throw new IllegalStateException("Can only create plan for Pending packages");
        }

        validatePlan(plan);

        if (plan.getStartDate().isBefore(packageEntity.getStartDate()) ||
                plan.getEndDate().isAfter(packageEntity.getEndDate())) {
            throw new IllegalArgumentException("Plan dates must be within package dates");
        }

        plan.setId(UUID.randomUUID());
        plan.setPackageId(packageId);
        plan.setStatus("Unfulfilled"); // Default status

        return planRepository.save(plan);
    }

    @Override
    public Plan updatePlan(Plan plan) {
        Optional<Plan> existingPlan = planRepository.findById(plan.getId());
        if (existingPlan.isEmpty()) {
            throw new IllegalArgumentException("Plan not found with ID: " + plan.getId());
        }

        Plan existing = existingPlan.get();

        Optional<Package> packageOptional = packageRepository.findById(existing.getPackageId());
        if (packageOptional.isPresent()) {
            Package packageEntity = packageOptional.get();

            // Can only update if Package status = Pending
            if (!"Pending".equals(packageEntity.getStatus())) {
                throw new IllegalStateException("Can only update plans for Pending packages");
            }

            if (plan.getStartDate().isBefore(packageEntity.getStartDate()) ||
                    plan.getEndDate().isAfter(packageEntity.getEndDate())) {
                throw new IllegalArgumentException("Plan dates must be within package dates");
            }
        }

        validatePlan(plan);

        existing.setActivityType(plan.getActivityType());
        existing.setPrice(plan.getPrice());
        existing.setStartDate(plan.getStartDate());
        existing.setEndDate(plan.getEndDate());
        existing.setStartLocation(plan.getStartLocation());
        existing.setEndLocation(plan.getEndLocation());

        return planRepository.save(existing);
    }

    @Override
    public boolean deletePlan(String id) {
        Optional<Plan> planOptional = planRepository.findById(UUID.fromString(id));

        if (planOptional.isEmpty()) {
            return false;
        }

        Plan plan = planOptional.get();

        Optional<Package> packageOptional = packageRepository.findById(plan.getPackageId());
        if (packageOptional.isPresent()) {
            // Can only delete if Package status = Pending
            if (!"Pending".equals(packageOptional.get().getStatus())) {
                throw new IllegalStateException("Can only delete plans for Pending packages");
            }
        }

        orderedQuantityRepository.deleteByPlanId(UUID.fromString(id));
        planRepository.deleteById(UUID.fromString(id));
        return true;
    }

    @Override
    public void processPlan(String id) {
        Optional<Plan> planOptional = planRepository.findByIdWithOrderedQuantities(UUID.fromString(id));

        if (planOptional.isEmpty()) {
            throw new IllegalArgumentException("Plan not found with ID: " + id);
        }

        Plan plan = planOptional.get();

        // Already fulfilled
        if ("Fulfilled".equals(plan.getStatus())) {
            throw new IllegalStateException("Plan is already fulfilled");
        }

        // Must have ordered quantities
        if (plan.getOrderedQuantities() == null || plan.getOrderedQuantities().isEmpty()) {
            throw new IllegalStateException("Cannot process plan without ordered quantities");
        }

        // Calculate total price from ordered quantities
        Long totalPrice = calculateTotalPlanPrice(id);
        plan.setPrice(totalPrice);

        Optional<Package> pkgOpt = packageRepository.findById(plan.getPackageId());
        if (pkgOpt.isEmpty()) {
            throw new IllegalStateException("Package not found for this plan");
        }

        Package pkg = pkgOpt.get();
        int packageQuota = pkg.getQuota();

        // Calculate total ordered quantity across this plan
        int totalOrderedQuantity = plan.getOrderedQuantities().stream()
                .mapToInt(OrderedQuantity::getOrderedQuota)
                .sum();

        if (totalOrderedQuantity >= packageQuota) {
            plan.setStatus("Fulfilled");
        } else {
            plan.setStatus("Unfulfilled");
        }

        planRepository.save(plan);
    }

    @Override
    public List<Plan> getPlansByPackageId(String packageId) {
        return planRepository.findByPackageId(packageId);
    }

    @Override
    public Long calculateTotalPlanPrice(String planId) {
        Long totalPrice = orderedQuantityRepository.sumTotalPriceByPlanId(UUID.fromString(planId));
        return totalPrice != null ? totalPrice : 0L;
    }

    private void validatePlan(Plan plan) {
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (plan.getPrice() != null && plan.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        if (!isValidActivityType(plan.getActivityType())) {
            throw new IllegalArgumentException("Invalid activity type: " + plan.getActivityType());
        }

        if (plan.getStartLocation() == null || plan.getStartLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Start location is required");
        }

        if (plan.getEndLocation() == null || plan.getEndLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("End location is required");
        }
    }

    private boolean isValidActivityType(String type) {
        return "Flight".equalsIgnoreCase(type) ||
                "Accommodation".equalsIgnoreCase(type) ||
                "Vehicle".equalsIgnoreCase(type);
    }
}