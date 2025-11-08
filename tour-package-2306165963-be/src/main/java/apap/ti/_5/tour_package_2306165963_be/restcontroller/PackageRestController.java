package apap.ti._5.tour_package_2306165963_be.restcontroller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.packagedto.*;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.service.PackageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/packages")
public class PackageRestController {

    @Autowired
    private PackageService packageService;

    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping
    public List<ReadPackageDto> getAllPackages() {
        return packageService.getAllPackages()
                .stream()
                .map(dtoMapper::toReadDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadPackageDto> getPackageById(@PathVariable String id) {
        Optional<Package> pkg = packageService.getPackageById(id);
        return pkg.map(p -> ResponseEntity.ok(dtoMapper.toReadDto(p)))
                  .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<ReadPackageDto> createPackage(@Valid @RequestBody CreatePackageDto dto) {
        Package pkg = dtoMapper.toEntity(dto);
        Package saved = packageService.createPackage(pkg);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toReadDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReadPackageDto> updatePackage(@PathVariable String id,
                                                        @Valid @RequestBody UpdatePackageDto dto) {
        dto.setId(id);
        Package pkg = dtoMapper.toEntity(dto);
        Package updated = packageService.updatePackage(pkg);
        return ResponseEntity.ok(dtoMapper.toReadDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable String id) {
        try {
            packageService.deletePackage(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<Void> processPackage(@PathVariable String id) {
        try {
            packageService.processPackage(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/user/{userId}")
    public List<ReadPackageDto> getPackagesByUser(@PathVariable String userId) {
        return packageService.getPackagesByUserId(userId)
                .stream()
                .map(dtoMapper::toReadDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/status/{status}")
    public List<ReadPackageDto> getPackagesByStatus(@PathVariable String status) {
        return packageService.getPackagesByStatus(status)
                .stream()
                .map(dtoMapper::toReadDto)
                .collect(Collectors.toList());
    }
}