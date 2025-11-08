package apap.ti._5.tour_package_2306165963_be.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    
    @Id
    private String id;
    
    // @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "package_id")
    private String packageId;
    
    private Long price;
    
    @Column(name = "activity_type")
    private String activityType; // Flight, Accommodation, Vehicle
    
    private String status; // Unfinished, Pending, Processed
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "start_location")
    private String startLocation;
    
    @Column(name = "end_location")
    private String endLocation;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "plan_id", referencedColumnName = "id")
@Builder.Default
private List<OrderedQuantity> orderedQuantities = new ArrayList<>();
    
    @Transient
    public String getFormattedPrice() {
        if (this.price == null) return "Rp 0";
        return String.format("Rp %,d", this.price);
    }
    
    @Transient
    public String getActivityTypeIcon() {
        if (activityType == null) return "📋";
        switch (activityType.toLowerCase()) {
            case "flight": return "✈️";
            case "accommodation": return "🏨";
            case "vehicle": return "🚗";
            default: return "📋";
        }
    }
}