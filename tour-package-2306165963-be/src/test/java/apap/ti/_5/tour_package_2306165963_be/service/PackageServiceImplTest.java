package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.repository.ActivityRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageServiceImplTest {

    @Mock
    PackageRepository packageRepository;

    @Mock
    PlanRepository planRepository;

    @Mock
    ActivityRepository activityRepository;

    @InjectMocks
    PackageServiceImpl service;

    Package pkg;

    @BeforeEach
    void setup() {
        pkg = TestDataFactory.pkg("pkg-1");
    }

    @Test
    void createPackage_success() {
        when(packageRepository.countByUserId(anyString())).thenReturn(0L);
        when(packageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Package saved = service.createPackage(pkg);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).startsWith("PACK-user-123-");
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
    void updatePackage_processed_success() {
        pkg.setStatus("Processed");
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        when(packageRepository.save(any(Package.class))).thenReturn(pkg);

        Package updated = service.updatePackage(pkg);
        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo("Processed");
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
        verify(packageRepository, never()).save(any(Package.class));
    }

    @Test
    void deletePackage_processed_throws() {
        pkg.setStatus("Processed");
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        assertThatThrownBy(() -> service.deletePackage("pkg-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only delete packages with status Pending");
    }

    @Test
    void deletePackage_success_softDelete() {
        when(packageRepository.findById("pkg-1")).thenReturn(Optional.of(pkg));
        boolean result = service.deletePackage("pkg-1");
        assertThat(result).isTrue();
        assertThat(pkg.isDeleted()).isTrue();
        verify(packageRepository).save(pkg);
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
                .hasMessageContaining("Only Pending packages can be processed");
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
        Plan p1 = TestDataFactory.plan(UUID.randomUUID(), "pkg-1");
        p1.setOrderedQuantities(new ArrayList<>()); // empty
        pkg.setPlans(List.of(p1));
        when(packageRepository.findByIdWithPlans("pkg-1")).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> service.processPackage("pkg-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unfulfilled plans");
    }

    @Test
    void processPackage_success() {
        Plan p1 = TestDataFactory.plan(UUID.randomUUID(), "pkg-1");
        p1.setStatus("Fulfilled");
        p1.setOrderedQuantities(List.of(TestDataFactory.oq(UUID.randomUUID(), p1.getId(), "act-1")));

        pkg.setStatus("Pending");
        pkg.setPlans(List.of(p1));

        Activity activity = TestDataFactory.activity("act-1");
        activity.setCapacity(100);

        when(packageRepository.findByIdWithPlans("pkg-1")).thenReturn(Optional.of(pkg));
        when(activityRepository.findById("act-1")).thenReturn(Optional.of(activity));

        service.processPackage("pkg-1");

        assertThat(pkg.getStatus()).isEqualTo("Processed");
        assertThat(activity.getCapacity()).isEqualTo(98); // 100 - 2
        verify(activityRepository, times(1)).save(any(Activity.class));
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