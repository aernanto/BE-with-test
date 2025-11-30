package apap.ti._5.tour_package_2306165963_be.dto.loyalty;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedCouponResponseDTO {

    private UUID id;
    private String code;
    private UUID couponId;
    private String couponName;
    private Integer points;
    private Integer percentOff;
    private UUID customerId;
    private LocalDateTime purchasedDate;
    private LocalDateTime usedDate;
}
