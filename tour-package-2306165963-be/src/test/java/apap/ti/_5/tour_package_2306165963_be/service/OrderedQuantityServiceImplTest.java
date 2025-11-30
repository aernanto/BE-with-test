package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165963_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165963_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165963_be.repository.PlanRepository;
import apap.ti._5.tour_package_2306165963_be.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderedQuantityServiceImplTest {

    @Mock
    OrderedQuantityRepository orderedQuantityRepository;

    @Mock
    PlanRepository planRepository;

    @Mock
    ActivityRepository activityRepository;

    @Mock
    PackageRepository packageRepository;

    @InjectMocks
    OrderedQuantityServiceImpl service;

    Plan plan;
    Activity activity;
    Package pkg;

    @BeforeEach
    void setup() {
        pkg = TestDataFactory.pkg("pkg-1");
        plan = TestDataFactory.plan(UUID.randomUUID(), "pkg-1");
        plan.setActivityType("Accommodation");
        activity = TestDataFactory.activity("act-1");
        activity.setActivityType("Accommodation");
    }

    @Test
    void createOrderedQuantity_planNotFound_throws() {
        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createOrderedQuantity(plan.getId().toString(),
                TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Plan not found");
    }

    @Test
    void createOrderedQuantity_packageProcessed_throws() {
        pkg.setStatus("Processed");
        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> service.createOrderedQuantity(plan.getId().toString(),
                TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pending packages");
    }

    @Test
    void createOrderedQuantity_activityNotFound_throws() {
        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(activityRepository.findById("act-1")).thenReturn(Optional.empty());
        OrderedQuantity rq = TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1");
        assertThatThrownBy(() -> service.createOrderedQuantity(plan.getId().toString(), rq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Activity not found");
    }

    @Test
    void createOrderedQuantity_typeMismatch_throws() {
        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        activity.setActivityType("Flight");
        when(activityRepository.findById("act-1")).thenReturn(Optional.of(activity));
        OrderedQuantity rq = TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1");
        assertThatThrownBy(() -> service.createOrderedQuantity(plan.getId().toString(), rq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void createOrderedQuantity_duplicate_throws() {
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(activityRepository.findById("act-1")).thenReturn(Optional.of(activity));
        when(orderedQuantityRepository.findByPlanIdAndActivityId(eq(plan.getId()), eq("act-1")))
                .thenReturn(List.of(TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1")));

        OrderedQuantity rq = TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1");
        assertThatThrownBy(() -> service.createOrderedQuantity(plan.getId().toString(), rq))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already added");
    }

    @Test
    void createOrderedQuantity_quotaExceeds_throws() {
        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(activityRepository.findById("act-1")).thenReturn(Optional.of(activity));
        when(orderedQuantityRepository.findByPlanIdAndActivityId(plan.getId(), "act-1")).thenReturn(List.of());

        OrderedQuantity rq = TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1");
        rq.setOrderedQuota(activity.getCapacity() + 1);

        assertThatThrownBy(() -> service.createOrderedQuantity(plan.getId().toString(), rq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds activity capacity");
    }

    @Test
    void createOrderedQuantity_success_setsCopies_andPlanStatus() {
        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(activityRepository.findById("act-1")).thenReturn(Optional.of(activity));
        when(orderedQuantityRepository.findByPlanIdAndActivityId(plan.getId(), "act-1")).thenReturn(List.of());
        when(orderedQuantityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderedQuantity rq = new OrderedQuantity();
        rq.setActivityId("act-1");
        rq.setOrderedQuota(2);

        OrderedQuantity saved = service.createOrderedQuantity(plan.getId().toString(), rq);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPlanId()).isEqualTo(plan.getId());
        assertThat(saved.getActivityName()).isEqualTo(activity.getActivityName());
        assertThat(saved.getQuota()).isEqualTo(activity.getCapacity());
        // Plan status should move from Unfulfilled -> Unfulfilled (because quota not
        // met)
        // Or Fulfilled if quota met.
        // Package quota is 10. Ordered quota is 2. So Unfulfilled.
        assertThat(plan.getStatus()).isEqualTo("Unfulfilled");
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void updateOrderedQuantity_notFound_throws() {
        when(orderedQuantityRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateOrderedQuantity(UUID.randomUUID().toString(), 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateOrderedQuantity_packageProcessed_throws() {
        OrderedQuantity existing = TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1");
        when(orderedQuantityRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        pkg.setStatus("Processed");
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> service.updateOrderedQuantity(existing.getId().toString(), 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pending packages");
    }

    @Test
    void updateOrderedQuantity_nonPositive_throws() {
        OrderedQuantity existing = TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1");
        when(orderedQuantityRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> service.updateOrderedQuantity(existing.getId().toString(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than 0");
    }

    @Test
    void updateOrderedQuantity_exceedsCapacity_throws() {
        OrderedQuantity existing = TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1");
        existing.setQuota(3);
        when(orderedQuantityRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> service.updateOrderedQuantity(existing.getId().toString(), 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds activity capacity");
    }

    @Test
    void updateOrderedQuantity_success() {
        OrderedQuantity existing = TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1");
        existing.setQuota(10);
        when(orderedQuantityRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(orderedQuantityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderedQuantity updated = service.updateOrderedQuantity(existing.getId().toString(), 7);

        assertThat(updated.getOrderedQuota()).isEqualTo(7);
        verify(orderedQuantityRepository).save(any(OrderedQuantity.class));
    }

    @Test
    void deleteOrderedQuantity_notFound_returnsFalse() {
        when(orderedQuantityRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThat(service.deleteOrderedQuantity(UUID.randomUUID().toString())).isFalse();
    }

    @Test
    void deleteOrderedQuantity_packageProcessed_throws() {
        OrderedQuantity existing = TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1");
        when(orderedQuantityRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        pkg.setStatus("Processed");
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> service.deleteOrderedQuantity(existing.getId().toString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pending packages");
    }

    @Test
    void deleteOrderedQuantity_success_andPlanStatusUpdated() {
        OrderedQuantity existing = TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1");
        when(orderedQuantityRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        // when(orderedQuantityRepository.countByPlanId(plan.getId())).thenReturn(0L);
        // // Not used in service impl, uses findByPlanId stream sum

        boolean result = service.deleteOrderedQuantity(existing.getId().toString());

        assertThat(result).isTrue();
        verify(orderedQuantityRepository).deleteById(existing.getId());
        // Plan status back to Unfulfilled
        assertThat(plan.getStatus()).isEqualTo("Unfulfilled");
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void calculateTotalPriceForPlan_nullReturnsZero() {
        when(orderedQuantityRepository.sumTotalPriceByPlanId(any(UUID.class))).thenReturn(null);
        assertThat(service.calculateTotalPriceForPlan(UUID.randomUUID().toString())).isEqualTo(0L);
    }
}