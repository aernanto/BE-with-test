// src/main/java/apap/ti/_5/tour_package_2306165963_be/restcontroller/PackageRestController.java
package apap.ti._5.tour_package_2306165963_be.restcontroller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.packagedto.*;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.service.PackageService;
import apap.ti._5.tour_package_2306165963_be.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/packages")
public class PackageRestController {

    @Autowired
    private PackageService packageService;

    @Autowired
    private DtoMapper dtoMapper;

    @Autowired
    private JwtUtils jwtUtils;

    // GET ALL - Superadmin, Customer, TourPackageVendor dapat akses
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @GetMapping
    public ResponseEntity<?> getAllPackages(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            List<ReadPackageDto> packages;

            if ("Superadmin".equals(role) || "TourPackageVendor".equals(role)) {
                // Superadmin dan Vendor bisa lihat semua
                packages = packageService.getAllPackages()
                        .stream()
                        .map(dtoMapper::toReadDto)
                        .collect(Collectors.toList());
            } else {
                // Customer hanya lihat paket miliknya
                packages = packageService.getPackagesByUserId(userId)
                        .stream()
                        .map(dtoMapper::toReadDto)
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Berhasil mendapatkan daftar packages",
                    "timestamp", new Date(),
                    "data", packages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // GET BY ID - Dengan validasi ownership
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getPackageById(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Package> pkgOpt = packageService.getPackageById(id);

            if (pkgOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Package not found",
                                "timestamp", new Date()));
            }

            Package pkg = pkgOpt.get();

            // RBAC Check
            if (!("Superadmin".equals(role) ||
                    "TourPackageVendor".equals(role) ||
                    pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have access to this package",
                                "timestamp", new Date()));
            }

            ReadPackageDto packageDto = dtoMapper.toReadDto(pkg);

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Berhasil mendapatkan detail package",
                    "timestamp", new Date(),
                    "data", packageDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // CREATE - Customer, TourPackageVendor, and Superadmin can create (PBI-FE-T9)
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @PostMapping
    public ResponseEntity<?> createPackage(@Valid @RequestBody CreatePackageDto dto,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);

            // Set userId from token
            dto.setUserId(userId);

            Package pkg = dtoMapper.toEntity(dto);
            Package saved = packageService.createPackage(pkg);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status", HttpStatus.CREATED.value(),
                            "message", "Package berhasil dibuat",
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

    // UPDATE - Owner atau Superadmin
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePackage(@PathVariable String id,
            @Valid @RequestBody UpdatePackageDto dto,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Package> pkgOpt = packageService.getPackageById(id);

            if (pkgOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Package not found",
                                "timestamp", new Date()));
            }

            Package pkg = pkgOpt.get();

            // RBAC Check - hanya owner atau superadmin yang bisa update
            if (!("Superadmin".equals(role) || pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to update this package",
                                "timestamp", new Date()));
            }

            dto.setId(id);
            Package updated = packageService.updatePackage(dtoMapper.toEntity(dto));

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Package berhasil diupdate",
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

    // DELETE - Owner atau Superadmin
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePackage(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Package> pkgOpt = packageService.getPackageById(id);

            if (pkgOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Package not found",
                                "timestamp", new Date()));
            }

            Package pkg = pkgOpt.get();

            // RBAC Check
            if (!("Superadmin".equals(role) || pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to delete this package",
                                "timestamp", new Date()));
            }

            packageService.deletePackage(id);

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Package berhasil dihapus",
                    "timestamp", new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "status", HttpStatus.CONFLICT.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // PROCESS - Owner atau Superadmin
    @PreAuthorize("hasAnyAuthority('Superadmin', 'Customer', 'TourPackageVendor')")
    @PostMapping("/{id}/process")
    public ResponseEntity<?> processPackage(@PathVariable String id,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtils.getIdFromJwtToken(jwt);
            String role = jwtUtils.getRoleFromJwtToken(jwt);

            Optional<Package> pkgOpt = packageService.getPackageById(id);

            if (pkgOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", HttpStatus.NOT_FOUND.value(),
                                "message", "Package not found",
                                "timestamp", new Date()));
            }

            Package pkg = pkgOpt.get();

            // RBAC Check
            if (!("Superadmin".equals(role) || pkg.getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", HttpStatus.FORBIDDEN.value(),
                                "message", "You don't have permission to process this package",
                                "timestamp", new Date()));
            }

            packageService.processPackage(id);

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Package berhasil diproses",
                    "timestamp", new Date()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", HttpStatus.BAD_REQUEST.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // GET BY USER - Get packages by specific user
    @PreAuthorize("hasAuthority('Superadmin')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPackagesByUser(@PathVariable String userId) {
        try {
            List<ReadPackageDto> packages = packageService.getPackagesByUserId(userId)
                    .stream()
                    .map(dtoMapper::toReadDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Berhasil mendapatkan packages by user",
                    "timestamp", new Date(),
                    "data", packages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }

    // GET BY STATUS - Filter by status
    @PreAuthorize("hasAnyAuthority('Superadmin', 'TourPackageVendor')")
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPackagesByStatus(@PathVariable String status) {
        try {
            List<ReadPackageDto> packages = packageService.getPackagesByStatus(status)
                    .stream()
                    .map(dtoMapper::toReadDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "status", HttpStatus.OK.value(),
                    "message", "Berhasil mendapatkan packages by status",
                    "timestamp", new Date(),
                    "data", packages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Error: " + e.getMessage(),
                            "timestamp", new Date()));
        }
    }
}