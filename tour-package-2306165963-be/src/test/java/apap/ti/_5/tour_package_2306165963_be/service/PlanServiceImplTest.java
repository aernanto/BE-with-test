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
        plan = TestDataFactory.plan("plan-1", "pkg-1");
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
                .hasMessageContaining("processed package");
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
        assertThat(saved.getStatus()).isEqualTo("Unfinished");
    }

    @Test
    void updatePlan_notFound_throws() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updatePlan(plan))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatePlan_processed_throws() {
        Plan existing = TestDataFactory.plan("plan-1", "pkg-1");
        existing.setStatus("Processed");
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(existing));
        assertThatThrownBy(() -> service.updatePlan(plan))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("processed plan");
    }

    @Test
    void updatePlan_datesOutOfRange_throws() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        plan.setStartDate(pkg.getEndDate().plusDays(1));
        assertThatThrownBy(() -> service.updatePlan(plan))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("within package dates");
    }

    @Test
    void updatePlan_invalidType_throws() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        plan.setActivityType("InvalidType");
        assertThatThrownBy(() -> service.updatePlan(plan))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatePlan_success() {
        Plan existing = TestDataFactory.plan("plan-1", "pkg-1");
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(existing));
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(planRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        plan.setStartLocation("Bandung");
        Plan updated = service.updatePlan(plan);

        assertThat(updated.getStartLocation()).isEqualTo("Bandung");
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void deletePlan_notFound_returnsFalse() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.empty());
        boolean result = service.deletePlan("plan-1");
        assertThat(result).isFalse();
        verify(orderedQuantityRepository, never()).deleteByPlanId(anyString());
    }

    @Test
    void deletePlan_processed_throws() {
        plan.setStatus("Processed");
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        assertThatThrownBy(() -> service.deletePlan("plan-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("processed plan");
    }

    @Test
    void deletePlan_success_cascadesOQ() {
        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));
        boolean result = service.deletePlan("plan-1");
        assertThat(result).isTrue();
        verify(orderedQuantityRepository).deleteByPlanId("plan-1");
        verify(planRepository).deleteById("plan-1");
    }

    @Test
    void processPlan_notFound_throws() {
        when(planRepository.findByIdWithOrderedQuantities("plan-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.processPlan("plan-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void processPlan_alreadyProcessed_throws() {
        plan.setStatus("Processed");
        when(planRepository.findByIdWithOrderedQuantities("plan-1")).thenReturn(Optional.of(plan));
        assertThatThrownBy(() -> service.processPlan("plan-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already processed");
    }

    @Test
    void processPlan_noOQ_throws() {
        plan.setOrderedQuantities(List.of());
        when(planRepository.findByIdWithOrderedQuantities("plan-1")).thenReturn(Optional.of(plan));
        assertThatThrownBy(() -> service.processPlan("plan-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without ordered quantities");
    }

    @Test
    void processPlan_success_setsPriceFromSum() {
        plan.setStatus("Pending");
        plan.setOrderedQuantities(List.of(TestDataFactory.oq("oq-1","plan-1","act-1")));
        when(planRepository.findByIdWithOrderedQuantities("plan-1")).thenReturn(Optional.of(plan));
        when(orderedQuantityRepository.sumTotalPriceByPlanId("plan-1")).thenReturn(3000000L);

        service.processPlan("plan-1");

        assertThat(plan.getStatus()).isEqualTo("Processed");
        assertThat(plan.getPrice()).isEqualTo(3000000L);
        verify(planRepository).save(any(Plan.class));
    }

    @Test
    void calculateTotalPlanPrice_nullReturnsZero() {
        when(orderedQuantityRepository.sumTotalPriceByPlanId("plan-1")).thenReturn(null);
        assertThat(service.calculateTotalPlanPrice("plan-1")).isEqualTo(0L);
    }

    @Test
    void getPlansByPackageId_success() {
        when(planRepository.findByPackageId("pkg-1")).thenReturn(List.of(plan));
        assertThat(service.getPlansByPackageId("pkg-1")).hasSize(1);
    }
}