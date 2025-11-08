package apap.ti._5.tour_package_2306165963_be.dto.orderedquantity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadOrderedQuantityDto {
    
    private String id;
    private String planId;
    private String activityId;
    private Integer orderedQuota;
    private Integer quota;
    private Long price;
    private String activityName;
    private String activityItem;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    // Computed fields
    public Long getTotalPrice() {
        if (this.price == null) return 0L;
        return this.price * this.orderedQuota;
    }
    
    public String getFormattedPrice() {
        if (this.price == null) return "Rp 0";
        return String.format("Rp %,d", this.price);
    }
    
    public String getFormattedTotalPrice() {
        return String.format("Rp %,d", getTotalPrice());
    }
    
    public String getFormattedSchedule() {
        if (startDate == null || endDate == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        return startDate.format(formatter) + " - " + endDate.format(formatter);
    }
    
    public String getQuotaInfo() {
        return String.format("%d / %d", orderedQuota, quota);
    }
}