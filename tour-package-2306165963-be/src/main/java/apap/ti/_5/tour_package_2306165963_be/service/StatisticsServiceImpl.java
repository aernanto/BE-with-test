package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private PackageService packageService;

    @Override
    public Map<String, Long> getRevenueByActivityType(Integer year, Integer month) {
        Map<String, Long> revenueMap = new HashMap<>();

        revenueMap.put("Flight", 0L);
        revenueMap.put("Accommodation", 0L);
        revenueMap.put("Vehicle Rental", 0L);

        List<Package> allPackages = packageService.getAllPackages();

        for (Package pkg : allPackages) {
            // Only count processed packages (or Waiting for Payment if that's the status)
            // The prompt says "Data diambil dari OrderedActivities yang sudah fulfilled"
            // But usually we check package status too.
            // PBI-BE-T17: "Data diambil dari OrderedActivities yang sudah fulfilled"
            // So we check Plan status = Fulfilled.

            // Check year
            if (pkg.getStartDate() != null && pkg.getStartDate().getYear() != year) {
                continue;
            }

            // Check month if provided
            if (month != null && pkg.getStartDate() != null &&
                    pkg.getStartDate().getMonthValue() != month) {
                continue;
            }

            for (Plan plan : pkg.getPlans()) {
                if (!"Fulfilled".equals(plan.getStatus())) {
                    continue;
                }

                String activityType = plan.getActivityType();

                if ("Vehicle".equalsIgnoreCase(activityType)) {
                    activityType = "Vehicle Rental";
                }

                long planRevenue = plan.getOrderedQuantities().stream()
                        .mapToLong(OrderedQuantity::getTotalPrice)
                        .sum();

                revenueMap.put(activityType, revenueMap.getOrDefault(activityType, 0L) + planRevenue);
            }
        }

        return revenueMap;
    }

    @Override
    public Map<Integer, Long> getYearlyRevenue(Integer year) {
        Map<Integer, Long> revenueMap = new TreeMap<>();
        for (int i = 1; i <= 12; i++) {
            revenueMap.put(i, 0L);
        }

        List<Package> allPackages = packageService.getAllPackages();

        for (Package pkg : allPackages) {
            if (pkg.getStartDate() != null && pkg.getStartDate().getYear() == year) {
                for (Plan plan : pkg.getPlans()) {
                    if ("Fulfilled".equals(plan.getStatus())) {
                        int month = pkg.getStartDate().getMonthValue();
                        long planRevenue = plan.getOrderedQuantities().stream()
                                .mapToLong(OrderedQuantity::getTotalPrice)
                                .sum();
                        revenueMap.put(month, revenueMap.getOrDefault(month, 0L) + planRevenue);
                    }
                }
            }
        }

        return revenueMap;
    }
}