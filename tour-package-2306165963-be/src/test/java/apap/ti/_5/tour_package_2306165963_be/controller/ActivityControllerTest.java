package apap.ti._5.tour_package_2306165963_be.controller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.activity.CreateActivityDto;
import apap.ti._5.tour_package_2306165963_be.dto.activity.ReadActivityDto;
import apap.ti._5.tour_package_2306165963_be.dto.activity.UpdateActivityDto;
import apap.ti._5.tour_package_2306165963_be.model.Activity;
import apap.ti._5.tour_package_2306165963_be.restcontroller.ActivityRestController;
import apap.ti._5.tour_package_2306165963_be.service.ActivityService;
import apap.ti._5.tour_package_2306165963_be.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityRestController.class)
@ContextConfiguration(classes = {ActivityRestControllerTest.TestConfig.class})
class ActivityRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ActivityService activityService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DtoMapper dtoMapper;  // Tambah ini untuk convert di test

    @Configuration
    static class TestConfig {
        @Bean
        DtoMapper dtoMapper() {
            return new DtoMapper();
        }
    }

    @Test
    void getAllActivities_ok() throws Exception {
        Activity activity = TestDataFactory.activity("act-1");
        dtoMapper.toReadDto(activity);
        when(activityService.getAllActivities()).thenReturn(List.of(activity));

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("act-1")));
    }

    @Test
    void getActivityById_found() throws Exception {
        Activity activity = TestDataFactory.activity("act-1");
        when(activityService.getActivityById("act-1")).thenReturn(Optional.of(activity));

        mockMvc.perform(get("/api/activities/act-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("act-1")));
    }

    @Test
    void getActivityById_notFound() throws Exception {
        when(activityService.getActivityById("x")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/activities/x"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createActivity_created() throws Exception {
        CreateActivityDto reqDto = CreateActivityDto.builder()
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

        Activity savedActivity = TestDataFactory.activity("act-1");
        when(activityService.createActivity(any(Activity.class))).thenReturn(savedActivity);

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("act-1")));
    }

    @Test
    void updateActivity_ok() throws Exception {
        UpdateActivityDto reqDto = UpdateActivityDto.builder()
                .id("act-1")
                .activityName("Updated Name")
                .activityType("Accommodation")
                .activityItem("Deluxe Room")
                .capacity(50)
                .price(750000L)
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(10))
                .startLocation("Jakarta")
                .endLocation("Jakarta")
                .build();

        Activity updatedActivity = TestDataFactory.activity("act-1");
        updatedActivity.setActivityName("Updated Name");
        when(activityService.updateActivity(any(Activity.class))).thenReturn(updatedActivity);

        mockMvc.perform(put("/api/activities/act-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityName", is("Updated Name")));
    }

    @Test
    void deleteActivity_noContent() throws Exception {
        when(activityService.deleteActivity("act-1")).thenReturn(true);

        mockMvc.perform(delete("/api/activities/act-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteActivity_conflictWhenInUse() throws Exception {
        Mockito.doThrow(new IllegalStateException("Cannot delete"))
               .when(activityService).deleteActivity("act-1");

        mockMvc.perform(delete("/api/activities/act-1"))
                .andExpect(status().isConflict());
    }
}