package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165963_be.repository.OrderedQuantityRepository;
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

    @InjectMocks
    OrderedQuantityServiceImpl service;

    Plan plan;
    Activity activity;

    @BeforeEach
    void setup() {
        plan = TestDataFactory.plan("plan-1", "pkg-1");
        plan.setActivityType("Accommodation");
        activity = TestDataFactory.activity("act-1");
        activity.setActivityType("Accommodation");
    }

    @Test
    void createOrderedQuantity_planNotFound_throws() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createOrderedQuantity("plan-1", TestDataFactory.oq("oq-1","plan-1","act-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Plan not found");
    }

    @Test
    void createOrderedQuantity_planProcessed_throws() {
        plan.setStatus("Processed");
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        assertThatThrownBy(() -> service.createOrderedQuantity("plan-1", TestDataFactory.oq("oq-1","plan-1","act-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("processed plan");
    }

    @Test
    void createOrderedQuantity_activityNotFound_throws() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        when(activityRepository.findById("act-1")).thenReturn(Optional.empty());
        OrderedQuantity rq = TestDataFactory.oq("oq-1", "plan-1", "act-1");
        assertThatThrownBy(() -> service.createOrderedQuantity("plan-1", rq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Activity not found");
    }

    @Test
    void createOrderedQuantity_typeMismatch_throws() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        activity.setActivityType("Flight");
        when(activityRepository.findById("act-1")).thenReturn(Optional.of(activity));
        OrderedQuantity rq = TestDataFactory.oq("oq-1", "plan-1", "act-1");
        assertThatThrownBy(() -> service.createOrderedQuantity("plan-1", rq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void createOrderedQuantity_duplicate_throws() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        when(activityRepository.findById("act-1")).thenReturn(Optional.of(activity));
        when(orderedQuantityRepository.findByPlanIdAndActivityId("plan-1","act-1"))
                .thenReturn(List.of(TestDataFactory.oq("oq-1","plan-1","act-1")));

        OrderedQuantity rq = TestDataFactory.oq("oq-2", "plan-1", "act-1");
        assertThatThrownBy(() -> service.createOrderedQuantity("plan-1", rq))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already added");
    }

    @Test
    void createOrderedQuantity_quotaExceeds_throws() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        when(activityRepository.findById("act-1")).thenReturn(Optional.of(activity));
        when(orderedQuantityRepository.findByPlanIdAndActivityId("plan-1","act-1")).thenReturn(List.of());

        OrderedQuantity rq = TestDataFactory.oq("oq-2", "plan-1", "act-1");
        rq.setOrderedQuota(activity.getCapacity() + 1);

        assertThatThrownBy(() -> service.createOrderedQuantity("plan-1", rq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds activity capacity");
    }

    @Test
    void createOrderedQuantity_success_setsCopies_andPlanStatus() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        when(activityRepository.findById("act-1")).thenReturn(Optional.of(activity));
        when(orderedQuantityRepository.findByPlanIdAndActivityId("plan-1","act-1")).thenReturn(List.of());
        when(orderedQuantityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderedQuantity rq = new OrderedQuantity();
        rq.setActivityId("act-1");
        rq.setOrderedQuota(2);

        OrderedQuantity saved = service.createOrderedQuantity("plan-1", rq);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPlanId()).isEqualTo("plan-1");
        assertThat(saved.getActivityName()).isEqualTo(activity.getActivityName());
        assertThat(saved.getQuota()).isEqualTo(activity.getCapacity());
        // Plan status should move from Unfinished -> Pending
        assertThat(plan.getStatus()).isEqualTo("Pending");
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void updateOrderedQuantity_notFound_throws() {
        when(orderedQuantityRepository.findById("oq-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateOrderedQuantity("oq-1", 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateOrderedQuantity_planProcessed_throws() {
        OrderedQuantity existing = TestDataFactory.oq("oq-1","plan-1","act-1");
        when(orderedQuantityRepository.findById("oq-1")).thenReturn(Optional.of(existing));
        plan.setStatus("Processed");
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        assertThatThrownBy(() -> service.updateOrderedQuantity("oq-1", 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("processed plan");
    }

    @Test
    void updateOrderedQuantity_nonPositive_throws() {
        OrderedQuantity existing = TestDataFactory.oq("oq-1","plan-1","act-1");
        when(orderedQuantityRepository.findById("oq-1")).thenReturn(Optional.of(existing));
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        assertThatThrownBy(() -> service.updateOrderedQuantity("oq-1", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than 0");
    }

    @Test
    void updateOrderedQuantity_exceedsCapacity_throws() {
        OrderedQuantity existing = TestDataFactory.oq("oq-1","plan-1","act-1");
        existing.setQuota(3);
        when(orderedQuantityRepository.findById("oq-1")).thenReturn(Optional.of(existing));
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        assertThatThrownBy(() -> service.updateOrderedQuantity("oq-1", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds activity capacity");
    }

    @Test
    void updateOrderedQuantity_success() {
        OrderedQuantity existing = TestDataFactory.oq("oq-1","plan-1","act-1");
        existing.setQuota(10);
        when(orderedQuantityRepository.findById("oq-1")).thenReturn(Optional.of(existing));
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        when(orderedQuantityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderedQuantity updated = service.updateOrderedQuantity("oq-1", 7);

        assertThat(updated.getOrderedQuota()).isEqualTo(7);
        verify(orderedQuantityRepository).save(any(OrderedQuantity.class));
    }

    @Test
    void deleteOrderedQuantity_notFound_returnsFalse() {
        when(orderedQuantityRepository.findById("oq-1")).thenReturn(Optional.empty());
        assertThat(service.deleteOrderedQuantity("oq-1")).isFalse();
    }

    @Test
    void deleteOrderedQuantity_planProcessed_throws() {
        OrderedQuantity existing = TestDataFactory.oq("oq-1","plan-1","act-1");
        when(orderedQuantityRepository.findById("oq-1")).thenReturn(Optional.of(existing));
        plan.setStatus("Processed");
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> service.deleteOrderedQuantity("oq-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("processed plan");
    }

    @Test
    void deleteOrderedQuantity_success_andPlanStatusUpdated() {
        OrderedQuantity existing = TestDataFactory.oq("oq-1","plan-1","act-1");
        when(orderedQuantityRepository.findById("oq-1")).thenReturn(Optional.of(existing));
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        when(orderedQuantityRepository.countByPlanId("plan-1")).thenReturn(0L);

        boolean result = service.deleteOrderedQuantity("oq-1");

        assertThat(result).isTrue();
        verify(orderedQuantityRepository).deleteById("oq-1");
        // Plan status back to Unfinished
        assertThat(plan.getStatus()).isEqualTo("Unfinished");
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void calculateTotalPriceForPlan_nullReturnsZero() {
        when(orderedQuantityRepository.sumTotalPriceByPlanId("plan-1")).thenReturn(null);
        assertThat(service.calculateTotalPriceForPlan("plan-1")).isEqualTo(0L);
    }
}