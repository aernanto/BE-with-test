package apap.ti._5.tour_package_2306165963_be.controller;

import apap.ti._5.tour_package_2306165963_be.dto.activity.*;
import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.service.ActivityService;
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
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping
    public String getAllActivities(Model model, HttpServletRequest request) {
        List<ReadActivityDto> activities = activityService.getAllActivities()
                .stream()
                .map(activity -> {
                return dtoMapper.toReadDto(activity);
            })
            .collect(Collectors.toList());
        
        model.addAttribute("listActivity", activities);
        model.addAttribute("currentUri", request.getRequestURI());
        return "activity/view-all";
    }

    @GetMapping("/{id}")
    public String getActivityById(@PathVariable String id, Model model, HttpServletRequest request) {
        Optional<Activity> activityOptional = activityService.getActivityById(id);

        if (activityOptional.isEmpty()) {
            model.addAttribute("title", "Activity Not Found");
            model.addAttribute("message", "Activity with ID " + id + " not found.");
            return "error/404";
        }

        ReadActivityDto activityDto = dtoMapper.toReadDto(activityOptional.get());
        model.addAttribute("activity", activityDto);
        model.addAttribute("currentUri", request.getRequestURI());
        return "activity/detail";
    }

    @GetMapping("/create")
    public String formCreateActivity(Model model, HttpServletRequest request) {
        model.addAttribute("isEdit", false);
        model.addAttribute("activityData", new CreateActivityDto());
        model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle Rental"});
        model.addAttribute("currentUri", request.getRequestURI());
        return "activity/form";
    }

    @PostMapping("/create")
    public String createActivity(@Valid @ModelAttribute("activityData") CreateActivityDto activityDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model,
                                 HttpServletRequest request) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("activityData", activityDto);
            model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle Rental"});
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("errorMessage", "❌ Please fix the validation errors.");
            return "activity/form";
        }
        
        try {
            Activity activity = dtoMapper.toEntity(activityDto);
            activityService.createActivity(activity);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Activity created successfully!");
            return "redirect:/activities";
        } catch (Exception e) {
            model.addAttribute("isEdit", false);
            model.addAttribute("activityData", activityDto);
            model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle Rental"});
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("errorMessage", "❌ " + e.getMessage());
            return "activity/form";
        }
    }

    @GetMapping("/update/{id}")
    public String formEditActivity(@PathVariable String id, Model model, HttpServletRequest request) {
        Optional<Activity> activityOptional = activityService.getActivityById(id);

        if (activityOptional.isEmpty()) {
            model.addAttribute("title", "Activity Not Found");
            model.addAttribute("message", "Activity with ID " + id + " not found.");
            return "error/404";
        }

        UpdateActivityDto activityDto = dtoMapper.toUpdateDto(activityOptional.get());
        model.addAttribute("isEdit", true);
        model.addAttribute("activityId", id);
        model.addAttribute("activityData", activityDto);
        model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle Rental"});
        model.addAttribute("currentUri", request.getRequestURI());
        return "activity/form";
    }

    @PostMapping("/update/{id}")
    public String updateActivity(@PathVariable String id,
                                @Valid @ModelAttribute("activityData") UpdateActivityDto activityDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                HttpServletRequest request) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("activityId", id);
            model.addAttribute("activityData", activityDto);
            model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle Rental"});
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("errorMessage", "❌ Please fix the validation errors.");
            return "activity/form";
        }
        
        try {
            activityDto.setId(id);
            Activity activity = dtoMapper.toEntity(activityDto);
            activityService.updateActivity(activity);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Activity updated successfully!");
            return "redirect:/activities/" + id;
        } catch (Exception e) {
            model.addAttribute("isEdit", true);
            model.addAttribute("activityId", id);
            model.addAttribute("activityData", activityDto);
            model.addAttribute("activityTypes", new String[]{"Flight", "Accommodation", "Vehicle Rental"});
            model.addAttribute("currentUri", request.getRequestURI());
            model.addAttribute("errorMessage", "❌ " + e.getMessage());
            return "activity/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteActivity(@PathVariable String id, RedirectAttributes redirectAttributes) {
        boolean removed = activityService.deleteActivity(id);

        if (removed) {
            redirectAttributes.addFlashAttribute("successMessage", "✅ Activity deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Activity not found for deletion.");
        }
        return "redirect:/activities";
    }
}