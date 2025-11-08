package apap.ti._5.tour_package_2306165963_be.restcontroller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.orderedquantity.*;
import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.service.OrderedQuantityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class OrderedQuantityRestController {

    @Autowired
    private OrderedQuantityService orderedQuantityService;

    @Autowired
    private DtoMapper dtoMapper;

    // GET /api/ordered-quantities/{id}
    @GetMapping("/ordered-quantities/{id}")
    public ResponseEntity<ReadOrderedQuantityDto> getById(@PathVariable String id) {
        Optional<OrderedQuantity> oq = orderedQuantityService.getOrderedQuantityById(id);
        return oq.map(quantity -> ResponseEntity.ok(dtoMapper.toReadDto(quantity)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // POST /api/plans/{planId}/ordered-quantities
    @PostMapping("/plans/{planId}/ordered-quantities")
    public ResponseEntity<ReadOrderedQuantityDto> create(
            @PathVariable String planId,
            @Valid @RequestBody CreateOrderedQuantityDto dto) {

        OrderedQuantity oq = dtoMapper.toEntity(dto);
        OrderedQuantity saved = orderedQuantityService.createOrderedQuantity(planId, oq);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toReadDto(saved));
    }

    // PUT /api/ordered-quantities/{id}?newQuota=5
    @PutMapping("/ordered-quantities/{id}")
    public ResponseEntity<ReadOrderedQuantityDto> update(
            @PathVariable String id,
            @RequestParam("newQuota") Integer newQuota) {

        OrderedQuantity updated = orderedQuantityService.updateOrderedQuantity(id, newQuota);
        return ResponseEntity.ok(dtoMapper.toReadDto(updated));
    }

    // DELETE /api/ordered-quantities/{id}
    @DeleteMapping("/ordered-quantities/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = orderedQuantityService.deleteOrderedQuantity(id);
        return deleted ? ResponseEntity.noContent().build()
                       : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}