package apap.ti._5.tour_package_2306165963_be.restcontroller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.plan.*;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PlanRestController {

    @Autowired
    private PlanService planService;

    @Autowired
    private DtoMapper dtoMapper;

    @GetMapping("/plans/{id}")
    public ResponseEntity<ReadPlanDto> getPlanById(@PathVariable String id) {
        Optional<Plan> plan = planService.getPlanById(id);
        return plan.map(p -> ResponseEntity.ok(dtoMapper.toReadDto(p)))
                   .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/packages/{packageId}/plans")
    public ResponseEntity<ReadPlanDto> createPlan(
            @PathVariable String packageId,
            @Valid @RequestBody CreatePlanDto dto) {

        Plan plan = dtoMapper.toEntity(dto);
        Plan saved = planService.createPlan(packageId, plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toReadDto(saved));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<ReadPlanDto> updatePlan(
            @PathVariable String id,
            @Valid @RequestBody UpdatePlanDto dto) {

        dto.setId(id);
        Plan plan = dtoMapper.toEntity(dto);
        Plan updated = planService.updatePlan(plan);
        return ResponseEntity.ok(dtoMapper.toReadDto(updated));
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable String id) {
        try {
            planService.deletePlan(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/plans/{id}/process")
    public ResponseEntity<Void> processPlan(@PathVariable String id) {
        try {
            planService.processPlan(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}