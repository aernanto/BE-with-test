package apap.ti._5.tour_package_2306165963_be.repository;

import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderedQuantityRepository extends JpaRepository<OrderedQuantity, UUID> {

    // Find by plan ID
    List<OrderedQuantity> findByPlanId(UUID planId);

    // Find by activity ID
    List<OrderedQuantity> findByActivityId(String activityId);

    // Find by plan ID and activity ID
    List<OrderedQuantity> findByPlanIdAndActivityId(UUID planId, String activityId);

    // Calculate total ordered quota for a plan
    @Query("SELECT SUM(oq.orderedQuota) FROM OrderedQuantity oq WHERE oq.planId = :planId")
    Integer sumOrderedQuotaByPlanId(@Param("planId") UUID planId);

    // Calculate total price for a plan
    @Query("SELECT SUM(oq.price * oq.orderedQuota) FROM OrderedQuantity oq WHERE oq.planId = :planId")
    Long sumTotalPriceByPlanId(@Param("planId") UUID planId);

    // Count ordered quantities by plan
    long countByPlanId(UUID planId);

    // Delete by plan ID
    void deleteByPlanId(UUID planId);

    // Check if activity is used in any plan
    boolean existsByActivityId(String activityId);
}