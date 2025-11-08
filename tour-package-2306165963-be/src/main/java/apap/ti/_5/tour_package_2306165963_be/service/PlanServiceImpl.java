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
        return planRepository.findById(id);
    }

    @Override
    public Optional<Plan> getPlanWithOrderedQuantities(String id) {
        return planRepository.findByIdWithOrderedQuantities(id);
    }

    @Override
    public Plan createPlan(String packageId, Plan plan) {
        // Verify package exists
        Optional<Package> packageOptional = packageRepository.findById(packageId);
        if (packageOptional.isEmpty()) {
            throw new IllegalArgumentException("Package not found with ID: " + packageId);
        }
        
        Package packageEntity = packageOptional.get();
        
        // Check if package can be edited
        if ("Processed".equals(packageEntity.getStatus())) {
            throw new IllegalStateException("Cannot add plan to processed package");
        }
        
        // Validate plan
        validatePlan(plan);
        
        // Validate plan dates are within package dates
        if (plan.getStartDate().isBefore(packageEntity.getStartDate()) || 
            plan.getEndDate().isAfter(packageEntity.getEndDate())) {
            throw new IllegalArgumentException("Plan dates must be within package dates");
        }
        
        // Generate ID and set package ID
        plan.setId(UUID.randomUUID().toString());
        plan.setPackageId(packageId);
        
        // Set initial status
        plan.setStatus("Unfinished");
        
        return planRepository.save(plan);
    }

    @Override
    public Plan updatePlan(Plan plan) {
        // Check if exists
        Optional<Plan> existingPlan = planRepository.findById(plan.getId());
        if (existingPlan.isEmpty()) {
            throw new IllegalArgumentException("Plan not found with ID: " + plan.getId());
        }
        
        Plan existing = existingPlan.get();
        
        // Check if can be edited
        if ("Processed".equals(existing.getStatus())) {
            throw new IllegalStateException("Cannot update processed plan");
        }
        
        // Get package to validate dates
        Optional<Package> packageOptional = packageRepository.findById(existing.getPackageId());
        if (packageOptional.isPresent()) {
            Package packageEntity = packageOptional.get();
            
            // Validate plan dates are within package dates
            if (plan.getStartDate().isBefore(packageEntity.getStartDate()) || 
                plan.getEndDate().isAfter(packageEntity.getEndDate())) {
                throw new IllegalArgumentException("Plan dates must be within package dates");
            }
        }
        
        // Validate
        validatePlan(plan);
        
        // Update fields
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
        Optional<Plan> planOptional = planRepository.findById(id);
        
        if (planOptional.isEmpty()) {
            return false;
        }
        
        Plan plan = planOptional.get();
        
        // Check if can be deleted
        if ("Processed".equals(plan.getStatus())) {
            throw new IllegalStateException("Cannot delete processed plan");
        }
        
        // Delete all associated ordered quantities first
        orderedQuantityRepository.deleteByPlanId(id);
        
        // Delete plan
        planRepository.deleteById(id);
        return true;
    }

    @Override
    public void processPlan(String id) {
        Optional<Plan> planOptional = planRepository.findByIdWithOrderedQuantities(id);
        
        if (planOptional.isEmpty()) {
            throw new IllegalArgumentException("Plan not found with ID: " + id);
        }
        
        Plan plan = planOptional.get();
        
        // Check if already processed
        if ("Processed".equals(plan.getStatus())) {
            throw new IllegalStateException("Plan is already processed");
        }
        
        // Check if plan has ordered quantities
        if (plan.getOrderedQuantities() == null || plan.getOrderedQuantities().isEmpty()) {
            throw new IllegalStateException("Cannot process plan without ordered quantities");
        }
        
        // Calculate total price from ordered quantities
        Long totalPrice = calculateTotalPlanPrice(id);
        plan.setPrice(totalPrice);
        
        // Set status to Processed
        plan.setStatus("Processed");
        
        planRepository.save(plan);
    }

    @Override
    public List<Plan> getPlansByPackageId(String packageId) {
        return planRepository.findByPackageId(packageId);
    }

    @Override
    public Long calculateTotalPlanPrice(String planId) {
        Long totalPrice = orderedQuantityRepository.sumTotalPriceByPlanId(planId);
        return totalPrice != null ? totalPrice : 0L;
    }

    // ========== PRIVATE HELPER METHODS ==========
    
    private void validatePlan(Plan plan) {
        // Validate dates
        if (plan.getEndDate().isBefore(plan.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        // Validate price
        if (plan.getPrice() != null && plan.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        
        // Validate activity type
        if (!isValidActivityType(plan.getActivityType())) {
            throw new IllegalArgumentException("Invalid activity type: " + plan.getActivityType());
        }
        
        // Validate locations
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