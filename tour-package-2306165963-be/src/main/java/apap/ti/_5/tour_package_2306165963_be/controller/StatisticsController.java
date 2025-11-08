package apap.ti._5.tour_package_2306165963_be.controller;

import apap.ti._5.tour_package_2306165963_be.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    public String getStatistics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model,
            HttpServletRequest request) {

        // Default to current year if not provided
        if (year == null) {
            year = Year.now().getValue();
        }

        // Get revenue data
        Map<String, Long> revenueData = statisticsService.getRevenueByActivityType(year, month);

        // Prepare years list (current year ± 5 years)
        List<Integer> years = new ArrayList<>();
        int currentYear = Year.now().getValue();
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            years.add(i);
        }

        // Prepare months list
        List<String> months = List.of("All", "January", "February", "March", "April", "May", 
                                      "June", "July", "August", "September", "October", 
                                      "November", "December");

        model.addAttribute("revenueData", revenueData);
        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month == null ? "All" : months.get(month));
        model.addAttribute("currentUri", request.getRequestURI());

        return "statistics/index";
    }
}