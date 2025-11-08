package apap.ti._5.tour_package_2306165963_be.dto.packagedto;

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
public class UpdatePackageDto {
    
    @NotBlank(message = "Package ID is required")
    private String id;
    
    @NotBlank(message = "User ID is required")
    @Size(min = 3, max = 50, message = "User ID must be between 3 and 50 characters")
    private String userId;
    
    @NotBlank(message = "Package name is required")
    @Size(min = 5, max = 150, message = "Package name must be between 5 and 150 characters")
    private String packageName;
    
    @NotNull(message = "Quota is required")
    @Min(value = 1, message = "Quota must be at least 1")
    @Max(value = 500, message = "Quota cannot exceed 500")
    private Integer quota;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Long price;
    
    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
}