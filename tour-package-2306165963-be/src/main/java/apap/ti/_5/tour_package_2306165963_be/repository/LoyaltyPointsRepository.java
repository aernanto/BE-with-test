package apap.ti._5.tour_package_2306165963_be.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import apap.ti._5.tour_package_2306165963_be.model.loyalty.LoyaltyPoints;

@Repository
public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, UUID> {
    Optional<LoyaltyPoints> findByCustomerId(UUID customerId);
}
