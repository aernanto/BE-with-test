package apap.ti._5.tour_package_2306165963_be.service;

import java.util.Map;

public interface StatisticsService {

    /**
     * Get potential revenue by activity type
     * 
     * @param year  Filter by year
     * @param month Filter by month (null = all months)
     * @return Map of activity type to total revenue
     */
    Map<String, Long> getRevenueByActivityType(Integer year, Integer month);

    Map<Integer, Long> getYearlyRevenue(Integer year);
}