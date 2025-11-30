package apap.ti._5.tour_package_2306165963_be.controller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.plan.CreatePlanDto;
import apap.ti._5.tour_package_2306165963_be.dto.plan.UpdatePlanDto;
import apap.ti._5.tour_package_2306165963_be.model.Plan;
import apap.ti._5.tour_package_2306165963_be.restcontroller.PlanRestController;
import apap.ti._5.tour_package_2306165963_be.service.PlanService;
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

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanRestController.class)
@ContextConfiguration(classes = { PlanControllerTest.TestConfig.class })
class PlanControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PlanService planService;

    @Autowired
    ObjectMapper objectMapper;

    private final UUID planId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Configuration
    static class TestConfig {
        @Bean
        DtoMapper dtoMapper() {
            return new DtoMapper(); // HARUS PUNYA CONSTRUCTOR KOSONG
        }
    }

    @Test
    void getPlanById_found() throws Exception {
        Plan plan = TestDataFactory.plan(planId, "pkg-1");
        when(planService.getPlanById(planId.toString())).thenReturn(Optional.of(plan));

        mockMvc.perform(get("/api/plans/" + planId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(planId.toString())));
    }

    @Test
    void getPlanById_notFound() throws Exception {
        when(planService.getPlanById("x")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/plans/x"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPlan_created() throws Exception {
        CreatePlanDto req = new CreatePlanDto();
        req.setActivityType("Flight");
        req.setPrice(5000000L);

        Plan resp = TestDataFactory.plan(planId, "pkg-1");
        when(planService.createPlan(eq("pkg-1"), any())).thenReturn(resp);

        mockMvc.perform(post("/api/packages/pkg-1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(planId.toString())));
    }

    @Test
    void updatePlan_ok() throws Exception {
        UpdatePlanDto req = new UpdatePlanDto();
        req.setId(planId.toString());
        req.setPrice(6000000L);

        Plan saved = TestDataFactory.plan(planId, "pkg-1");
        saved.setPrice(6000000L);

        when(planService.updatePlan(any())).thenReturn(saved);

        mockMvc.perform(put("/api/plans/" + planId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price", is(6000000)));
    }

    @Test
    void deletePlan_noContent() throws Exception {
        when(planService.deletePlan(planId.toString())).thenReturn(true);

        mockMvc.perform(delete("/api/plans/" + planId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePlan_conflict_whenProcessed() throws Exception {
        Mockito.doThrow(new IllegalStateException("Cannot delete processed plan"))
                .when(planService).deletePlan(planId.toString());

        mockMvc.perform(delete("/api/plans/" + planId))
                .andExpect(status().isConflict());
    }

    @Test
    void processPlan_ok() throws Exception {
        Mockito.doNothing().when(planService).processPlan(planId.toString());

        mockMvc.perform(post("/api/plans/" + planId + "/process"))
                .andExpect(status().isNoContent());
    }

    @Test
    void processPlan_badRequest_whenError() throws Exception {
        Mockito.doThrow(new RuntimeException("Error"))
                .when(planService).processPlan(planId.toString());

        mockMvc.perform(post("/api/plans/" + planId + "/process"))
                .andExpect(status().isBadRequest());
    }
}