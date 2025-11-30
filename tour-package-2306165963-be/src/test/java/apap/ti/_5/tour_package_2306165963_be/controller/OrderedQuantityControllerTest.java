package apap.ti._5.tour_package_2306165963_be.controller;

import apap.ti._5.tour_package_2306165963_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165963_be.service.OrderedQuantityService;
import apap.ti._5.tour_package_2306165963_be.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderedQuantityController.class)
class OrderedQuantityControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrderedQuantityService orderedQuantityService;

    @Autowired
    ObjectMapper objectMapper;

    private final UUID oqId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID planId = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void getOrderedQuantityById_found() throws Exception {
        when(orderedQuantityService.getOrderedQuantityById(oqId.toString()))
                .thenReturn(Optional.of(TestDataFactory.oq(oqId, planId, "act-1")));

        mockMvc.perform(get("/api/ordered-quantities/" + oqId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(oqId.toString())));
    }

    @Test
    void createOrderedQuantity_created() throws Exception {
        OrderedQuantity req = new OrderedQuantity();
        req.setActivityId("act-1");
        req.setOrderedQuota(2);

        OrderedQuantity resp = TestDataFactory.oq(oqId, planId, "act-1");
        when(orderedQuantityService.createOrderedQuantity(eq(planId.toString()), any())).thenReturn(resp);

        mockMvc.perform(post("/api/plans/" + planId + "/ordered-quantities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(oqId.toString())));
    }

    @Test
    void updateOrderedQuantity_ok() throws Exception {
        OrderedQuantity resp = TestDataFactory.oq(oqId, planId, "act-1");
        resp.setOrderedQuota(5);
        when(orderedQuantityService.updateOrderedQuantity(oqId.toString(), 5)).thenReturn(resp);

        mockMvc.perform(put("/api/ordered-quantities/" + oqId).param("newQuota", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderedQuota", is(5)));
    }
}