package apap.ti._5.tour_package_2306165963_be.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadActivityDto {
    private String id;
    private String activityName;
    private String activityItem;
    private Integer capacity;
    private Long price;
    private String activityType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String startLocation;
    private String endLocation;

    // DIHITUNG SAAT DIPANGGIL
    public String getFormattedPrice() {
        if (price == null) return "Rp 0";
        return String.format("Rp %,d", price);
    }

    public String getActivityTypeIcon() {
        if (activityType == null) return "Icon";
        return switch (activityType.toLowerCase()) {
            case "flight" -> "Airplane";
            case "accommodation" -> "Hotel";
            case "vehicle rental" -> "Car";
            default -> "Clipboard";
        };
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