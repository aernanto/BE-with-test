package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageServiceImplTest {

    @Mock
    PackageRepository packageRepository;

    @Mock
    PlanRepository planRepository;

    @InjectMocks
    PackageServiceImpl service;

    Package pkg;

    @BeforeEach
    void setup() {
        pkg = TestDataFactory.pkg("pkg-1");
    }

    @Test
    void createPackage_success() {
        when(packageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Package saved = service.createPackage(pkg);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("Pending");
    }

    @Test
    void createPackage_invalidDates_throws() {
        pkg.setStartDate(LocalDateTime.now().plusDays(10));
        pkg.setEndDate(LocalDateTime.now().plusDays(5));
        assertThatThrownBy(() -> service.createPackage(pkg))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date");
    }

    @Test
    void createPackage_invalidQuota_throws() {
        pkg.setQuota(0);
        assertThatThrownBy(() -> service.createPackage(pkg))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quota");
    }

    @Test
    void createPackage_negativePrice_throws() {
        pkg.setPrice(-1L);
        assertThatThrownBy(() -> service.createPackage(pkg))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price");
    }

    @Test
    void updatePackage_notFound_throws() {
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updatePackage(pkg))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatePackage_processed_throws() {
        pkg.setStatus("Processed");
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        assertThatThrownBy(() -> service.updatePackage(pkg))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update processed package");
    }

    @Test
    void updatePackage_success() {
        Package existing = TestDataFactory.pkg("pkg-1");
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(existing));
        when(packageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        pkg.setPackageName("New Name");
        Package updated = service.updatePackage(pkg);

        assertThat(updated.getPackageName()).isEqualTo("New Name");
        verify(packageRepository).save(any(Package.class));
    }

    @Test
    void deletePackage_notFound_returnsFalse() {
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.empty());
        boolean result = service.deletePackage("pkg-1");
        assertThat(result).isFalse();
        verify(planRepository, never()).deleteByPackageId(anyString());
        verify(packageRepository, never()).deleteById(anyString());
    }

    @Test
    void deletePackage_processed_throws() {
        pkg.setStatus("Processed");
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        assertThatThrownBy(() -> service.deletePackage("pkg-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete processed package");
    }

    @Test
    void deletePackage_success_cascadesPlans() {
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        boolean result = service.deletePackage("pkg-1");
        assertThat(result).isTrue();
        verify(planRepository).deleteByPackageId("pkg-1");
        verify(packageRepository).deleteById("pkg-1");
    }

    @Test
    void processPackage_notFound_throws() {
        when(packageRepository.findByIdWithPlans("pkg-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.processPackage("pkg-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void processPackage_alreadyProcessed_throws() {
        pkg.setStatus("Processed");
        when(packageRepository.findByIdWithPlans("pkg-1")).thenReturn(Optional.of(pkg));
        assertThatThrownBy(() -> service.processPackage("pkg-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already processed");
    }

    @Test
    void processPackage_noPlans_throws() {
        pkg.setPlans(new ArrayList<>());
        when(packageRepository.findByIdWithPlans("pkg-1")).thenReturn(Optional.of(pkg));
        assertThatThrownBy(() -> service.processPackage("pkg-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without plans");
    }

    @Test
    void processPackage_incompletePlan_throws() {
        Plan p1 = TestDataFactory.plan("plan-1", "pkg-1");
        p1.setOrderedQuantities(new ArrayList<>()); // empty
        pkg.setPlans(List.of(p1));
        when(packageRepository.findByIdWithPlans("pkg-1")).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> service.processPackage("pkg-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("incomplete plans");
    }

    @Test
    void processPackage_success() {
        Plan p1 = TestDataFactory.plan("plan-1", "pkg-1");
        p1.setOrderedQuantities(List.of(TestDataFactory.oq("oq-1", "plan-1", "act-1")));

        pkg.setStatus("Pending");
        pkg.setPlans(List.of(p1));

        when(packageRepository.findByIdWithPlans("pkg-1")).thenReturn(Optional.of(pkg));

        service.processPackage("pkg-1");

        assertThat(pkg.getStatus()).isEqualTo("Processed");
        assertThat(p1.getStatus()).isEqualTo("Processed");
        verify(planRepository, times(1)).save(any(Plan.class));
        verify(packageRepository, times(1)).save(any(Package.class));
    }

    @Test
    void getPackagesByUserId_success() {
        when(packageRepository.findByUserId("user-123")).thenReturn(List.of(pkg));
        assertThat(service.getPackagesByUserId("user-123")).hasSize(1);
    }

    @Test
    void getPackagesByStatus_success() {
        when(packageRepository.findByStatus("Pending")).thenReturn(List.of(pkg));
        assertThat(service.getPackagesByStatus("Pending")).hasSize(1);
    }

    @Test
    void getPackageWithPlans_success() {
        when(packageRepository.findByIdWithPlans("pkg-1")).thenReturn(Optional.of(pkg));
        assertThat(service.getPackageWithPlans("pkg-1")).isPresent();
    }
}