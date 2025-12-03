package apap.ti._5.tour_package_2306165963_be.restcontroller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.plan.*;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.service.PackageService;
import apap.ti._5.tour_package_2306165963_be.service.PlanService;
import apap.ti._5.tour_package_2306165963_be.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PlanRestController {

    @Autowired
    private PlanService planService;

    @Autowired
    private PackageService packageService;

    @Autowired
    private DtoMapper dtoMapper;

    @Autowired
    private JwtUtils jwtUtils;

    // GET Plan by ID - with ownership check (PBI-FE-T14)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @GetMapping("/plans/{id}")
    public ResponseEntity<?> getPlanById(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Plan> planOpt = planService.getPlanById(id);

            if (planOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Plan not found",
                                "timestamp", new Date()));
            }

            Plan plan = planOpt.get();
            Optional<Package> pkgOpt = packageService.getPackageById(plan.getPackageId());

            if (pkgOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Package not found",
                                "timestamp", new Date()));
            }

            Package pkg = pkgOpt.get();

            // RBAC Check - only owner or superadmin/vendor can view
            if (!("Superadmin".equals(role) || "TourPackageVendor".equals(role) ||
                    pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have access to this plan",
                                "timestamp", new Date()));
            }

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Plan retrieved successfully",
                    "timestamp", new Date(),
                    "data", dtoMapper.toReadDto(plan)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // CREATE Plan - with ownership check (PBI-FE-T15)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @PostMapping("/packages/{packageId}/plans")
    public ResponseEntity<?> createPlan(
            @PathVariable String packageId,
            @Valid @RequestBody CreatePlanDto dto,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Package> pkgOpt = packageService.getPackageById(packageId);

            if (pkgOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Package not found",
                                "timestamp", new Date()));
            }

            Package pkg = pkgOpt.get();

            // RBAC Check - only owner or superadmin can create plan
            if (!("Superadmin".equals(role) || pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to create plan for this package",
                                "timestamp", new Date()));
            }

            Plan plan = dtoMapper.toEntity(dto);
            Plan saved = planService.createPlan(packageId, plan);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status", HttpStatus.CREATED.value(),
                            "message", "Plan created successfully",
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

    // UPDATE Plan - with ownership check (PBI-FE-T16)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @PutMapping("/plans/{id}")
    public ResponseEntity<?> updatePlan(
            @PathVariable String id,
            @Valid @RequestBody UpdatePlanDto dto,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Plan> planOpt = planService.getPlanById(id);

            if (planOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Plan not found",
                                "timestamp", new Date()));
            }

            Plan existingPlan = planOpt.get();
            Optional<Package> pkgOpt = packageService.getPackageById(existingPlan.getPackageId());

            if (pkgOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Package not found",
                                "timestamp", new Date()));
            }

            Package pkg = pkgOpt.get();

            // RBAC Check - only owner or superadmin can update
            if (!("Superadmin".equals(role) || pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to update this plan",
                                "timestamp", new Date()));
            }

            dto.setId(id);
            Plan plan = dtoMapper.toEntity(dto);
            Plan updated = planService.updatePlan(plan);

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Plan updated successfully",
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

    // DELETE Plan - with ownership check (PBI-FE-T17)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Plan> planOpt = planService.getPlanById(id);

            if (planOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Plan not found",
                                "timestamp", new Date()));
            }

            Plan plan = planOpt.get();
            Optional<Package> pkgOpt = packageService.getPackageById(plan.getPackageId());

            if (pkgOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Package not found",
                                "timestamp", new Date()));
            }

            Package pkg = pkgOpt.get();

            // RBAC Check - only owner or superadmin can delete
            if (!("Superadmin".equals(role) || pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to delete this plan",
                                "timestamp", new Date()));
            }

            planService.deletePlan(id);

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Plan deleted successfully",
                    "timestamp", new Date()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "status", HttpStatus.CONFLICT.value(),
                            "message", e.getMessage(),
                            "timestamp", new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // PROCESS Plan - with ownership check
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @PostMapping("/plans/{id}/process")
    public ResponseEntity<?> processPlan(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Plan> planOpt = planService.getPlanById(id);

            if (planOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Plan not found",
                                "timestamp", new Date()));
            }

            Plan plan = planOpt.get();
            Optional<Package> pkgOpt = packageService.getPackageById(plan.getPackageId());

            if (pkgOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Package not found",
                                "timestamp", new Date()));
            }

            Package pkg = pkgOpt.get();

            // RBAC Check - only owner or superadmin can process
            if (!("Superadmin".equals(role) || pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to process this plan",
                                "timestamp", new Date()));
            }

            planService.processPlan(id);

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Plan processed successfully",
                    "timestamp", new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", HttpStatus.BAD_REQUEST.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }
}