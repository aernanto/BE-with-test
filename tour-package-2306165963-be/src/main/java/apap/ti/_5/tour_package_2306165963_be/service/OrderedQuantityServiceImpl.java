package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165963_be.repository.OrderedQuantityRepository;
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

    @Override
    public List<OrderedQuantity> getAllOrderedQuantities() {
        return orderedQuantityRepository.findAll();
    }

    @Override
    public Optional<OrderedQuantity> getOrderedQuantityById(String id) {
        return orderedQuantityRepository.findById(id);
    }

    @Override
    public OrderedQuantity createOrderedQuantity(String planId, OrderedQuantity orderedQuantity) {
        // Verify plan exists
        Optional<Plan> planOptional = planRepository.findById(planId);
        if (planOptional.isEmpty()) {
            throw new IllegalArgumentException("Plan not found with ID: " + planId);
        }
        
        Plan plan = planOptional.get();
        
        // Check if plan can be edited
        if ("Processed".equals(plan.getStatus())) {
            throw new IllegalStateException("Cannot add ordered quantity to processed plan");
        }
        
        // Verify activity exists
        String activityId = orderedQuantity.getActivityId();
        Optional<Activity> activityOptional = activityRepository.findById(activityId);
        if (activityOptional.isEmpty()) {
            throw new IllegalArgumentException("Activity not found with ID: " + activityId);
        }
        
        Activity activity = activityOptional.get();
        
        // Validate activity type matches plan type
        if (!activity.getActivityType().equals(plan.getActivityType())) {
            throw new IllegalArgumentException(
                "Activity type (" + activity.getActivityType() + 
                ") does not match plan type (" + plan.getActivityType() + ")"
            );
        }
        
        // Check if activity is already in the plan
        List<OrderedQuantity> existingOQ = orderedQuantityRepository
            .findByPlanIdAndActivityId(planId, activityId);
        if (!existingOQ.isEmpty()) {
            throw new IllegalStateException("Activity is already added to this plan");
        }
        
        // Validate ordered quota
        if (orderedQuantity.getOrderedQuota() > activity.getCapacity()) {
            throw new IllegalArgumentException(
                "Ordered quota (" + orderedQuantity.getOrderedQuota() + 
                ") exceeds activity capacity (" + activity.getCapacity() + ")"
            );
        }
        
        // Generate ID and set plan ID
        orderedQuantity.setId(UUID.randomUUID().toString());
        orderedQuantity.setPlanId(planId);
        
        // Copy activity details to ordered quantity for denormalization
        orderedQuantity.setQuota(activity.getCapacity());
        orderedQuantity.setPrice(activity.getPrice());
        orderedQuantity.setActivityName(activity.getActivityName());
        orderedQuantity.setActivityItem(activity.getActivityItem());
        orderedQuantity.setStartDate(activity.getStartDate());
        orderedQuantity.setEndDate(activity.getEndDate());
        
        // Save
        OrderedQuantity saved = orderedQuantityRepository.save(orderedQuantity);
        
        // Update plan status to Pending (has at least one ordered quantity)
        if ("Unfinished".equals(plan.getStatus())) {
            plan.setStatus("Pending");
            planRepository.save(plan);
        }
        
        return saved;
    }

    @Override
    public OrderedQuantity updateOrderedQuantity(String id, Integer newQuota) {
        // Check if exists
        Optional<OrderedQuantity> existingOQ = orderedQuantityRepository.findById(id);
        if (existingOQ.isEmpty()) {
            throw new IllegalArgumentException("Ordered quantity not found with ID: " + id);
        }
        
        OrderedQuantity oq = existingOQ.get();
        
        // Get plan to check status
        Optional<Plan> planOptional = planRepository.findById(oq.getPlanId());
        if (planOptional.isPresent() && "Processed".equals(planOptional.get().getStatus())) {
            throw new IllegalStateException("Cannot update ordered quantity of processed plan");
        }
        
        // Validate new quota
        if (newQuota <= 0) {
            throw new IllegalArgumentException("Ordered quota must be greater than 0");
        }
        
        if (newQuota > oq.getQuota()) {
            throw new IllegalArgumentException(
                "Ordered quota (" + newQuota + 
                ") exceeds activity capacity (" + oq.getQuota() + ")"
            );
        }
        
        // Update
        oq.setOrderedQuota(newQuota);
        
        return orderedQuantityRepository.save(oq);
    }

    @Override
    public boolean deleteOrderedQuantity(String id) {
        Optional<OrderedQuantity> oqOptional = orderedQuantityRepository.findById(id);
        
        if (oqOptional.isEmpty()) {
            return false;
        }
        
        OrderedQuantity oq = oqOptional.get();
        
        // Get plan to check status
        Optional<Plan> planOptional = planRepository.findById(oq.getPlanId());
        if (planOptional.isPresent()) {
            Plan plan = planOptional.get();
            
            if ("Processed".equals(plan.getStatus())) {
                throw new IllegalStateException("Cannot delete ordered quantity from processed plan");
            }
            
            // Delete
            orderedQuantityRepository.deleteById(id);
            
            // Check if plan now has no ordered quantities
            long count = orderedQuantityRepository.countByPlanId(oq.getPlanId());
            if (count == 0) {
                plan.setStatus("Unfinished");
                planRepository.save(plan);
            }
        } else {
            // Delete anyway if plan doesn't exist
            orderedQuantityRepository.deleteById(id);
        }
        
        return true;
    }

    @Override
    public List<OrderedQuantity> getOrderedQuantitiesByPlanId(String planId) {
        return orderedQuantityRepository.findByPlanId(planId);
    }

    @Override
    public Long calculateTotalPriceForPlan(String planId) {
        Long totalPrice = orderedQuantityRepository.sumTotalPriceByPlanId(planId);
        return totalPrice != null ? totalPrice : 0L;
    }
}