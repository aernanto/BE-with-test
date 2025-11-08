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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
  Assumsi endpoint:
  - GET /api/ordered-quantities/{id}
  - POST /api/plans/{planId}/ordered-quantities
  - PUT /api/ordered-quantities/{id}?newQuota=5
  - DELETE /api/ordered-quantities/{id}
*/
@WebMvcTest(OrderedQuantityController.class)
class OrderedQuantityControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrderedQuantityService orderedQuantityService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getOrderedQuantityById_found() throws Exception {
        when(orderedQuantityService.getOrderedQuantityById("oq-1"))
                .thenReturn(Optional.of(TestDataFactory.oq("oq-1","plan-1","act-1")));

        mockMvc.perform(get("/api/ordered-quantities/oq-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("oq-1")));
    }

    @Test
    void createOrderedQuantity_created() throws Exception {
        OrderedQuantity req = new OrderedQuantity();
        req.setActivityId("act-1");
        req.setOrderedQuota(2);

        OrderedQuantity resp = TestDataFactory.oq("oq-1","plan-1","act-1");
        when(orderedQuantityService.createOrderedQuantity(eq("plan-1"), any())).thenReturn(resp);

        mockMvc.perform(post("/api/plans/plan-1/ordered-quantities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("oq-1")));
    }

    @Test
    void updateOrderedQuantity_ok() throws Exception {
        OrderedQuantity resp = TestDataFactory.oq("oq-1","plan-1","act-1");
        resp.setOrderedQuota(5);
        when(orderedQuantityService.updateOrderedQuantity("oq-1", 5)).thenReturn(resp);

        mockMvc.perform(put("/api/ordered-quantities/oq-1").param("newQuota","5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderedQuota", is(5)));
    }
}