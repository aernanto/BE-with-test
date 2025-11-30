package apap.ti._5.tour_package_2306165963_be.util;

import apap.ti._5.tour_package_2306165963_be.model.*;
import apap.ti._5.tour_package_2306165963_be.model.Package;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestDataFactory {

    public static Activity activity(String id) {
        return Activity.builder()
                .id(id)
                .activityName("Hotel Jakarta")
                .activityType("Accommodation")
                .activityItem("Deluxe Room")
                .capacity(50)
                .price(750000L)
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(10))
                .startLocation("Jakarta")
                .endLocation("Jakarta")
                .build();
    }

    public static Package pkg(String id) {
        return Package.builder()
                .id(id)
                .userId("user-123")
                .packageName("Summer Getaway")
                .quota(10)
                .price(5000000L)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(14))
                .status("Pending")
                .plans(new ArrayList<>())
                .build();
    }

    public static Plan plan(UUID id, String packageId) {
        return Plan.builder()
                .id(id)
                .packageId(packageId)
                .activityType("Accommodation")
                .price(0L)
                .startDate(LocalDateTime.now().plusDays(3))
                .endDate(LocalDateTime.now().plusDays(5))
                .startLocation("Jakarta")
                .endLocation("Jakarta")
                .status("Unfinished")
                .orderedQuantities(new ArrayList<>())
                .build();
    }

    public static OrderedQuantity oq(UUID id, UUID planId, String activityId) {
        return OrderedQuantity.builder()
                .id(id)
                .planId(planId)
                .activityId(activityId)
                .orderedQuota(2)
                .quota(50)
                .price(750000L)
                .activityName("Hotel Jakarta")
                .activityItem("Deluxe Room")
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(10))
                .build();
    }

    public static List<OrderedQuantity> oqList(UUID planId, String actId, int count) {
        List<OrderedQuantity> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(oq(UUID.randomUUID(), planId, actId));
        }
        return list;
    }
}