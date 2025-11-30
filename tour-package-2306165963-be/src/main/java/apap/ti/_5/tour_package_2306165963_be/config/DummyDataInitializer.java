package apap.ti._5.tour_package_2306165963_be.config;

import apap.ti._5.tour_package_2306165963_be.model.*;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.loyalty.Coupon;
import apap.ti._5.tour_package_2306165963_be.model.loyalty.Customer;
import apap.ti._5.tour_package_2306165963_be.model.loyalty.LoyaltyPoints;
import apap.ti._5.tour_package_2306165963_be.model.loyalty.PurchasedCoupon;
import apap.ti._5.tour_package_2306165963_be.repository.*;
import apap.ti._5.tour_package_2306165963_be.service.LocationService;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {

        private final ActivityRepository activityRepository;
        private final PackageRepository packageRepository;
        private final PlanRepository planRepository;
        private final OrderedQuantityRepository orderedQuantityRepository;
        private final CustomerRepository customerRepository;
        private final CouponRepository couponRepository;
        private final LoyaltyPointsRepository loyaltyPointsRepository;
        private final PurchasedCouponRepository purchasedCouponRepository;
        private final LocationService locationService;

        @Override
        @Transactional
        public void run(String... args) {
                if (activityRepository.count() > 0) {
                        log.info("✅ Dummy data already exists. Skipping initialization.");
                        return;
                }

                // ANSI color codes
                String RESET = "\u001B[0m";
                String BOLD = "\u001B[1m";
                String CYAN = "\u001B[36m";
                String GREEN = "\u001B[32m";
                String YELLOW = "\u001B[33m";
                String BLUE = "\u001B[34m";
                String MAGENTA = "\u001B[35m";

                System.out.println("\n" + CYAN + "╔" + "═".repeat(78) + "╗" + RESET);
                System.out.println(
                                CYAN + "║" + RESET + BOLD + "  🌍  Tour Package & Loyalty System - Dummy Data Generator"
                                                + " ".repeat(18) + CYAN + "║" + RESET);
                System.out.println(CYAN + "╚" + "═".repeat(78) + "╝" + RESET + "\n");

                Faker faker = new Faker(new Locale("id", "ID"));
                Random random = new Random();

                // 1. Create Customers
                System.out.println(BOLD + BLUE + "\n┌─ [1/5] Creating Customers" + RESET);
                System.out.println(BLUE + "│" + RESET);
                List<Customer> customers = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                        String name = faker.name().fullName();
                        Customer customer = Customer.builder()
                                        .id(UUID.randomUUID().toString())
                                        .name(name)
                                        .loyaltyPoints(0)
                                        .build();
                        customers.add(customerRepository.save(customer));
                        System.out.println(BLUE + "│  " + RESET + "➤ " + name);
                }
                System.out.println(BLUE + "│" + RESET);
                System.out.println(
                                BLUE + "└─" + RESET + GREEN + " ✓ Created " + customers.size() + " customers" + RESET);

                // 2. Create Activities (based on LocationService)
                System.out.println(BOLD + MAGENTA + "\n┌─ [2/5] Creating Activities (from Locations)" + RESET);
                System.out.println(MAGENTA + "│" + RESET);

                List<Activity> activities = new ArrayList<>();
                try {
                        // Fetch provinces and regencies
                        var provinces = locationService.getAllProvinces().block();
                        if (provinces != null && !provinces.isEmpty()) {
                                List<Map<String, String>> locations = new ArrayList<>();
                                int provLimit = 3;
                                int regLimit = 2;

                                for (int i = 0; i < Math.min(provinces.size(), provLimit); i++) {
                                        Map<String, Object> p = provinces.get(i);
                                        String pCode = String.valueOf(p.get("id"));
                                        String pName = String.valueOf(p.get("name"));

                                        var regencies = locationService.getRegenciesByProvince(pCode).block();
                                        if (regencies != null) {
                                                for (int j = 0; j < Math.min(regencies.size(), regLimit); j++) {
                                                        Map<String, Object> r = regencies.get(j);
                                                        String rName = String.valueOf(r.get("name"));
                                                        Map<String, String> loc = new HashMap<>();
                                                        loc.put("label", pName + " - " + rName);
                                                        locations.add(loc);
                                                }
                                        }
                                }

                                if (locations.size() >= 2) {
                                        LocalDateTime base = LocalDateTime.now().plusDays(7);
                                        int seq = 1;

                                        // Accommodation: per location
                                        for (Map<String, String> loc : locations) {
                                                String label = loc.get("label");
                                                Activity acc = Activity.builder()
                                                                .id(UUID.randomUUID().toString())
                                                                .activityName("Hotel "
                                                                                + faker.address().buildingNumber()
                                                                                + " @ " + label)
                                                                .activityItem("Room " + seq)
                                                                .activityType("Accommodation")
                                                                .capacity(100)
                                                                .price(500_000L + (seq % 5) * 50_000L)
                                                                .startDate(base.withHour(14).withMinute(0))
                                                                .endDate(base.plusDays(2).withHour(12).withMinute(0))
                                                                .startLocation(label)
                                                                .endLocation(label)
                                                                .build();
                                                activities.add(activityRepository.save(acc));
                                                seq++;
                                        }

                                        // Flight & Vehicle: between locations
                                        for (int i = 0; i < locations.size(); i++) {
                                                for (int j = 0; j < locations.size(); j++) {
                                                        if (i == j)
                                                                continue;
                                                        String start = locations.get(i).get("label");
                                                        String end = locations.get(j).get("label");

                                                        Activity flight = Activity.builder()
                                                                        .id(UUID.randomUUID().toString())
                                                                        .activityName("Flight " + seq + " (" + start
                                                                                        + " → " + end + ")")
                                                                        .activityItem("Ticket " + seq)
                                                                        .activityType("Flight")
                                                                        .capacity(150)
                                                                        .price(1_000_000L + (seq % 7) * 100_000L)
                                                                        .startDate(base.withHour(8).withMinute(0))
                                                                        .endDate(base.withHour(10).withMinute(0))
                                                                        .startLocation(start)
                                                                        .endLocation(end)
                                                                        .build();
                                                        activities.add(activityRepository.save(flight));
                                                        seq++;

                                                        Activity vehicle = Activity.builder()
                                                                        .id(UUID.randomUUID().toString())
                                                                        .activityName("Rental " + seq + " (" + start
                                                                                        + " → " + end + ")")
                                                                        .activityItem("Car " + seq)
                                                                        .activityType("Vehicle")
                                                                        .capacity(50)
                                                                        .price(400_000L + (seq % 7) * 50_000L)
                                                                        .startDate(base.withHour(9).withMinute(0))
                                                                        .endDate(base.plusDays(1).withHour(18)
                                                                                        .withMinute(0))
                                                                        .startLocation(start)
                                                                        .endLocation(end)
                                                                        .build();
                                                        activities.add(activityRepository.save(vehicle));
                                                        seq++;
                                                }
                                        }
                                }
                        }
                } catch (Exception e) {
                        log.error("Failed to fetch locations", e);
                        // Fallback activities if location service fails
                        LocalDateTime now = LocalDateTime.now();
                        activities.add(activityRepository.save(Activity.builder()
                                        .id(UUID.randomUUID().toString())
                                        .activityName("Fallback Flight")
                                        .activityType("Flight")
                                        .activityItem("Ticket")
                                        .capacity(100)
                                        .price(1000000L)
                                        .startDate(now.plusDays(1))
                                        .endDate(now.plusDays(1).plusHours(2))
                                        .startLocation("City A")
                                        .endLocation("City B")
                                        .build()));
                }
                System.out.println(MAGENTA + "│" + RESET);
                System.out.println(MAGENTA + "└─" + RESET + GREEN + " ✓ Created " + activities.size() + " activities"
                                + RESET);

                // 3. Create Packages
                System.out.println(BOLD + YELLOW + "\n┌─ [3/5] Creating Packages" + RESET);
                System.out.println(YELLOW + "│" + RESET);

                List<Activity> flights = activities.stream().filter(a -> "Flight".equals(a.getActivityType()))
                                .collect(Collectors.toList());
                List<Activity> hotels = activities.stream().filter(a -> "Accommodation".equals(a.getActivityType()))
                                .collect(Collectors.toList());

                int packageCount = 0;
                if (!activities.isEmpty()) {
                        for (int i = 0; i < 15; i++) {
                                Customer owner = customers.get(random.nextInt(customers.size()));
                                String pkgName = faker.address().cityName() + " " + faker.commerce().material()
                                                + " Trip";

                                Package pkg = Package.builder()
                                                .id("PKG-" + System.currentTimeMillis() + "-" + i)
                                                .userId(owner.getId())
                                                .packageName(pkgName)
                                                .quota(5 + random.nextInt(10))
                                                .price(0L)
                                                .status(i % 5 == 0 ? "Processed" : "Pending")
                                                .startDate(LocalDateTime.now().plusDays(random.nextInt(10)))
                                                .endDate(LocalDateTime.now().plusDays(10 + random.nextInt(10)))
                                                .plans(new ArrayList<>())
                                                .build();

                                // Add Plans
                                int numPlans = 1 + random.nextInt(3);
                                for (int p = 0; p < numPlans; p++) {
                                        Activity act = null;
                                        if (p == 0 && !flights.isEmpty())
                                                act = flights.get(random.nextInt(flights.size()));
                                        else if (!hotels.isEmpty())
                                                act = hotels.get(random.nextInt(hotels.size()));
                                        else
                                                act = activities.get(random.nextInt(activities.size()));

                                        if (act != null) {
                                                Plan plan = Plan.builder()
                                                                .packageId(pkg.getId())
                                                                .activityType(act.getActivityType())
                                                                .price(0L)
                                                                .status("Unfulfilled")
                                                                .startDate(act.getStartDate())
                                                                .endDate(act.getEndDate())
                                                                .startLocation(act.getStartLocation())
                                                                .endLocation(act.getEndLocation())
                                                                .orderedQuantities(new ArrayList<>())
                                                                .build();

                                                // Add OrderedQuantity
                                                OrderedQuantity oq = OrderedQuantity.builder()
                                                                .activityId(act.getId())
                                                                .orderedQuota(1 + random.nextInt(3))
                                                                .quota(act.getCapacity())
                                                                .price(act.getPrice())
                                                                .activityName(act.getActivityName())
                                                                .activityItem(act.getActivityItem())
                                                                .startDate(act.getStartDate())
                                                                .endDate(act.getEndDate())
                                                                .build();

                                                plan.getOrderedQuantities().add(oq);
                                                plan.setPrice(plan.getPrice() + (oq.getPrice() * oq.getOrderedQuota()));

                                                pkg.getPlans().add(plan);
                                                pkg.setPrice(pkg.getPrice() + plan.getPrice());
                                        }
                                }
                                packageRepository.save(pkg);
                                packageCount++;
                                System.out.println(
                                                YELLOW + "│  " + RESET + "➤ " + pkgName + " (" + pkg.getStatus() + ")");
                        }
                }
                System.out.println(YELLOW + "│" + RESET);
                System.out.println(YELLOW + "└─" + RESET + GREEN + " ✓ Created " + packageCount + " packages" + RESET);

                // 4. Create Coupons
                System.out.println(BOLD + CYAN + "\n┌─ [4/5] Creating Coupons" + RESET);
                System.out.println(CYAN + "│" + RESET);
                List<Coupon> coupons = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                        Coupon coupon = Coupon.builder()
                                        .name(faker.commerce().promotionCode() + " DEAL")
                                        .description("Discount for " + faker.commerce().productName())
                                        .points(faker.number().numberBetween(100, 1000))
                                        .percentOff(faker.number().numberBetween(5, 50))
                                        .build();
                        coupons.add(couponRepository.save(coupon));
                        System.out.println(CYAN + "│  " + RESET + "➤ " + coupon.getName() + " ("
                                        + coupon.getPercentOff() + "% OFF)");
                }
                System.out.println(CYAN + "│" + RESET);
                System.out.println(CYAN + "└─" + RESET + GREEN + " ✓ Created " + coupons.size() + " coupons" + RESET);

                // 5. Create Loyalty Points & Purchased Coupons
                System.out.println(BOLD + MAGENTA + "\n┌─ [5/5] Creating Loyalty Data" + RESET);
                System.out.println(MAGENTA + "│" + RESET);
                int purchasedCount = 0;
                for (Customer customer : customers) {
                        // Points
                        int pointsVal = faker.number().numberBetween(500, 5000);
                        LoyaltyPoints points = LoyaltyPoints.builder()
                                        .customerId(UUID.fromString(customer.getId()))
                                        .points(pointsVal)
                                        .build();
                        loyaltyPointsRepository.save(points);

                        // Update customer entity too for consistency
                        customer.setLoyaltyPoints(pointsVal);
                        customerRepository.save(customer);

                        // Purchased Coupons
                        if (!coupons.isEmpty() && random.nextBoolean()) {
                                Coupon coupon = coupons.get(random.nextInt(coupons.size()));
                                PurchasedCoupon pc = PurchasedCoupon.builder()
                                                .code(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                                                .customerId(UUID.fromString(customer.getId()))
                                                .couponId(coupon.getId())
                                                .purchasedDate(LocalDateTime.now().minusDays(random.nextInt(30)))
                                                .build();
                                purchasedCouponRepository.save(pc);
                                purchasedCount++;
                        }
                }
                System.out.println(MAGENTA + "│" + RESET);
                System.out.println(MAGENTA + "└─" + RESET + GREEN + " ✓ Assigned points & created " + purchasedCount
                                + " purchased coupons" + RESET);

                System.out.println("\n" + CYAN + "╔" + "═".repeat(78) + "╗" + RESET);
                System.out.println(CYAN + "║" + RESET + BOLD + GREEN + "  ✓ Data Generation Complete!"
                                + " ".repeat(48) + CYAN + "║" + RESET);
                System.out.println(CYAN + "╚" + "═".repeat(78) + "╝" + RESET + "\n");
        }
}