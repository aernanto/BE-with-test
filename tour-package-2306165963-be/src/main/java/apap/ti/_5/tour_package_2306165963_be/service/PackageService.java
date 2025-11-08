package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Package;

import java.util.List;
import java.util.Optional;

public interface PackageService {
    List<Package> getAllPackages();
    Optional<Package> getPackageById(String id);
    Package createPackage(Package packageEntity);
    Package updatePackage(Package packageEntity);
    boolean deletePackage(String id);
    void processPackage(String id);
    List<Package> getPackagesByUserId(String userId);
    List<Package> getPackagesByStatus(String status);
    Optional<Package> getPackageWithPlans(String id);
}