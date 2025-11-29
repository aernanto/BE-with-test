package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165963_be.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // Validate
        validatePackage(packageEntity);
        
        // Generate ID
        packageEntity.setId(UUID.randomUUID().toString());
        
        // Set initial status
        packageEntity.setStatus("Pending");
        
        return packageRepository.save(packageEntity);
    }

    @Override
    public Package updatePackage(Package packageEntity) {
        // Check if exists
        Optional<Package> existingPackage = packageRepository.findById(packageEntity.getId());
        if (existingPackage.isEmpty()) {
            throw new IllegalArgumentException("Package not found with ID: " + packageEntity.getId());
        }
        
        Package existing = existingPackage.get();
        
        // Check if can be edited
        if ("Processed".equals(existing.getStatus())) {
            throw new IllegalStateException("Cannot update processed package");
        }
        
        // Validate
        validatePackage(packageEntity);
        
        // Update fields
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
        
        // Check if can be deleted
        if ("Processed".equals(packageEntity.getStatus())) {
            throw new IllegalStateException("Cannot delete processed package");
        }
        
        // Delete all associated plans first
        planRepository.deleteByPackageId(id);
        
        // Delete package
        packageRepository.deleteById(id);
        return true;
    }

    @Override
    public void processPackage(String id) {
        Optional<Package> packageOptional = packageRepository.findByIdWithPlans(id);
        
        if (packageOptional.isEmpty()) {
            throw new IllegalArgumentException("Package not found with ID: " + id);
        }
        
        Package packageEntity = packageOptional.get();
        
        // Validate package can be processed
        if ("Processed".equals(packageEntity.getStatus())) {
            throw new IllegalStateException("Package is already processed");
        }
        
        // Check if package has at least one plan
        if (packageEntity.getPlans() == null || packageEntity.getPlans().isEmpty()) {
            throw new IllegalStateException("Cannot process package without plans");
        }
        
        // Check if all plans are complete (have ordered quantities)
        for (Plan plan : packageEntity.getPlans()) {
            if (!"Processed".equals(plan.getStatus())) {
                throw new IllegalStateException("All plans must be processed before processing package. Plan ID: " + plan.getId() + " is still " + plan.getStatus());
            }
            
        // Set package status to Processed
        packageEntity.setStatus("Processed");
        packageRepository.save(packageEntity);
        }
    
    }

    @Override
    public List<Package> getPackagesByUserId(String userId) {
        return packageRepository.findByUserId(userId);
    }

    @Override
    public List<Package> getPackagesByStatus(String status) {
        return packageRepository.findByStatus(status);
    }

    // ========== PRIVATE HELPER METHODS ==========
    
    private void validatePackage(Package packageEntity) {
        // Validate dates
        if (packageEntity.getEndDate().isBefore(packageEntity.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        // Validate quota
        if (packageEntity.getQuota() <= 0) {
            throw new IllegalArgumentException("Quota must be greater than 0");
        }
        
        // Validate price
        if (packageEntity.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        
        // Validate user ID
        if (packageEntity.getUserId() == null || packageEntity.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // Validate package name
        if (packageEntity.getPackageName() == null || packageEntity.getPackageName().trim().isEmpty()) {
            throw new IllegalArgumentException("Package name is required");
        }
    }
}