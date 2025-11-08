package apap.ti._5.tour_package_2306165963_be.repository;

import apap.ti._5.tour_package_2306165963_be.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, String> {
    
    // Find by activity type
    List<Activity> findByActivityType(String activityType);
    
    // Find by name containing (case-insensitive search)
    List<Activity> findByActivityNameContainingIgnoreCase(String name);
    
    // Find by price range
    List<Activity> findByPriceBetween(Long minPrice, Long maxPrice);
    
    // Find by capacity greater than or equal
    List<Activity> findByCapacityGreaterThanEqual(Integer capacity);
    
    // Find available activities (capacity > 0)
    List<Activity> findByCapacityGreaterThan(Integer capacity);
    
    // Check if activity exists by name
    boolean existsByActivityName(String activityName);
}