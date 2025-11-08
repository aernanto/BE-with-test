package apap.ti._5.tour_package_2306165963_be.dto.orderedquantity;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderedQuantityDto {
    
    @NotBlank(message = "Ordered Quantity ID is required")
    private String id;
    
    @NotNull(message = "Ordered quantity is required")
    @Min(value = 1, message = "Ordered quantity must be at least 1")
    private Integer orderedQuota;
}