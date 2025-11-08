package apap.ti._5.tour_package_2306165963_be.controller;

import apap.ti._5.tour_package_2306165963_be.dto.DtoMapper;
import apap.ti._5.tour_package_2306165963_be.dto.packagedto.UpdatePackageDto;
import apap.ti._5.tour_package_2306165963_be.model.Package;
import apap.ti._5.tour_package_2306165963_be.restcontroller.PackageRestController;
import apap.ti._5.tour_package_2306165963_be.service.PackageService;
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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PackageRestController.class)
@ContextConfiguration(classes = {PackageControllerTest.TestConfig.class})
class PackageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PackageService packageService;

    @Autowired
    ObjectMapper objectMapper;

    @Configuration
    static class TestConfig {
        @Bean
        DtoMapper dtoMapper() {
            return new DtoMapper(); // HARUS PUNYA CONSTRUCTOR KOSONG
        }
    }

    @Test
    void getAllPackages_ok() throws Exception {
        when(packageService.getAllPackages()).thenReturn(List.of(TestDataFactory.pkg("pkg-1")));

        mockMvc.perform(get("/api/packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("pkg-1")));
    }

    @Test
    void getPackageById_found() throws Exception {
        when(packageService.getPackageById("pkg-1")).thenReturn(Optional.of(TestDataFactory.pkg("pkg-1")));

        mockMvc.perform(get("/api/packages/pkg-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("pkg-1")));
    }

    @Test
    void getPackageById_notFound() throws Exception {
        when(packageService.getPackageById("x")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/packages/x"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPackage_created() throws Exception {
        Package req = TestDataFactory.pkg(null);
        Package resp = TestDataFactory.pkg("pkg-1");
        when(packageService.createPackage(any())).thenReturn(resp);

        mockMvc.perform(post("/api/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("pkg-1")));
    }

    @Test
    void updatePackage_ok() throws Exception {
        UpdatePackageDto req = new UpdatePackageDto();
        req.setPackageName("Updated Package");

        Package saved = TestDataFactory.pkg("pkg-1");
        saved.setPackageName("Updated Package");

        when(packageService.updatePackage(any())).thenReturn(saved);

        mockMvc.perform(put("/api/packages/pkg-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packageName", is("Updated Package")));
    }

    @Test
    void deletePackage_noContent() throws Exception {
        when(packageService.deletePackage("pkg-1")).thenReturn(true);

        mockMvc.perform(delete("/api/packages/pkg-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePackage_conflict_whenProcessed() throws Exception {
        Mockito.doThrow(new IllegalStateException("Cannot delete processed package"))
               .when(packageService).deletePackage("pkg-1");

        mockMvc.perform(delete("/api/packages/pkg-1"))
                .andExpect(status().isConflict());
    }

    @Test
    void processPackage_ok() throws Exception {
        Mockito.doNothing().when(packageService).processPackage("pkg-1");

        mockMvc.perform(post("/api/packages/pkg-1/process"))
                .andExpect(status().isNoContent());
    }

    @Test
    void processPackage_badRequest_whenError() throws Exception {
        Mockito.doThrow(new RuntimeException("Error"))
               .when(packageService).processPackage("pkg-1");

        mockMvc.perform(post("/api/packages/pkg-1/process"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPackagesByUser_ok() throws Exception {
        when(packageService.getPackagesByUserId("user-1"))
                .thenReturn(List.of(TestDataFactory.pkg("pkg-1")));

        mockMvc.perform(get("/api/packages/user/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("pkg-1")));
    }

    @Test
    void getPackagesByStatus_ok() throws Exception {
        when(packageService.getPackagesByStatus("Pending"))
                .thenReturn(List.of(TestDataFactory.pkg("pkg-1")));

        mockMvc.perform(get("/api/packages/status/Pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("pkg-1")));
    }
}