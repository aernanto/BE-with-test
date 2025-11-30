package apap.ti._5.tour_package_2306165963_be.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apap.ti._5.tour_package_2306165963_be.dto.coupon.CouponRequestDto;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.CouponResponseDTO;
import apap.ti._5.tour_package_2306165963_be.model.loyalty.Coupon;
import apap.ti._5.tour_package_2306165963_be.repository.CouponRepository;

@Service
@Transactional
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepo;

    @Override
    public List<CouponResponseDTO> getCoupons() {
        return couponRepo.findAllByOrderByCreatedDateDesc().stream()
                .map(this::mapToCouponResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CouponResponseDTO createCoupon(CouponRequestDto dto) {
        Coupon c = Coupon.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .points(dto.getPoints())
                .percentOff(dto.getPercentOff())
                .build();
        Coupon saved = couponRepo.save(c);
        return mapToCouponResponseDTO(saved);
    }

    @Override
    public CouponResponseDTO updateCoupon(UUID id, CouponRequestDto dto) {
        Coupon c = couponRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coupon with id " + id + " not found"));
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        c.setPoints(dto.getPoints());
        c.setPercentOff(dto.getPercentOff());
        Coupon saved = couponRepo.save(c);
        return mapToCouponResponseDTO(saved);
    }

    private CouponResponseDTO mapToCouponResponseDTO(Coupon coupon) {
        return CouponResponseDTO.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .points(coupon.getPoints())
                .percentOff(coupon.getPercentOff())
                .createdDate(coupon.getCreatedDate())
                .updatedDate(coupon.getUpdatedDate())
                .build();
    }
}