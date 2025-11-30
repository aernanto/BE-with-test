package apap.ti._5.tour_package_2306165963_be.dto.loyalty;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyDashboardResponseDTO {

    private UUID customerId;
    private LoyaltyPointsResponseDTO balance;
    private List<PurchasedCouponResponseDTO> purchasedCoupons;
    private List<CouponResponseDTO> availableCoupons;
    private Integer totalPurchasedCoupons;
    private Integer activeCoupons;
    private Integer redeemedCoupons;
    private Integer availableCouponCount;
}
