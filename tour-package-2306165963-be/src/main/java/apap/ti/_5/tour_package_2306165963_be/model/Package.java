package apap.ti._5.tour_package_2306165963_be.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Package {
    
    @Id
    private String id;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "package_name")
    private String packageName;
    
    private int quota;
    
    private Long price;
    
    private String status; 
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    // ✅ HAPUS mappedBy, pakai @JoinColumn di sini
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    @Builder.Default
    private List<Plan> plans = new ArrayList<>();
    
    @Transient
    public boolean canBeEdited() {
        return "Pending".equals(this.status);
    }
    
    @Transient
    public String getFormattedStartDate() {
        if (this.startDate == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        return this.startDate.format(formatter);
    }
    
    @Transient
    public String getFormattedEndDate() {
        if (this.endDate == null) return "-";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        return this.endDate.format(formatter);
    }
    
    @Transient
    public String getFormattedPrice() {
        if (this.price == null) return "Rp 0";
        return String.format("Rp %,d", this.price);
    }
    
    @Transient
    public int getTotalPlans() {
        return plans != null ? plans.size() : 0;
    }
}