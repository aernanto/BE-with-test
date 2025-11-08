package apap.ti._5.tour_package_2306165963_be.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activities")
public class Activity {
    
    @Id  
    private String id;
    
    @Column(nullable = false, length = 100)
    private String activityName;
    
    @Column(nullable = false, length = 200)
    private String activityItem; 
    
    @Column(nullable = false)
    private int capacity; 
    
    @Column(nullable = false)
    private Long price;
    
    @Column(nullable = false)
    private String activityType; 

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    
    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    
    @Column(nullable = false, length = 100)
    private String startLocation;
    
    @Column(nullable = false, length = 100)
    private String endLocation;
        
    public String getFormattedPrice() {
        if (this.price == null) return "Rp 0";
        return String.format("Rp %,d", this.price);
    }
    
    public String getActivityTypeIcon() {
        if (activityType == null) return "📋";
        switch (activityType.toLowerCase()) {
            case "flight": return "✈️";
            case "accommodation": return "🏨";
            case "vehicle rental": return "🚗"; 
            default: return "📋";
        }
    }

    public String getDuration() {
        if (startDate == null || endDate == null) return "N/A";
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        long hours = ChronoUnit.HOURS.between(startDate, endDate) % 24;
        if (days > 0) {
            return String.format("%d day%s %d hour%s", days, days > 1 ? "s" : "", hours, hours > 1 ? "s" : "");
        } else {
            return String.format("%d hour%s", hours, hours > 1 ? "s" : "");
        }
    }
}