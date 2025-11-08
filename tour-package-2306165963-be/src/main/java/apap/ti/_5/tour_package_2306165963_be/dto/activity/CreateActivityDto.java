package apap.ti._5.tour_package_2306165963_be.dto.activity;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateActivityDto {
    
    @NotBlank(message = "Activity name is required")
    @Size(min = 3, max = 100, message = "Activity name must be between 3 and 100 characters")
    private String activityName;
    
    @NotBlank(message = "Activity type is required")
    private String activityType;
    
    @NotBlank(message = "Activity item is required")
    @Size(min = 3, max = 200, message = "Activity item must be between 3 and 200 characters")
    private String activityItem;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 1000, message = "Capacity cannot exceed 1000")
    private Integer capacity;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Long price;
    
    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    
    @NotBlank(message = "Start location is required")
    @Size(min = 3, max = 100, message = "Start location must be between 3 and 100 characters")
    private String startLocation;
    
    @NotBlank(message = "End location is required")
    @Size(min = 3, max = 100, message = "End location must be between 3 and 100 characters")
    private String endLocation;
}