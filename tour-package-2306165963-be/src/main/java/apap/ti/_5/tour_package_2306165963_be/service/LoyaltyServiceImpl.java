package apap.ti._5.tour_package_2306165963_be.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import apap.ti._5.tour_package_2306165963_be.dto.coupon.AddPointsRequestDto;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.CouponResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.LoyaltyDashboardResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.LoyaltyPointsResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.PurchaseCouponRequestDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.PurchasedCouponResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.RedeemCouponRequestDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.RedeemCouponResponseDTO;
import apap.ti._5.tour_package_2306165963_be.model.loyalty.Coupon;
import apap.ti._5.tour_package_2306165963_be.model.loyalty.LoyaltyPoints;
import apap.ti._5.tour_package_2306165963_be.model.loyalty.PurchasedCoupon;
import apap.ti._5.tour_package_2306165963_be.repository.CouponRepository;
import apap.ti._5.tour_package_2306165963_be.repository.LoyaltyPointsRepository;
import apap.ti._5.tour_package_2306165963_be.repository.PurchasedCouponRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoyaltyServiceImpl implements LoyaltyService {

    private final LoyaltyPointsRepository loyaltyPointsRepository;
    private final CouponRepository couponRepository;
    private final PurchasedCouponRepository purchasedCouponRepository;

    @Transactional
    public LoyaltyPointsResponseDTO addPoints(AddPointsRequestDto request) {
        LoyaltyPoints points = loyaltyPointsRepository.findByCustomerId(request.getCustomerId())
                .orElseGet(() -> LoyaltyPoints.builder()
                        .customerId(request.getCustomerId())
                        .points(0)
                        .build());

        points.setPoints(points.getPoints() + request.getPoints());
        LoyaltyPoints saved = loyaltyPointsRepository.save(points);
        return mapPoints(saved);
    }

    public LoyaltyPointsResponseDTO getBalance(UUID customerId) {
        LoyaltyPoints points = loyaltyPointsRepository.findByCustomerId(customerId)
                .orElseGet(() -> LoyaltyPoints.builder()
                        .customerId(customerId)
                        .points(0)
                        .build());

        return mapPoints(points);
    }

    @Transactional
    public PurchasedCouponResponseDTO purchaseCoupon(PurchaseCouponRequestDTO request) {
        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Coupon with id " + request.getCouponId() + " not found"));

        LoyaltyPoints points = loyaltyPointsRepository.findByCustomerId(request.getCustomerId())
                .orElseGet(() -> LoyaltyPoints.builder()
                        .customerId(request.getCustomerId())
                        .points(0)
                        .build());

        if (points.getPoints() < coupon.getPoints()) {
            throw new IllegalStateException("Insufficient loyalty points");
        }

        points.setPoints(points.getPoints() - coupon.getPoints());
        loyaltyPointsRepository.save(points);

        String code = generateCouponCode(coupon, request.getCustomerId());

        PurchasedCoupon purchasedCoupon = PurchasedCoupon.builder()
                .code(code)
                .couponId(coupon.getId())
                .customerId(request.getCustomerId())
                .build();

        PurchasedCoupon saved = purchasedCouponRepository.save(purchasedCoupon);
        return mapPurchasedCoupon(saved);
    }

    public List<PurchasedCouponResponseDTO> getPurchasedCoupons(UUID customerId) {
        return purchasedCouponRepository.findByCustomerIdOrderByPurchasedDateDesc(customerId)
                .stream()
                .map(this::mapPurchasedCoupon)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LoyaltyDashboardResponseDTO getDashboard(UUID customerId) {
        LoyaltyPointsResponseDTO balance = getBalance(customerId);
        List<PurchasedCouponResponseDTO> purchasedCoupons = getPurchasedCoupons(customerId);
        List<CouponResponseDTO> availableCoupons = couponRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(this::mapCoupon)
                .collect(Collectors.toList());

        long totalPurchased = purchasedCouponRepository.countByCustomerId(customerId);
        long activeCoupons = purchasedCouponRepository.countByCustomerIdAndUsedDateIsNull(customerId);
        long redeemedCoupons = purchasedCouponRepository.countByCustomerIdAndUsedDateIsNotNull(customerId);

        return LoyaltyDashboardResponseDTO.builder()
                .customerId(customerId)
                .balance(balance)
                .purchasedCoupons(purchasedCoupons)
                .availableCoupons(availableCoupons)
                .totalPurchasedCoupons(Math.toIntExact(totalPurchased))
                .activeCoupons(Math.toIntExact(activeCoupons))
                .redeemedCoupons(Math.toIntExact(redeemedCoupons))
                .availableCouponCount(availableCoupons.size())
                .build();
    }

    @Transactional
    public RedeemCouponResponseDTO redeemCoupon(RedeemCouponRequestDTO request) {
        PurchasedCoupon purchasedCoupon = purchasedCouponRepository.findByCode(request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("Coupon code " + request.getCode() + " not found"));

        if (!purchasedCoupon.getCustomerId().equals(request.getCustomerId())) {
            throw new IllegalArgumentException("Coupon code does not belong to this customer");
        }

        if (purchasedCoupon.getUsedDate() != null) {
            throw new IllegalStateException("Coupon has already been used");
        }

        purchasedCoupon.setUsedDate(LocalDateTime.now());
        purchasedCouponRepository.save(purchasedCoupon);

        Coupon coupon = couponRepository.findById(purchasedCoupon.getCouponId())
                .orElseThrow(() -> new IllegalStateException("Coupon definition not found"));

        return RedeemCouponResponseDTO.builder()
                .code(purchasedCoupon.getCode())
                .customerId(purchasedCoupon.getCustomerId())
                .couponId(coupon.getId())
                .percentOff(coupon.getPercentOff())
                .valid(true)
                .build();
    }

    private LoyaltyPointsResponseDTO mapPoints(LoyaltyPoints points) {
        return LoyaltyPointsResponseDTO.builder()
                .customerId(points.getCustomerId())
                .points(points.getPoints())
                .build();
    }

    private PurchasedCouponResponseDTO mapPurchasedCoupon(PurchasedCoupon purchasedCoupon) {
        Coupon coupon = couponRepository.findById(purchasedCoupon.getCouponId()).orElse(null);
        return PurchasedCouponResponseDTO.builder()
                .id(purchasedCoupon.getId())
                .code(purchasedCoupon.getCode())
                .couponId(purchasedCoupon.getCouponId())
                .couponName(coupon != null ? coupon.getName() : null)
                .points(coupon != null ? coupon.getPoints() : null)
                .percentOff(coupon != null ? coupon.getPercentOff() : null)
                .customerId(purchasedCoupon.getCustomerId())
                .purchasedDate(purchasedCoupon.getPurchasedDate())
                .usedDate(purchasedCoupon.getUsedDate())
                .build();
    }

    private CouponResponseDTO mapCoupon(Coupon coupon) {
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

    private String generateCouponCode(Coupon coupon, UUID customerId) {
        long sequence = purchasedCouponRepository.countByCustomerIdAndCouponId(customerId, coupon.getId()) + 1;
        String couponPart = sanitize(coupon.getName());
        String userPart = customerId.toString().substring(0, 6).toUpperCase();

        String code = buildCode(couponPart, userPart, sequence);
        while (purchasedCouponRepository.existsByCode(code)) {
            sequence++;
            code = buildCode(couponPart, userPart, sequence);
        }
        return code;
    }

    private String buildCode(String couponPart, String userPart, long sequence) {
        return String.format("%s-%s-%d", couponPart, userPart, sequence);
    }

    private String sanitize(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "LOYAL";
        }
        String cleaned = raw.replaceAll("[^A-Za-z0-9]", "");
        if (cleaned.length() > 6) {
            cleaned = cleaned.substring(0, 6);
        }
        return cleaned.toUpperCase();
    }
}
