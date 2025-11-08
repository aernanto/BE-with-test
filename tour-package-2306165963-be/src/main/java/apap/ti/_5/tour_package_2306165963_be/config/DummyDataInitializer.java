package apap.ti._5.tour_package_2306165963_be.config;

import apap.ti._5.tour_package_2306165963_be.model.*;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class DummyDataInitializer implements CommandLineRunner {

    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private PackageRepository packageRepository;
    
    @Autowired
    private PlanRepository planRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (activityRepository.count() > 0) {
            log.info("✅ Dummy data already exists. Skipping initialization.");
            return;
        }

        log.info("🚀 Starting dummy data generation...");
        
        // 1. Create Activities
        List<Activity> activities = createActivities();
        activityRepository.saveAll(activities);
        log.info("✅ Created {} activities", activities.size());
        
        // 2. Create Packages with Plans
        List<Package> packages = createPackages(activities);
        packageRepository.saveAll(packages);
        log.info("✅ Created {} packages", packages.size());
        
        log.info("🎉 Dummy data generation completed!");
    }

    private List<Activity> createActivities() {
        LocalDateTime now = LocalDateTime.now();
        List<Activity> activities = new ArrayList<>();

        // Flights
        activities.add(Activity.builder()
                .id(UUID.randomUUID().toString())
                .activityName("Garuda Indonesia")
                .activityType("Flight")
                .activityItem("Jakarta (CGK) - Bali (DPS)")
                .capacity(180)
                .price(1500000L)
                .startDate(now.plusDays(7))
                .endDate(now.plusDays(7).plusHours(2))
                .startLocation("Jakarta (CGK)")
                .endLocation("Bali (DPS)")
                .build());

        activities.add(Activity.builder()
                .id(UUID.randomUUID().toString())
                .activityName("Lion Air")
                .activityType("Flight")
                .activityItem("Jakarta (CGK) - Yogyakarta (YIA)")
                .capacity(150)
                .price(800000L)
                .startDate(now.plusDays(5))
                .endDate(now.plusDays(5).plusHours(1))
                .startLocation("Jakarta (CGK)")
                .endLocation("Yogyakarta (YIA)")
                .build());

        // Accommodations
        activities.add(Activity.builder()
                .id(UUID.randomUUID().toString())
                .activityName("Grand Hyatt Bali")
                .activityType("Accommodation")
                .activityItem("Deluxe Ocean View Room")
                .capacity(50)
                .price(2500000L)
                .startDate(now.plusDays(7))
                .endDate(now.plusDays(10))
                .startLocation("Nusa Dua, Bali")
                .endLocation("Nusa Dua, Bali")
                .build());

        activities.add(Activity.builder()
                .id(UUID.randomUUID().toString())
                .activityName("Tentrem Hotel Yogyakarta")
                .activityType("Accommodation")
                .activityItem("Superior Room")
                .capacity(80)
                .price(1200000L)
                .startDate(now.plusDays(5))
                .endDate(now.plusDays(8))
                .startLocation("Yogyakarta")
                .endLocation("Yogyakarta")
                .build());

        // Vehicles
        activities.add(Activity.builder()
                .id(UUID.randomUUID().toString())
                .activityName("Bali Car Rental")
                .activityType("Vehicle")
                .activityItem("Toyota Avanza + Driver")
                .capacity(20)
                .price(500000L)
                .startDate(now.plusDays(7))
                .endDate(now.plusDays(10))
                .startLocation("Bali Airport")
                .endLocation("Bali Airport")
                .build());

        activities.add(Activity.builder()
                .id(UUID.randomUUID().toString())
                .activityName("Jogja Transport")
                .activityType("Vehicle")
                .activityItem("Innova Reborn + Driver")
                .capacity(15)
                .price(600000L)
                .startDate(now.plusDays(5))
                .endDate(now.plusDays(8))
                .startLocation("Yogyakarta Airport")
                .endLocation("Yogyakarta Airport")
                .build());

        return activities;
    }

    private List<Package> createPackages(List<Activity> activities) {
        LocalDateTime now = LocalDateTime.now();
        List<Package> packages = new ArrayList<>();

        // Package 1: Bali Getaway (Processed)
        Package pkg1 = Package.builder()
                .id(UUID.randomUUID().toString())
                .userId("user-001")
                .packageName("Bali Paradise Getaway")
                .quota(10)
                .price(8500000L)
                .startDate(now.plusDays(7))
                .endDate(now.plusDays(10))
                .status("Processed")
                .plans(new ArrayList<>())
                .build();

        // Plans for Package 1
        Activity flight1 = activities.stream()
                .filter(a -> a.getActivityName().equals("Garuda Indonesia"))
                .findFirst().orElse(null);
        
        Activity hotel1 = activities.stream()
                .filter(a -> a.getActivityName().equals("Grand Hyatt Bali"))
                .findFirst().orElse(null);
        
        Activity car1 = activities.stream()
                .filter(a -> a.getActivityName().equals("Bali Car Rental"))
                .findFirst().orElse(null);

        if (flight1 != null) {
            Plan plan1 = createPlan(pkg1.getId(), "Flight", flight1, 2);
            pkg1.getPlans().add(plan1);
        }

        if (hotel1 != null) {
            Plan plan2 = createPlan(pkg1.getId(), "Accommodation", hotel1, 2);
            pkg1.getPlans().add(plan2);
        }

        if (car1 != null) {
            Plan plan3 = createPlan(pkg1.getId(), "Vehicle", car1, 1);
            pkg1.getPlans().add(plan3);
        }

        packages.add(pkg1);

        // Package 2: Yogyakarta Cultural Tour (Pending)
        Package pkg2 = Package.builder()
                .id(UUID.randomUUID().toString())
                .userId("user-002")
                .packageName("Yogyakarta Cultural Experience")
                .quota(15)
                .price(0L)
                .startDate(now.plusDays(5))
                .endDate(now.plusDays(8))
                .status("Pending")
                .plans(new ArrayList<>())
                .build();

        Activity flight2 = activities.stream()
                .filter(a -> a.getActivityName().equals("Lion Air"))
                .findFirst().orElse(null);
        
        Activity hotel2 = activities.stream()
                .filter(a -> a.getActivityName().equals("Tentrem Hotel Yogyakarta"))
                .findFirst().orElse(null);

        if (flight2 != null) {
            Plan plan4 = createPlanUnfinished(pkg2.getId(), "Flight", flight2.getStartDate(), flight2.getEndDate(), flight2.getStartLocation(), flight2.getEndLocation());
            pkg2.getPlans().add(plan4);
        }

        if (hotel2 != null) {
            Plan plan5 = createPlanUnfinished(pkg2.getId(), "Accommodation", hotel2.getStartDate(), hotel2.getEndDate(), hotel2.getStartLocation(), hotel2.getEndLocation());
            pkg2.getPlans().add(plan5);
        }

        packages.add(pkg2);

        // Package 3: Weekend Escape (Unfinished)
        Package pkg3 = Package.builder()
                .id(UUID.randomUUID().toString())
                .userId("user-003")
                .packageName("Weekend Escape Package")
                .quota(8)
                .price(0L)
                .startDate(now.plusDays(14))
                .endDate(now.plusDays(16))
                .status("Pending")
                .plans(new ArrayList<>())
                .build();

        packages.add(pkg3);

        return packages;
    }

    private Plan createPlan(String packageId, String activityType, Activity activity, int orderedQuota) {
        Plan plan = Plan.builder()
                .id(UUID.randomUUID().toString())
                .packageId(packageId)
                .activityType(activityType)
                .price((long) (activity.getPrice() * orderedQuota))
                .status("Processed")
                .startDate(activity.getStartDate())
                .endDate(activity.getEndDate())
                .startLocation(activity.getStartLocation())
                .endLocation(activity.getEndLocation())
                .orderedQuantities(new ArrayList<>())
                .build();

        OrderedQuantity oq = OrderedQuantity.builder()
                .id(UUID.randomUUID().toString())
                .planId(plan.getId())
                .activityId(activity.getId())
                .orderedQuota(orderedQuota)
                .quota(activity.getCapacity())
                .price(activity.getPrice())
                .activityName(activity.getActivityName())
                .activityItem(activity.getActivityItem())
                .startDate(activity.getStartDate())
                .endDate(activity.getEndDate())
                .build();

        plan.getOrderedQuantities().add(oq);
        return plan;
    }

    private Plan createPlanUnfinished(String packageId, String activityType, LocalDateTime start, LocalDateTime end, String startLoc, String endLoc) {
        return Plan.builder()
                .id(UUID.randomUUID().toString())
                .packageId(packageId)
                .activityType(activityType)
                .price(0L)
                .status("Unfinished")
                .startDate(start)
                .endDate(end)
                .startLocation(startLoc)
                .endLocation(endLoc)
                .orderedQuantities(new ArrayList<>())
                .build();
    }
}