package apap.ti._5.tour_package_2306165963_be.restcontroller;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import apap.ti._5.tour_package_2306165963_be.dto.coupon.AddPointsRequestDto;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.CouponResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.LoyaltyDashboardResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.LoyaltyPointsResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.PurchaseCouponRequestDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.PurchasedCouponResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.RedeemCouponRequestDTO;
import apap.ti._5.tour_package_2306165963_be.dto.loyalty.RedeemCouponResponseDTO;
import apap.ti._5.tour_package_2306165963_be.dto.rest.BaseResponseDTO;
import apap.ti._5.tour_package_2306165963_be.service.CouponService;
import apap.ti._5.tour_package_2306165963_be.service.LoyaltyService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class LoyaltyRestController {

    private final LoyaltyService loyaltyService;
    private final CouponService couponService;

    public LoyaltyRestController(LoyaltyService loyaltyService, CouponService couponService) {
        this.loyaltyService = loyaltyService;
        this.couponService = couponService;
    }

    @GetMapping("/loyalty/coupons")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BaseResponseDTO<List<CouponResponseDTO>>> getAvailableCoupons() {
        var baseResponseDTO = new BaseResponseDTO<List<CouponResponseDTO>>();

        try {
            List<CouponResponseDTO> coupons = couponService.getCoupons();

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(coupons);
            baseResponseDTO.setMessage("Available coupons retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve coupons: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add-points")
    public ResponseEntity<BaseResponseDTO<LoyaltyPointsResponseDTO>> addPoints(
            @Valid @RequestBody AddPointsRequestDto request,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<LoyaltyPointsResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();

            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            LoyaltyPointsResponseDTO response = loyaltyService.addPoints(request);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(response);
            baseResponseDTO.setMessage("Points added successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to add points: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/loyalty/balance/{userId}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BaseResponseDTO<LoyaltyPointsResponseDTO>> getBalance(@PathVariable UUID userId) {
        var baseResponseDTO = new BaseResponseDTO<LoyaltyPointsResponseDTO>();

        try {
            LoyaltyPointsResponseDTO balance = loyaltyService.getBalance(userId);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(balance);
            baseResponseDTO.setMessage("Balance retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve balance: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/loyalty/dashboard/{userId}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BaseResponseDTO<LoyaltyDashboardResponseDTO>> getDashboard(@PathVariable UUID userId) {
        var baseResponseDTO = new BaseResponseDTO<LoyaltyDashboardResponseDTO>();

        try {
            LoyaltyDashboardResponseDTO dashboard = loyaltyService.getDashboard(userId);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(dashboard);
            baseResponseDTO.setMessage("Dashboard retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve dashboard: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/loyalty/coupons/purchase")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BaseResponseDTO<PurchasedCouponResponseDTO>> purchaseCoupon(
            @Valid @RequestBody PurchaseCouponRequestDTO request,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<PurchasedCouponResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();

            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            PurchasedCouponResponseDTO response = loyaltyService.purchaseCoupon(request);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(response);
            baseResponseDTO.setMessage("Coupon purchased successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (IllegalArgumentException | IllegalStateException e) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to purchase coupon: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/loyalty/coupons/purchased/{userId}")
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BaseResponseDTO<List<PurchasedCouponResponseDTO>>> getPurchasedCoupons(
            @PathVariable UUID userId) {
        var baseResponseDTO = new BaseResponseDTO<List<PurchasedCouponResponseDTO>>();

        try {
            List<PurchasedCouponResponseDTO> coupons = loyaltyService.getPurchasedCoupons(userId);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(coupons);
            baseResponseDTO.setMessage("Purchased coupons retrieved successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to retrieve purchased coupons: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/use-coupon")
    public ResponseEntity<BaseResponseDTO<RedeemCouponResponseDTO>> redeemCoupon(
            @Valid @RequestBody RedeemCouponRequestDTO request,
            BindingResult bindingResult) {

        var baseResponseDTO = new BaseResponseDTO<RedeemCouponResponseDTO>();

        if (bindingResult.hasFieldErrors()) {
            StringBuilder errorMessages = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();

            for (FieldError error : errors) {
                errorMessages.append(error.getDefaultMessage()).append("; ");
            }

            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(errorMessages.toString());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        }

        try {
            RedeemCouponResponseDTO response = loyaltyService.redeemCoupon(request);

            baseResponseDTO.setStatus(HttpStatus.OK.value());
            baseResponseDTO.setData(response);
            baseResponseDTO.setMessage("Coupon redeemed successfully");
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.OK);

        } catch (IllegalArgumentException | IllegalStateException e) {
            baseResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
            baseResponseDTO.setMessage(e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            baseResponseDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            baseResponseDTO.setMessage("Failed to redeem coupon: " + e.getMessage());
            baseResponseDTO.setTimestamp(new Date());
            return new ResponseEntity<>(baseResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
