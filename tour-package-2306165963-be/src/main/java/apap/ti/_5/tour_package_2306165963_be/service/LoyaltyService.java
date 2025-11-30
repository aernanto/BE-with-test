package apap.ti._5.tour_package_2306165963_be.service;

import java.util.List;
import java.util.UUID;

import apap.ti._5.tour_package_2306165963_be.dto.coupon.AddPointsRequestDto;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.LoyaltyDashboardResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.LoyaltyPointsResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.PurchaseCouponRequestDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.PurchasedCouponResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.RedeemCouponRequestDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.RedeemCouponResponseDTO;

public interface LoyaltyService {

    LoyaltyPointsResponseDTO addPoints(AddPointsRequestDto request);

    LoyaltyPointsResponseDTO getBalance(UUID customerId);

    PurchasedCouponResponseDTO purchaseCoupon(PurchaseCouponRequestDTO request);

    List<PurchasedCouponResponseDTO> getPurchasedCoupons(UUID customerId);

    LoyaltyDashboardResponseDTO getDashboard(UUID customerId);

    RedeemCouponResponseDTO redeemCoupon(RedeemCouponRequestDTO request);
}
