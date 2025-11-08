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
public class CreateOrderedQuantityDto {
    
    @NotBlank(message = "Activity ID is required")
    private String activityId;
    
    @NotNull(message = "Ordered quantity is required")
    @Min(value = 1, message = "Ordered quantity must be at least 1")
    private Integer orderedQuota;
}