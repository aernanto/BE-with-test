package apap.ti._5.tour_package_2306165963_be.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import apap.ti._5.tour_package_2306165963_be.model.loyalty.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    List<Coupon> findAllByOrderByCreatedDateDesc();
}