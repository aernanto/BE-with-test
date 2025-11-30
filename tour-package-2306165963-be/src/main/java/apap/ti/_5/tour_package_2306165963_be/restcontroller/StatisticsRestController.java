package apap.ti._5.tour_package_2306165963_be.restcontroller;

import apap.ti._5.tour_package_2306165963_be.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsRestController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * GET /api/statistics/revenue?year=2025&month=1
     * Get revenue statistics by activity type for a specific year and optional
     * month
     * Only accessible by Superadmin and TourPackageVendor (PBI-FE-T22)
     */
    @PreAuthorize("hasAnyAuthority('Superadmin', 'TourPackageVendor')")
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueStatistics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        // Default to current year if not provided
        if (year == null) {
            year = Year.now().getValue();
        }

        // Validate month
        if (month != null && (month < 1 || month > 12)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Month must be between 1 and 12"));
        }

        // Get revenue data
        Map<String, Long> revenueByType = statisticsService.getRevenueByActivityType(year, month);

        // Calculate total
        long totalRevenue = revenueByType.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("period", month != null ? String.format("%d-%02d", year, month) : String.valueOf(year));
        response.put("totalRevenue", totalRevenue);
        response.put("breakdown", revenueByType);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/statistics/revenue/yearly/2025
     * Get monthly revenue breakdown for an entire year
     * Only accessible by Superadmin and TourPackageVendor (PBI-FE-T23)
     */
    @PreAuthorize("hasAnyAuthority('Superadmin', 'TourPackageVendor')")
    @GetMapping("/revenue/yearly/{year}")
    public ResponseEntity<Map<String, Object>> getYearlyRevenue(@PathVariable Integer year) {

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);

        Map<Integer, Long> monthlyRevenue = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            Map<String, Long> revenueByType = statisticsService.getRevenueByActivityType(year, month);
            long monthTotal = revenueByType.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();
            monthlyRevenue.put(month, monthTotal);
        }

        response.put("monthlyRevenue", monthlyRevenue);

        long yearTotal = monthlyRevenue.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        response.put("totalRevenue", yearTotal);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/statistics/revenue/monthly/2025/1
     * Get detailed revenue breakdown for a specific month
     * Only accessible by Superadmin and TourPackageVendor (PBI-FE-T24)
     */
    @PreAuthorize("hasAnyAuthority('Superadmin', 'TourPackageVendor')")
    @GetMapping("/revenue/monthly/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getMonthlyRevenue(
            @PathVariable Integer year,
            @PathVariable Integer month) {

        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest().body(Map.of("error", "Month must be between 1 and 12"));
        }

        Map<String, Long> revenueByType = statisticsService.getRevenueByActivityType(year, month);

        long totalRevenue = revenueByType.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("period", String.format("%d-%02d", year, month));
        response.put("totalRevenue", totalRevenue);
        response.put("breakdown", revenueByType);

        return ResponseEntity.ok(response);
    }
}