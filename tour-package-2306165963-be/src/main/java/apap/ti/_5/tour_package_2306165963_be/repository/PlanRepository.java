package apap.ti._5.tour_package_2306165963_be.repository;

import apap.ti._5.tour_package_2306165963_be.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    // Find by package ID
    List<Plan> findByPackageId(String packageId);

    // Find by activity type
    List<Plan> findByActivityType(String activityType);

    // Find by status
    List<Plan> findByStatus(String status);

    // Find by package ID and status
    List<Plan> findByPackageIdAndStatus(String packageId, String status);

    // Find plan with ordered quantities
    @Query("SELECT DISTINCT p FROM Plan p LEFT JOIN FETCH p.orderedQuantities WHERE p.id = :id")
    Optional<Plan> findByIdWithOrderedQuantities(@Param("id") UUID id);

    // Find plans by package with ordered quantities
    @Query("SELECT DISTINCT p FROM Plan p LEFT JOIN FETCH p.orderedQuantities WHERE p.packageId = :packageId")
    List<Plan> findByPackageIdWithOrderedQuantities(@Param("packageId") String packageId);

    // Count plans by package
    long countByPackageId(String packageId);

    // Delete by package ID
    void deleteByPackageId(String packageId);
}