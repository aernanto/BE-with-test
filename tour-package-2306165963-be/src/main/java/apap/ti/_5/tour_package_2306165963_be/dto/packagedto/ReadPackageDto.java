package apap.ti._5.tour_package_2306165963_be.dto.packagedto;

import apap.ti._5.tour_package_2306165963_be.dto.plan.ReadPlanDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadPackageDto {
    
    private String id;
    private String userId;
    private String packageName;
    private Integer quota;
    private Long price;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    @Builder.Default
    private List<ReadPlanDto> plans = new ArrayList<>();
    
    // Computed fields
    public boolean canBeEdited() {
        return "Pending".equals(this.status);
    }
    
    public String getFormattedStartDate() {
        if (this.startDate == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        return this.startDate.format(formatter);
    }
    
    public String getFormattedEndDate() {
        if (this.endDate == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        return this.endDate.format(formatter);
    }
    
    public String getFormattedPrice() {
        if (this.price == null) return "Rp 0";
        return String.format("Rp %,d", this.price);
    }
    
    public int getTotalPlans() {
        return plans != null ? plans.size() : 0;
    }
    
    public String getDuration() {
        if (startDate == null || endDate == null) return "N/A";
        
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return String.format("%d day%s", days, days != 1 ? "s" : "");
    }
    
    public String getStatusBadgeClass() {
        if ("Processed".equals(status)) return "status-processed";
        if ("Pending".equals(status)) return "status-pending";
        return "status-default";
    }
}