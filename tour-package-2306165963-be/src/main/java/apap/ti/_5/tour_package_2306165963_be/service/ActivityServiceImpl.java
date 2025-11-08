package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165963_be.repository.OrderedQuantityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private OrderedQuantityRepository orderedQuantityRepository;

    @Override
    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    @Override
    public Optional<Activity> getActivityById(String id) {
        return activityRepository.findById(id);
    }

    @Override
    public Activity createActivity(Activity activity) {
        // Validate business rules
        validateActivity(activity);
        
        // Generate ID
        activity.setId(UUID.randomUUID().toString());
        
        return activityRepository.save(activity);
    }

    @Override
    public Activity updateActivity(Activity activity) {
        // Check if exists
        Optional<Activity> existingActivity = activityRepository.findById(activity.getId());
        if (existingActivity.isEmpty()) {
            throw new IllegalArgumentException("Activity not found with ID: " + activity.getId());
        }
        
        // Validate business rules
        validateActivity(activity);
        
        // Update
        Activity existing = existingActivity.get();
        existing.setActivityName(activity.getActivityName());
        existing.setActivityType(activity.getActivityType());
        existing.setActivityItem(activity.getActivityItem());
        existing.setCapacity(activity.getCapacity());
        existing.setPrice(activity.getPrice());
        existing.setStartDate(activity.getStartDate());
        existing.setEndDate(activity.getEndDate());
        existing.setStartLocation(activity.getStartLocation());
        existing.setEndLocation(activity.getEndLocation());
        
        return activityRepository.save(existing);
    }

    @Override
    public boolean deleteActivity(String id) {
        // Check if activity is used in any ordered quantity
        if (orderedQuantityRepository.existsByActivityId(id)) {
            throw new IllegalStateException("Cannot delete activity that is used in existing plans");
        }
        
        if (activityRepository.existsById(id)) {
            activityRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Activity> getActivitiesByActivityType(String type) {
        return activityRepository.findByActivityType(type);
    }

    @Override
    public List<Activity> searchActivitiesByName(String name) {
        return activityRepository.findByActivityNameContainingIgnoreCase(name);
    }

    // ========== PRIVATE HELPER METHODS ==========
    
    private void validateActivity(Activity activity) {
        // Validate dates
        if (activity.getEndDate().isBefore(activity.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        // Validate capacity
        if (activity.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        
        // Validate price
        if (activity.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        
        // Validate activity type
        if (!isValidActivityType(activity.getActivityType())) {
            throw new IllegalArgumentException("Invalid activity type: " + activity.getActivityType());
        }
    }
    
    private boolean isValidActivityType(String type) {
        return "Flight".equalsIgnoreCase(type) || 
               "Accommodation".equalsIgnoreCase(type) || 
               "Vehicle Rental".equalsIgnoreCase(type);
    }
}