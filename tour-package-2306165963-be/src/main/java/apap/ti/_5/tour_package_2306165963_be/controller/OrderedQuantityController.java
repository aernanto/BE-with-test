package apap.ti._5.tour_package_2306165963_be.controller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.orderedquantity.*;
import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.service.ActivityService;
import apap.ti._5.tour_package_2306165963_be.service.OrderedQuantityService;
import apap.ti._5.tour_package_2306165963_be.service.PackageService;
import apap.ti._5.tour_package_2306165963_be.service.PlanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/package/{packageId}/plan/{planId}/ordered-quantity")
public class OrderedQuantityController {

    @Autowired
    private OrderedQuantityService orderedQuantityService;
    
    @Autowired
    private PlanService planService;
    
    @Autowired
    private PackageService packageService;
    
    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping("/create")
    public String formCreateOrderedQuantity(@PathVariable String packageId,
                                           @PathVariable String planId,
                                           Model model,
                                           HttpServletRequest request) {
        
        // Verify package exists
        Optional<Package> packageOptional = packageService.getPackageById(packageId);
        if (packageOptional.isEmpty()) {
            model.addAttribute("title", "Package Not Found");
            model.addAttribute("message", "Package with ID " + packageId + " not found.");
            return "error/404";
        }

        // Verify plan exists
        Optional<Plan> planOptional = planService.getPlanById(planId);
        if (planOptional.isEmpty()) {
            model.addAttribute("title", "Plan Not Found");
            model.addAttribute("message", "Plan with ID " + planId + " not found.");
            return "error/404";
        }

        Plan plan = planOptional.get();
        
        // Check if plan can be edited
        if ("Processed".equals(plan.getStatus())) {
            model.addAttribute("title", "Cannot Add Ordered Quantity");
            model.addAttribute("message", "Cannot add ordered quantity to processed plan.");
            return "error/403";
        }

        // Get available activities matching plan's activity type
        List<Activity> availableActivities = activityService.getAllActivities()
                .stream()
                .filter(a -> a.getActivityType().equals(plan.getActivityType()))
                .collect(Collectors.toList());

        model.addAttribute("packageId", packageId);
        model.addAttribute("planId", planId);
        model.addAttribute("planData", plan);
        model.addAttribute("orderedQuantityData", new CreateOrderedQuantityDto());
        model.addAttribute("availableActivities", availableActivities);
        model.addAttribute("currentUri", request.getRequestURI());
        return "ordered-quantity/form";
    }

    @PostMapping("/create")
    public String createOrderedQuantity(@PathVariable String packageId,
                                       @PathVariable String planId,
                                       @Valid @ModelAttribute("orderedQuantityData") CreateOrderedQuantityDto oqDto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model,
                                       HttpServletRequest request) {
        
        if (bindingResult.hasErrors()) {
            Optional<Plan> planOptional = planService.getPlanById(planId);
            List<Activity> availableActivities = activityService.getAllActivities()
                    .stream()
                    .filter(a -> planOptional.isPresent() && 
                                 a.getActivityType().equals(planOptional.get().getActivityType()))
                    .collect(Collectors.toList());
            
            model.addAttribute("packageId", packageId);
            model.addAttribute("planId", planId);
            model.addAttribute("planData", planOptional.orElse(null));
            model.addAttribute("orderedQuantityData", oqDto);
            model.addAttribute("availableActivities", availableActivities);
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("errorMessage", "❌ Please fix the validation errors.");
            return "ordered-quantity/form";
        }
        
        try {
            OrderedQuantity oq = dtoMapper.toEntity(oqDto);
            orderedQuantityService.createOrderedQuantity(planId, oq);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Ordered quantity added successfully!");
            return "redirect:/package/" + packageId + "/plan/" + planId;
        } catch (Exception e) {
            Optional<Plan> planOptional = planService.getPlanById(planId);
            List<Activity> availableActivities = activityService.getAllActivities()
                    .stream()
                    .filter(a -> planOptional.isPresent() && 
                                 a.getActivityType().equals(planOptional.get().getActivityType()))
                    .collect(Collectors.toList());
            
            model.addAttribute("packageId", packageId);
            model.addAttribute("planId", planId);
            model.addAttribute("planData", planOptional.orElse(null));
            model.addAttribute("orderedQuantityData", oqDto);
            model.addAttribute("availableActivities", availableActivities);
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("errorMessage", "❌ " + e.getMessage());
            return "ordered-quantity/form";
        }
    }

    @PostMapping("/{oqId}/delete")
    public String deleteOrderedQuantity(@PathVariable String packageId,
                                       @PathVariable String planId,
                                       @PathVariable String oqId,
                                       RedirectAttributes redirectAttributes) {
        try {
            boolean removed = orderedQuantityService.deleteOrderedQuantity(oqId);
            
            if (removed) {
                redirectAttributes.addFlashAttribute("successMessage", "✅ Ordered quantity deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "❌ Ordered quantity not found for deletion.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Cannot delete: " + e.getMessage());
        }
        return "redirect:/package/" + packageId + "/plan/" + planId;
    }

    @PostMapping("/{oqId}/update")
    public String updateOrderedQuantity(@PathVariable String packageId,
                                       @PathVariable String planId,
                                       @PathVariable String oqId,
                                       @RequestParam Integer orderedQuota,
                                       RedirectAttributes redirectAttributes) {
        try {
            UpdateOrderedQuantityDto dto = UpdateOrderedQuantityDto.builder()
                    .id(oqId)
                    .orderedQuota(orderedQuota)
                    .build();
            
            orderedQuantityService.updateOrderedQuantity(oqId, orderedQuota);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Ordered quantity updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Update failed: " + e.getMessage());
        }
        return "redirect:/package/" + packageId + "/plan/" + planId;
    }
}