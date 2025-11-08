package apap.ti._5.tour_package_2306165963_be.service;

import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165963_be.repository.OrderedQuantityRepository;
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
class ActivityServiceImplTest {

    @Mock
    ActivityRepository activityRepository;

    @Mock
    OrderedQuantityRepository orderedQuantityRepository;

    @InjectMocks
    ActivityServiceImpl service;

    Activity valid;

    @BeforeEach
    void setup() {
        valid = TestDataFactory.activity("act-1");
    }

    @Test
    void createActivity_success() {
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Activity saved = service.createActivity(valid);

        assertThat(saved.getId()).isNotNull();
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    void createActivity_invalidDates_throws() {
        valid.setStartDate(LocalDateTime.now().plusDays(10));
        valid.setEndDate(LocalDateTime.now().plusDays(5));
        assertThatThrownBy(() -> service.createActivity(valid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date must be after start date");
        verify(activityRepository, never()).save(any());
    }

    @Test
    void createActivity_invalidCapacity_throws() {
        valid.setCapacity(0);
        assertThatThrownBy(() -> service.createActivity(valid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capacity");
    }

    @Test
    void createActivity_negativePrice_throws() {
        valid.setPrice(-1L);
        assertThatThrownBy(() -> service.createActivity(valid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price");
    }

    @Test
    void createActivity_invalidType_throws() {
        valid.setActivityType("InvalidType");
        assertThatThrownBy(() -> service.createActivity(valid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid activity type");
    }

    @Test
    void updateActivity_success() {
        when(activityRepository.findById("act-1")).thenReturn(Optional.of(valid));
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Activity update = TestDataFactory.activity("act-1");
        update.setActivityName("Hotel Bandung");

        Activity saved = service.updateActivity(update);

        assertThat(saved.getActivityName()).isEqualTo("Hotel Bandung");
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void updateActivity_notFound_throws() {
        when(activityRepository.findById("act-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateActivity(valid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteActivity_inUse_throws() {
        when(orderedQuantityRepository.existsByActivityId("act-1")).thenReturn(true);
        assertThatThrownBy(() -> service.deleteActivity("act-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete activity");
        verify(activityRepository, never()).deleteById(anyString());
    }

    @Test
    void deleteActivity_exists_true() {
        when(orderedQuantityRepository.existsByActivityId("act-1")).thenReturn(false);
        when(activityRepository.existsById("act-1")).thenReturn(true);

        boolean result = service.deleteActivity("act-1");

        assertThat(result).isTrue();
        verify(activityRepository).deleteById("act-1");
    }

    @Test
    void deleteActivity_notExists_false() {
        when(orderedQuantityRepository.existsByActivityId("act-1")).thenReturn(false);
        when(activityRepository.existsById("act-1")).thenReturn(false);

        boolean result = service.deleteActivity("act-1");

        assertThat(result).isFalse();
        verify(activityRepository, never()).deleteById(anyString());
    }

    @Test
    void getActivitiesByType_success() {
        when(activityRepository.findByActivityType("Accommodation")).thenReturn(List.of(valid));
        assertThat(service.getActivitiesByActivityType("Accommodation")).hasSize(1);
    }

    @Test
    void searchActivitiesByName_success() {
        when(activityRepository.findByActivityNameContainingIgnoreCase("hotel")).thenReturn(List.of(valid));
        assertThat(service.searchActivitiesByName("hotel")).hasSize(1);
    }
}