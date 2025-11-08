package apap.ti._5.tour_package_2306165963_be.dto.plan;

import apap.ti._5.tour_package_2306165963_be.dto.orderedquantity.ReadOrderedQuantityDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadPlanDto {
    
    private String id;
    private String packageId;
    private Long price;
    private String activityType;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String startLocation;
    private String endLocation;
    
    @Builder.Default
    private List<ReadOrderedQuantityDto> orderedQuantities = new ArrayList<>();
    
    // Computed fields
    public String getFormattedPrice() {
        if (this.price == null) return "Rp 0";
        return String.format("Rp %,d", this.price);
    }
    
    public String getActivityTypeIcon() {
        if (activityType == null) return "📋";
        switch (activityType.toLowerCase()) {
            case "flight": return "✈️";
            case "accommodation": return "🏨";
            case "vehicle": return "🚗";
            default: return "📋";
        }
    }
    
    public String getFormattedStartDate() {
        if (this.startDate == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        return this.startDate.format(formatter);
    }
    
    public String getFormattedEndDate() {
        if (this.endDate == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        return this.endDate.format(formatter);
    }
    
    public String getStatusBadgeClass() {
        if ("Processed".equals(status)) return "status-processed";
        if ("Pending".equals(status)) return "status-pending";
        if ("Unfinished".equals(status)) return "status-unfinished";
        return "status-default";
    }
    
    public boolean canBeEdited() {
        return !"Processed".equals(this.status);
    }
}