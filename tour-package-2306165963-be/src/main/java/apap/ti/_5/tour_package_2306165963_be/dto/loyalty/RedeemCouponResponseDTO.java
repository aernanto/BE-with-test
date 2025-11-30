package apap.ti._5.tour_package_2306165963_be.dto.loyalty;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedeemCouponResponseDTO {

    private String code;
    private UUID customerId;
    private UUID couponId;
    private Integer percentOff;
    private boolean valid;
}
