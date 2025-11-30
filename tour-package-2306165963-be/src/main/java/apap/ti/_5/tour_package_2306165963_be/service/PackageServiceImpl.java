package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165963_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165963_be.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PackageServiceImpl implements PackageService {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Override
    public List<Package> getAllPackages() {
        return packageRepository.findAll();
    }

    @Override
    public Optional<Package> getPackageById(String id) {
        return packageRepository.findById(id);
    }

    @Override
    public Optional<Package> getPackageWithPlans(String id) {
        return packageRepository.findByIdWithPlans(id);
    }

    @Override
    public Package createPackage(Package packageEntity) {
        validatePackage(packageEntity);
        packageEntity.setId(generatePackageId(packageEntity.getUserId()));
        packageEntity.setStatus("Pending");
        packageEntity.setDeleted(false);
        return packageRepository.save(packageEntity);
    }

    @Override
    public Package updatePackage(Package packageEntity) {
        Optional<Package> existingPackage = packageRepository.findById(packageEntity.getId());
        if (existingPackage.isEmpty()) {
            throw new IllegalArgumentException("Package not found with ID: " + packageEntity.getId());
        }

        Package existing = existingPackage.get();

        // Check if can be edited (only Pending or Processed)
        if (!"Pending".equals(existing.getStatus()) && !"Processed".equals(existing.getStatus())) {
            throw new IllegalStateException("Cannot update package with status: " + existing.getStatus());
        }

        validatePackage(packageEntity);

        existing.setUserId(packageEntity.getUserId());
        existing.setPackageName(packageEntity.getPackageName());
        existing.setQuota(packageEntity.getQuota());
        existing.setPrice(packageEntity.getPrice());
        existing.setStartDate(packageEntity.getStartDate());
        existing.setEndDate(packageEntity.getEndDate());

        return packageRepository.save(existing);
    }

    @Override
    public boolean deletePackage(String id) {
        Optional<Package> packageOptional = packageRepository.findById(id);

        if (packageOptional.isEmpty()) {
            return false;
        }

        Package packageEntity = packageOptional.get();

        // Can only delete if status = Pending
        if (!"Pending".equals(packageEntity.getStatus())) {
            throw new IllegalStateException("Can only delete packages with status Pending");
        }

        // Soft delete
        packageEntity.setDeleted(true);
        packageRepository.save(packageEntity);
        return true;
    }

    @Override
    public void processPackage(String id) {
        Optional<Package> packageOptional = packageRepository.findByIdWithPlans(id);

        if (packageOptional.isEmpty()) {
            throw new IllegalArgumentException("Package not found with ID: " + id);
        }

        Package packageEntity = packageOptional.get();

        if (!"Pending".equals(packageEntity.getStatus())) {
            throw new IllegalStateException("Only Pending packages can be processed");
        }

        // Check if has plans
        if (packageEntity.getPlans() == null || packageEntity.getPlans().isEmpty()) {
            throw new IllegalStateException("Cannot process package without plans");
        }

        boolean allPlansFulfilled = packageEntity.getPlans().stream()
                .allMatch(plan -> "Fulfilled".equals(plan.getStatus()));

        if (!allPlansFulfilled) {
            String unfulfilledPlans = packageEntity.getPlans().stream()
                    .filter(plan -> !"Fulfilled".equals(plan.getStatus()))
                    .map(plan -> "Plan " + plan.getId() + " (status: " + plan.getStatus() + ")")
                    .collect(java.util.stream.Collectors.joining(", "));

            throw new IllegalStateException(
                    "All plan statuses must be 'Fulfilled' before processing package. " +
                            "Unfulfilled plans: " + unfulfilledPlans);
        }

        for (Plan plan : packageEntity.getPlans()) {
            for (OrderedQuantity oq : plan.getOrderedQuantities()) {
                if (oq.getOrderedQuota() > oq.getQuota()) {
                    throw new IllegalStateException(
                            "OrderedQuantity exceeds Activity Capacity for activity: " + oq.getActivityName());
                }

                // Reduce capacity
                Optional<Activity> activityOpt = activityRepository.findById(oq.getActivityId());
                if (activityOpt.isPresent()) {
                    Activity activity = activityOpt.get();
                    int newCapacity = activity.getCapacity() - oq.getOrderedQuota();
                    if (newCapacity < 0) {
                        throw new IllegalStateException(
                                "Not enough capacity for activity: " + activity.getActivityName());
                    }
                    activity.setCapacity(newCapacity);
                    activityRepository.save(activity);
                }
            }
        }

        // Calculate total package price from all fulfilled plans
        long totalPrice = packageEntity.getPlans().stream()
                .mapToLong(plan -> plan.getPrice() != null ? plan.getPrice() : 0L)
                .sum();

        packageEntity.setPrice(totalPrice);
        packageEntity.setStatus("Processed"); // Or "Waiting for Payment" based on PBI, but Prompt says "Processed"
                                              // locks it.

        packageRepository.save(packageEntity);
    }

    @Override
    public List<Package> getPackagesByUserId(String userId) {
        return packageRepository.findByUserId(userId);
    }

    @Override
    public List<Package> getPackagesByStatus(String status) {
        return packageRepository.findByStatus(status);
    }

    // NEW: Get packages by vendor ID
    @Override
    public List<Package> getPackagesByVendorId(String vendorId) {
        return packageRepository.findPackagesByVendorId(vendorId);
    }

    // NEW: Get package detail with RBAC
    @Override
    public Package getPackageDetail(String packageId, String userId, String role) {
        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Package not found"));

        // RBAC Logic
        boolean isOwner = pkg.getUserId().equals(userId);
        boolean isSuperadmin = "Superadmin".equals(role);
        boolean isVendor = "TourPackageVendor".equals(role) && packageHasVendorActivity(pkg, userId);

        if (!isOwner && !isSuperadmin && !isVendor) {
            throw new IllegalStateException("You don't have access to this package");
        }

        return pkg;
    }

    // NEW: Get all packages with RBAC
    @Override
    public List<Package> getAllPackagesWithRBAC(String userId, String role) {
        if ("Superadmin".equals(role) || "TourPackageVendor".equals(role)) {
            // Superadmin dan Vendor bisa lihat semua
            return packageRepository.findAll();
        } else {
            // Customer cuma bisa lihat miliknya sendiri + yang dibuat vendor
            return packageRepository.findByUserIdOrIsPublic(userId);
        }
    }

    // Helper methods
    private void validatePackage(Package packageEntity) {
        if (packageEntity.getEndDate().isBefore(packageEntity.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (packageEntity.getQuota() <= 0) {
            throw new IllegalArgumentException("Quota must be greater than 0");
        }

        if (packageEntity.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        if (packageEntity.getUserId() == null || packageEntity.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (packageEntity.getPackageName() == null || packageEntity.getPackageName().trim().isEmpty()) {
            throw new IllegalArgumentException("Package name is required");
        }
    }

    private boolean packageHasVendorActivity(Package pkg, String vendorId) {
        if (pkg.getPlans() == null) {
            return false;
        }
        // Check if any ordered quantity's activity belongs to this vendor
        // You need to fetch activity details to check vendorId
        return pkg.getPlans().stream()
                .filter(plan -> plan.getOrderedQuantities() != null)
                .flatMap(plan -> plan.getOrderedQuantities().stream())
                .map(OrderedQuantity::getActivityId)
                .distinct()
                .anyMatch(activityId -> isActivityOwnedByVendor(activityId, vendorId));
    }

    private boolean isActivityOwnedByVendor(String activityId, String vendorId) {
        Optional<Activity> activity = activityRepository.findById(activityId);
        return activity.map(value -> value.getVendorId().equals(vendorId)).orElse(false);
    }

    private String generatePackageId(String userId) {
        long count = packageRepository.countByUserId(userId);
        return String.format("PACK-%s-%03d", userId, count + 1);
    }
}