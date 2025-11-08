package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private PackageService packageService;

    @Override
    public Map<String, Long> getRevenueByActivityType(Integer year, Integer month) {
        Map<String, Long> revenueMap = new HashMap<>();
        
        // Initialize all activity types with 0
        revenueMap.put("Flight", 0L);
        revenueMap.put("Accommodation", 0L);
        revenueMap.put("Vehicle Rental", 0L);

        List<Package> allPackages = packageService.getAllPackages();

        for (Package pkg : allPackages) {
            // Filter by year
            if (pkg.getStartDate() != null && pkg.getStartDate().getYear() != year) {
                continue;
            }

            // Filter by month if provided
            if (month != null && pkg.getStartDate() != null && 
                pkg.getStartDate().getMonthValue() != month) {
                continue;
            }

            // Calculate revenue from all plans
            for (Plan plan : pkg.getPlans()) {
                String activityType = plan.getActivityType();
                
                // Normalize activity type name
                if ("Vehicle".equalsIgnoreCase(activityType)) {
                    activityType = "Vehicle Rental";
                }

                // Sum up all ordered quantities for this plan
                long planRevenue = plan.getOrderedQuantities().stream()
                        .mapToLong(OrderedQuantity::getTotalPrice)
                        .sum();

                // Add to the corresponding activity type
                revenueMap.put(activityType, revenueMap.getOrDefault(activityType, 0L) + planRevenue);
            }
        }

        return revenueMap;
    }
}