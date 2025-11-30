package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanServiceImplTest {

    @Mock
    PlanRepository planRepository;

    @Mock
    PackageRepository packageRepository;

    @Mock
    OrderedQuantityRepository orderedQuantityRepository;

    @InjectMocks
    PlanServiceImpl service;

    Package pkg;
    Plan plan;

    @BeforeEach
    void setup() {
        pkg = TestDataFactory.pkg("pkg-1");
        plan = TestDataFactory.plan(UUID.randomUUID(), "pkg-1");
    }

    @Test
    void createPlan_packageNotFound_throws() {
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createPlan("pkg-1", plan))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Package not found");
    }

    @Test
    void createPlan_packageProcessed_throws() {
        pkg.setStatus("Processed");
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        assertThatThrownBy(() -> service.createPlan("pkg-1", plan))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pending packages");
    }

    @Test
    void createPlan_datesOutOfRange_throws() {
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        plan.setStartDate(pkg.getStartDate().minusDays(1));
        assertThatThrownBy(() -> service.createPlan("pkg-1", plan))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("within package dates");
    }

    @Test
    void createPlan_invalidType_throws() {
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        plan.setActivityType("InvalidType");
        assertThatThrownBy(() -> service.createPlan("pkg-1", plan))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid activity type");
    }

    @Test
    void createPlan_success() {
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(planRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Plan saved = service.createPlan("pkg-1", plan);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPackageId()).isEqualTo("pkg-1");
        assertThat(saved.getStatus()).isEqualTo("Unfulfilled");
    }

    @Test
    void updatePlan_notFound_throws() {
        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updatePlan(plan))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatePlan_packageProcessed_throws() {
        Plan existing = TestDataFactory.plan(plan.getId(), "pkg-1");
        pkg.setStatus("Processed");
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(existing));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> service.updatePlan(plan))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pending packages");
    }

    @Test
    void updatePlan_datesOutOfRange_throws() {
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));

        // Package: now+1 to now+3
        // Plan: now+4 to now+5 (Out of range, but valid start<end)
        plan.setStartDate(pkg.getEndDate().plusDays(1));
        plan.setEndDate(pkg.getEndDate().plusDays(2));

        assertThatThrownBy(() -> service.updatePlan(plan))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("within package dates");
    }

    @Test
    void updatePlan_invalidType_throws() {
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        plan.setActivityType("InvalidType");
        assertThatThrownBy(() -> service.updatePlan(plan))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatePlan_success() {
        Plan existing = TestDataFactory.plan(plan.getId(), "pkg-1");
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(existing));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(planRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        plan.setStartLocation("Bandung");
        Plan updated = service.updatePlan(plan);

        assertThat(updated.getStartLocation()).isEqualTo("Bandung");
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void deletePlan_notFound_returnsFalse() {
        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        boolean result = service.deletePlan(UUID.randomUUID().toString());
        assertThat(result).isFalse();
        verify(orderedQuantityRepository, never()).deleteByPlanId(any(UUID.class));
    }

    @Test
    void deletePlan_packageProcessed_throws() {
        pkg.setStatus("Processed");
        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));

        String id = plan.getId().toString();
        assertThatThrownBy(() -> service.deletePlan(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pending packages");
    }

    @Test
    void deletePlan_success_cascadesOQ() {
        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.of(plan));
        // Need to mock package repository for status check
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));

        boolean result = service.deletePlan(plan.getId().toString());
        assertThat(result).isTrue();
        verify(orderedQuantityRepository).deleteByPlanId(plan.getId());
        verify(planRepository).deleteById(plan.getId());
    }

    @Test
    void processPlan_notFound_throws() {
        when(planRepository.findByIdWithOrderedQuantities(any(UUID.class))).thenReturn(Optional.empty());
        String id = UUID.randomUUID().toString();
        assertThatThrownBy(() -> service.processPlan(id))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void processPlan_alreadyProcessed_throws() {
        plan.setStatus("Fulfilled");
        when(planRepository.findByIdWithOrderedQuantities(any(UUID.class))).thenReturn(Optional.of(plan));
        String id = plan.getId().toString();
        assertThatThrownBy(() -> service.processPlan(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already fulfilled");
    }

    @Test
    void processPlan_noOQ_throws() {
        plan.setOrderedQuantities(List.of());
        when(planRepository.findByIdWithOrderedQuantities(any(UUID.class))).thenReturn(Optional.of(plan));
        String id = plan.getId().toString();
        assertThatThrownBy(() -> service.processPlan(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without ordered quantities");
    }

    @Test
    void processPlan_success_setsPriceFromSum() {
        plan.setStatus("Unfulfilled");
        plan.setOrderedQuantities(List.of(TestDataFactory.oq(UUID.randomUUID(), plan.getId(), "act-1")));

        // Mock package for quota check
        pkg.setQuota(100);
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));

        when(planRepository.findByIdWithOrderedQuantities(plan.getId())).thenReturn(Optional.of(plan));
        when(orderedQuantityRepository.sumTotalPriceByPlanId(plan.getId())).thenReturn(3000000L);

        service.processPlan(plan.getId().toString());

        // Total ordered quota is 2 (from TestDataFactory.oq default). Package quota is
        // 100.
        // So status should be Unfulfilled (unless I change logic or data).
        // Wait, logic: if (totalOrderedQuantity >= packageQuota) -> Fulfilled.
        // 2 < 100 -> Unfulfilled.
        // But I want to test success (Fulfilled?).
        // Or just verify price is set.

        assertThat(plan.getPrice()).isEqualTo(3000000L);
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void calculateTotalPlanPrice_nullReturnsZero() {
        when(orderedQuantityRepository.sumTotalPriceByPlanId(any(UUID.class))).thenReturn(null);
        assertThat(service.calculateTotalPlanPrice(UUID.randomUUID().toString())).isEqualTo(0L);
    }

    @Test
    void getPlansByPackageId_success() {
        when(planRepository.findByPackageId("pkg-1")).thenReturn(List.of(plan));
        assertThat(service.getPlansByPackageId("pkg-1")).hasSize(1);
    }
}