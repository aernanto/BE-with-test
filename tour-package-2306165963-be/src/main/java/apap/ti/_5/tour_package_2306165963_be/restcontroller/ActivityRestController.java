// src/main/java/apap/ti/_5/tour_package_2306165963_be/restcontroller/ActivityRestController.java
package apap.ti._5.tour_package_2306165963_be.restcontroller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.activity.*;
import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.service.ActivityService;
import apap.ti._5.tour_package_2306165963_be.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
public class ActivityRestController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private DtoMapper dtoMapper;

    @Autowired
    private JwtUtils jwtUtils;

    // GET ALL - Semua authenticated user bisa lihat activities
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor', 'FlightAirline', 'AccomodationOwner', 'RentalVendor')")
    @GetMapping
    public ResponseEntity<?> getAllActivities() {
        try {
            List<ReadActivityDto> activities = activityService.getAllActivities()
                    .stream()
                    .map(dtoMapper::toReadDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Berhasil mendapatkan daftar activities",
                    "timestamp", new Date(),
                    "data", activities));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // GET BY ID
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor', 'FlightAirline', 'AccomodationOwner', 'RentalVendor')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityById(@PathVariable String id) {
        try {
            Optional<Activity> activity = activityService.getActivityById(id);

            if (activity.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Activity not found",
                                "timestamp", new Date()));
            }

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Berhasil mendapatkan detail activity",
                    "timestamp", new Date(),
                    "data", dtoMapper.toReadDto(activity.get())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // CREATE - Hanya vendor dan superadmin (PBI-FE-T3)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'TourPackageVendor', 'FlightAirline', 'AccomodationOwner', 'RentalVendor')")
    @PostMapping
    public ResponseEntity<?> createActivity(@Valid @RequestBody CreateActivityDto dto,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String vendorId = jwtUtils.getIdFromJwtToken(jwt);

            Activity activity = dtoMapper.toEntity(dto);
            // Set vendor ID from token for ownership tracking
            activity.setVendorId(vendorId);
            Activity saved = activityService.createActivity(activity);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status", HttpStatus.CREATED.value(),
                            "message", "Activity berhasil dibuat",
                            "timestamp", new Date(),
                            "data", dtoMapper.toReadDto(saved)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", HttpStatus.BAD_REQUEST.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // UPDATE - Vendor can only update their own, Superadmin can update all
    // (PBI-FE-T4)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'TourPackageVendor', 'FlightAirline', 'AccomodationOwner', 'RentalVendor')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivity(@PathVariable String id,
            @Valid @RequestBody UpdateActivityDto dto,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String vendorId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Activity> activityOpt = activityService.getActivityById(id);

            if (activityOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Activity not found",
                                "timestamp", new Date()));
            }

            Activity existingActivity = activityOpt.get();

            // RBAC Check - only owner or superadmin can update
            if (!("Superadmin".equals(role) || existingActivity.getVendorId().equals(vendorId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to update this activity",
                                "timestamp", new Date()));
            }

            dto.setId(id);
            Activity activity = dtoMapper.toEntity(dto);
            Activity updated = activityService.updateActivity(activity);

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Activity berhasil diupdate",
                    "timestamp", new Date(),
                    "data", dtoMapper.toReadDto(updated)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", HttpStatus.BAD_REQUEST.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // DELETE - Vendor can delete their own, Superadmin can delete all (PBI-FE-T5)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'TourPackageVendor', 'FlightAirline', 'AccomodationOwner', 'RentalVendor')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String vendorId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Activity> activityOpt = activityService.getActivityById(id);

            if (activityOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Activity not found",
                                "timestamp", new Date()));
            }

            Activity existingActivity = activityOpt.get();

            // RBAC Check - only owner or superadmin can delete
            if (!("Superadmin".equals(role) || existingActivity.getVendorId().equals(vendorId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to delete this activity",
                                "timestamp", new Date()));
            }

            activityService.deleteActivity(id);

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Activity berhasil dihapus",
                    "timestamp", new Date()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "status", HttpStatus.CONFLICT.value(),
                            "message", e.getMessage(),
                            "timestamp", new Date()));
        }
    }
}