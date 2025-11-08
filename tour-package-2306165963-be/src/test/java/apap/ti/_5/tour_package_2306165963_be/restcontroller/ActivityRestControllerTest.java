package apap.ti._5.tour_package_2306165963_be.restcontroller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.activity.*;
import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.restcontroller.ActivityRestController;
import apap.ti._5.tour_package_2306165963_be.service.ActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// PENTING: Menggunakan @WebMvcTest untuk menguji Controller secara terisolasi.
// Ini mencegah 404 karena Spring akan tahu cara memuat ActivityRestController.
@WebMvcTest(ActivityRestController.class)
public class ActivityRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Wajib di-Mock karena ActivityRestController menggunakannya
    private ActivityService activityService;

    @MockBean // Wajib di-Mock karena ActivityRestController menggunakannya
    private DtoMapper dtoMapper;

    @Autowired
    private ObjectMapper objectMapper;
    
    // Data dummy untuk digunakan dalam test
    private Activity activity;
    private ReadActivityDto readDto;
    private CreateActivityDto createDto;
    private UpdateActivityDto updateDto;
    private final String ACTIVITY_ID = "ACT001";
    private final String BASE_URL = "/api/activities";

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        
        // Activity Model
        activity = Activity.builder()
            .id(ACTIVITY_ID)
            .activityName("Snorkeling Trip")
            .activityType("Activity")
            .activityItem("Snorkel Gear Rental")
            .capacity(10)
            .price(100000L)
            .startDate(start)
            .endDate(end)
            .startLocation("Beach A")
            .endLocation("Beach B")
            .build();
        
        // Read DTO
        readDto = ReadActivityDto.builder()
            .id(ACTIVITY_ID)
            .activityName("Snorkeling Trip")
            .activityType("Activity")
            .activityItem("Snorkel Gear Rental")
            .capacity(10)
            .price(100000L)
            .startDate(start)
            .endDate(end)
            .startLocation("Beach A")
            .endLocation("Beach B")
            .build();
            
        // Create DTO
        createDto = CreateActivityDto.builder()
            .activityName("New Activity")
            .activityType("Flight")
            .activityItem("Garuda Flight")
            .capacity(50)
            .price(500000L)
            .startDate(start.plusDays(1))
            .endDate(end.plusDays(1))
            .startLocation("CGK")
            .endLocation("DPS")
            .build();
            
        // Update DTO
        updateDto = UpdateActivityDto.builder()
            .id(ACTIVITY_ID)
            .activityName("Updated Snorkeling Trip")
            .activityType("Activity")
            .activityItem("Updated Gear Rental")
            .capacity(15)
            .price(150000L)
            .startDate(start)
            .endDate(end)
            .startLocation("Updated A")
            .endLocation("Updated B")
            .build();
    }
    
    // --- Test Case 1: getAllActivities_ok() ---
    @Test
    void getAllActivities_ok() throws Exception {
        // Mocking Service and Mapper
        when(activityService.getAllActivities()).thenReturn(List.of(activity));
        when(dtoMapper.toReadDto(any(Activity.class))).thenReturn(readDto);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk()) // Expected: 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(ACTIVITY_ID));
        
        verify(activityService, times(1)).getAllActivities();
    }

    // --- Test Case 2: getActivityById_found() ---
    @Test
    void getActivityById_found() throws Exception {
        // Mocking Service and Mapper
        when(activityService.getActivityById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
        when(dtoMapper.toReadDto(activity)).thenReturn(readDto);

        mockMvc.perform(get(BASE_URL + "/{id}", ACTIVITY_ID))
                .andExpect(status().isOk()) // Expected: 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ACTIVITY_ID))
                .andExpect(jsonPath("$.activityName").value(activity.getActivityName()));
        
        verify(activityService, times(1)).getActivityById(ACTIVITY_ID);
    }
    
    // Test Case Tambahan: getActivityById_notFound()
    @Test
    void getActivityById_notFound() throws Exception {
        when(activityService.getActivityById(ACTIVITY_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/{id}", ACTIVITY_ID))
                .andExpect(status().isNotFound()) // Expected: 404
                .andExpect(content().string("")); 
        
        verify(activityService, times(1)).getActivityById(ACTIVITY_ID);
    }

    // --- Test Case 3: createActivity_created() ---
    @Test
    void createActivity_created() throws Exception {
        // Activity yang tersimpan
        Activity savedActivity = activity; 
        
        // Mocking Mapper dan Service
        when(dtoMapper.toEntity(any(CreateActivityDto.class))).thenReturn(activity);
        when(activityService.createActivity(any(Activity.class))).thenReturn(savedActivity);
        when(dtoMapper.toReadDto(savedActivity)).thenReturn(readDto);
        
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated()) // Expected: 201
                .andExpect(jsonPath("$.id").value(ACTIVITY_ID))
                .andExpect(jsonPath("$.activityType").value(activity.getActivityType())); 
        
        verify(activityService, times(1)).createActivity(any(Activity.class));
    }

    // --- Test Case 4: updateActivity_ok() ---
    @Test
    void updateActivity_ok() throws Exception {
        // Activity yang terupdate
        Activity updatedActivity = Activity.builder().id(ACTIVITY_ID).activityName("UPDATED").build(); 
        ReadActivityDto updatedReadDto = ReadActivityDto.builder().id(ACTIVITY_ID).activityName("UPDATED").build();

        // Mocking Mapper dan Service
        when(dtoMapper.toEntity(any(UpdateActivityDto.class))).thenReturn(updatedActivity);
        when(activityService.updateActivity(any(Activity.class))).thenReturn(updatedActivity);
        when(dtoMapper.toReadDto(updatedActivity)).thenReturn(updatedReadDto);

        mockMvc.perform(put(BASE_URL + "/{id}", ACTIVITY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk()) // Expected: 200
                .andExpect(jsonPath("$.id").value(ACTIVITY_ID))
                .andExpect(jsonPath("$.activityName").value("UPDATED")); 
        
        verify(activityService, times(1)).updateActivity(any(Activity.class));
    }

    // --- Test Case 5: deleteActivity_noContent() ---
    @Test
    void deleteActivity_noContent() throws Exception {
        // Service dipanggil tanpa error (berhasil dihapus)
        doNothing().when(activityService).deleteActivity(ACTIVITY_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", ACTIVITY_ID))
                .andExpect(status().isNoContent()) // Expected: 204
                .andExpect(content().string(""));
        
        verify(activityService, times(1)).deleteActivity(ACTIVITY_ID);
    }
    
    // --- Test Case 6: deleteActivity_conflictWhenInUse() ---
    @Test
    void deleteActivity_conflictWhenInUse() throws Exception {
        // Service melempar IllegalStateException (sedang digunakan/berhubungan dengan data lain)
        doThrow(new IllegalStateException("Activity is in use")).when(activityService).deleteActivity(ACTIVITY_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", ACTIVITY_ID))
                .andExpect(status().isConflict()) // Expected: 409
                .andExpect(content().string(""));
        
        verify(activityService, times(1)).deleteActivity(ACTIVITY_ID);
    }
}