package apap.ti._5.tour_package_2306165963_be.controller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.plan.*;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
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

import java.util.Optional;

@Controller
@RequestMapping("/package/{packageId}/plan")
public class PlanController {

    @Autowired
    private PlanService planService;
    
    @Autowired
    private PackageService packageService;
    
    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping("/{planId}")
    public String getPlanById(@PathVariable String packageId, 
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

        Optional<Plan> planOptional = planService.getPlanById(planId);
        if (planOptional.isEmpty()) {
            model.addAttribute("title", "Plan Not Found");
            model.addAttribute("message", "Plan with ID " + planId + " not found.");
            return "error/404";
        }

        ReadPlanDto planDto = dtoMapper.toReadDto(planOptional.get());
        model.addAttribute("packageId", packageId);
        model.addAttribute("planData", planDto);
        model.addAttribute("currentUri", request.getRequestURI());
        return "plan/detail";
    }

    @GetMapping("/create")
    public String formCreatePlan(@PathVariable String packageId, 
                                Model model, 
                                HttpServletRequest request) {
        
        // Verify package exists
        Optional<Package> packageOptional = packageService.getPackageById(packageId);
        if (packageOptional.isEmpty()) {
            model.addAttribute("title", "Package Not Found");
            model.addAttribute("message", "Package with ID " + packageId + " not found.");
            return "error/404";
        }

        Package packageEntity = packageOptional.get();
        
        // Check if package can be edited
        if ("Processed".equals(packageEntity.getStatus())) {
            model.addAttribute("title", "Cannot Add Plan");
            model.addAttribute("message", "Cannot add plan to processed package.");
            return "error/403";
        }

        model.addAttribute("isEdit", false);
        model.addAttribute("packageId", packageId);
        model.addAttribute("currentPackage", packageEntity);
        model.addAttribute("planData", new CreatePlanDto());
        model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle"});
        model.addAttribute("currentUri", request.getRequestURI());
        return "plan/form";
    }

    @PostMapping("/create")
    public String createPlan(@PathVariable String packageId,
                            @Valid @ModelAttribute("planData") CreatePlanDto planDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model,
                            HttpServletRequest request) {
        
        if (bindingResult.hasErrors()) {
            Optional<Package> packageOptional = packageService.getPackageById(packageId);
            model.addAttribute("isEdit", false);
            model.addAttribute("packageId", packageId);
            model.addAttribute("currentPackage", packageOptional.get());
            model.addAttribute("planData", planDto);
            model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle"});
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("errorMessage", "❌ Please fix the validation errors.");
            return "plan/form";
        }
        
        try {
            Plan plan = dtoMapper.toEntity(planDto);
            planService.createPlan(packageId, plan);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Plan created successfully!");
            return "redirect:/package/" + packageId;
        } catch (Exception e) {
            Optional<Package> packageOptional = packageService.getPackageById(packageId);
            model.addAttribute("isEdit", false);
            model.addAttribute("packageId", packageId);
            model.addAttribute("currentPackage", packageOptional.get());
            model.addAttribute("planData", planDto);
            model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle"});
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("errorMessage", "❌ " + e.getMessage());
            return "plan/form";
        }
    }

    @GetMapping("/{planId}/update")
    public String formEditPlan(@PathVariable String packageId, 
                              @PathVariable String planId, 
                              Model model, 
                              HttpServletRequest request) {
        
        Optional<Package> packageOptional = packageService.getPackageById(packageId);
        if (packageOptional.isEmpty()) {
            model.addAttribute("title", "Package Not Found");
            model.addAttribute("message", "Package with ID " + packageId + " not found.");
            return "error/404";
        }

        Optional<Plan> planOptional = planService.getPlanById(planId);
        if (planOptional.isEmpty()) {
            model.addAttribute("title", "Plan Not Found");
            model.addAttribute("message", "Plan with ID " + planId + " not found.");
            return "error/404";
        }

        Plan plan = planOptional.get();
        
        // Check if can be edited
        if ("Processed".equals(plan.getStatus())) {
            model.addAttribute("title", "Cannot Edit Plan");
            model.addAttribute("message", "Processed plans cannot be edited.");
            return "error/403";
        }

        UpdatePlanDto planDto = dtoMapper.toUpdateDto(plan);
        model.addAttribute("isEdit", true);
        model.addAttribute("packageId", packageId);
        model.addAttribute("planId", planId);
        model.addAttribute("currentPackage", packageOptional.get());
        model.addAttribute("planData", planDto);
        model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle"});
        model.addAttribute("currentUri", request.getRequestURI());
        return "plan/form";
    }

    @PostMapping("/{planId}/update")
    public String updatePlan(@PathVariable String packageId,
                            @PathVariable String planId,
                            @Valid @ModelAttribute("planData") UpdatePlanDto planDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model,
                            HttpServletRequest request) {
        
        if (bindingResult.hasErrors()) {
            Optional<Package> packageOptional = packageService.getPackageById(packageId);
            model.addAttribute("isEdit", true);
            model.addAttribute("packageId", packageId);
            model.addAttribute("planId", planId);
            model.addAttribute("currentPackage", packageOptional.get());
            model.addAttribute("planData", planDto);
            model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle"});
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("errorMessage", "❌ Please fix the validation errors.");
            return "plan/form";
        }
        
        try {
            planDto.setId(planId);
            Plan plan = dtoMapper.toEntity(planDto);
            planService.updatePlan(plan);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Plan updated successfully!");
            return "redirect:/package/" + packageId + "/plan/" + planId;
        } catch (Exception e) {
            Optional<Package> packageOptional = packageService.getPackageById(packageId);
            model.addAttribute("isEdit", true);
            model.addAttribute("packageId", packageId);
            model.addAttribute("planId", planId);
            model.addAttribute("currentPackage", packageOptional.get());
            model.addAttribute("planData", planDto);
            model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle"});
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("errorMessage", "❌ " + e.getMessage());
            return "plan/form";
        }
    }

    @PostMapping("/{planId}/delete")
    public String deletePlan(@PathVariable String packageId, 
                            @PathVariable String planId, 
                            RedirectAttributes redirectAttributes) {
        try {
            boolean removed = planService.deletePlan(planId);
            
            if (removed) {
                redirectAttributes.addFlashAttribute("successMessage", "✅ Plan deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "❌ Plan not found for deletion.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Cannot delete plan: " + e.getMessage());
        }
        return "redirect:/package/" + packageId;
    }

    @PostMapping("/{planId}/process")
    public String processPlan(@PathVariable String packageId, 
                             @PathVariable String planId, 
                             RedirectAttributes redirectAttributes) {
        try {
            planService.processPlan(planId);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Plan processed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Failed to process plan: " + e.getMessage());
        }
        return "redirect:/package/" + packageId + "/plan/" + planId;
    }
}