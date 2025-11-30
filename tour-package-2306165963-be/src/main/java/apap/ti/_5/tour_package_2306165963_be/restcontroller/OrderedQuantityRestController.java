package apap.ti._5.tour_package_2306165963_be.restcontroller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.orderedquantity.*;
import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.service.OrderedQuantityService;
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
public class OrderedQuantityRestController {

    @Autowired
    private OrderedQuantityService orderedQuantityService;

    @Autowired
    private PlanService planService;

    @Autowired
    private PackageService packageService;

    @Autowired
    private DtoMapper dtoMapper;

    @Autowired
    private JwtUtils jwtUtils;

    // GET OrderedQuantity by ID - with ownership check (PBI-FE-T18)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @GetMapping("/ordered-quantities/{id}")
    public ResponseEntity<?> getById(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<OrderedQuantity> oqOpt = orderedQuantityService.getOrderedQuantityById(id);

            if (oqOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "OrderedQuantity not found",
                                "timestamp", new Date()));
            }

            OrderedQuantity oq = oqOpt.get();
            Optional<Plan> planOpt = planService.getPlanById(oq.getPlanId().toString());

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
                                "message", "You don't have access to this ordered quantity",
                                "timestamp", new Date()));
            }

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "OrderedQuantity retrieved successfully",
                    "timestamp", new Date(),
                    "data", dtoMapper.toReadDto(oq)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // CREATE OrderedQuantity - with ownership check (PBI-FE-T19)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @PostMapping("/plans/{planId}/ordered-quantities")
    public ResponseEntity<?> create(
            @PathVariable String planId,
            @Valid @RequestBody CreateOrderedQuantityDto dto,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Plan> planOpt = planService.getPlanById(planId);

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

            // RBAC Check - only owner or superadmin can create
            if (!("Superadmin".equals(role) || pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to create ordered quantity for this plan",
                                "timestamp", new Date()));
            }

            OrderedQuantity oq = dtoMapper.toEntity(dto);
            OrderedQuantity saved = orderedQuantityService.createOrderedQuantity(planId, oq);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status", HttpStatus.CREATED.value(),
                            "message", "OrderedQuantity created successfully",
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

    // UPDATE OrderedQuantity quota - with ownership check (PBI-FE-T20)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @PutMapping("/ordered-quantities/{id}")
    public ResponseEntity<?> update(
            @PathVariable String id,
            @RequestParam("newQuota") Integer newQuota,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<OrderedQuantity> oqOpt = orderedQuantityService.getOrderedQuantityById(id);

            if (oqOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "OrderedQuantity not found",
                                "timestamp", new Date()));
            }

            OrderedQuantity oq = oqOpt.get();
            Optional<Plan> planOpt = planService.getPlanById(oq.getPlanId().toString());

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

            // RBAC Check - only owner or superadmin can update
            if (!("Superadmin".equals(role) || pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to update this ordered quantity",
                                "timestamp", new Date()));
            }

            OrderedQuantity updated = orderedQuantityService.updateOrderedQuantity(id, newQuota);

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "OrderedQuantity updated successfully",
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

    // DELETE OrderedQuantity - with ownership check (PBI-FE-T21)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @DeleteMapping("/ordered-quantities/{id}")
    public ResponseEntity<?> delete(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<OrderedQuantity> oqOpt = orderedQuantityService.getOrderedQuantityById(id);

            if (oqOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "OrderedQuantity not found",
                                "timestamp", new Date()));
            }

            OrderedQuantity oq = oqOpt.get();
            Optional<Plan> planOpt = planService.getPlanById(oq.getPlanId().toString());

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
                                "message", "You don't have permission to delete this ordered quantity",
                                "timestamp", new Date()));
            }

            boolean deleted = orderedQuantityService.deleteOrderedQuantity(id);

            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "status", HttpStatus.OK.value(),
                        "message", "OrderedQuantity deleted successfully",
                        "timestamp", new Date()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "OrderedQuantity not found",
                                "timestamp", new Date()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }
}