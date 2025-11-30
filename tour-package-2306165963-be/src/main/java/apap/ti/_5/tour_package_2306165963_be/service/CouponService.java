package apap.ti._5.tour_package_2306165963_be.service;

import java.util.List;
import java.util.UUID;

import apap.ti._5.tour_package_2306165963_be.dto.coupon.CouponRequestDto;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.CouponResponseDTO;

public interface CouponService {
    List<CouponResponseDTO> getCoupons();

    CouponResponseDTO createCoupon(CouponRequestDto dto);

    CouponResponseDTO updateCoupon(UUID id, CouponRequestDto dto);
}