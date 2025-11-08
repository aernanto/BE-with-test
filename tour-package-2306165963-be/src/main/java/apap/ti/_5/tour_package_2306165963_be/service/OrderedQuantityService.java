package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;

import java.util.List;
import java.util.Optional;

public interface OrderedQuantityService {
    List<OrderedQuantity> getAllOrderedQuantities();
    Optional<OrderedQuantity> getOrderedQuantityById(String id);
    OrderedQuantity createOrderedQuantity(String planId, OrderedQuantity orderedQuantity);
    OrderedQuantity updateOrderedQuantity(String id, Integer newQuota);
    boolean deleteOrderedQuantity(String id);
    List<OrderedQuantity> getOrderedQuantitiesByPlanId(String planId);
    Long calculateTotalPriceForPlan(String planId);
}