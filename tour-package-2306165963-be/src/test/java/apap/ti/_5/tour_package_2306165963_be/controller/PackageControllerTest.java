package apap.ti._5.tour_package_2306165963_be.controller;

import apap.ti._5.tour_package_2306165963_be.model.Package;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Uji hanya PackageController
@WebMvcTest(PackageController.class)
class PackageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Untuk memanggil method getAllPackages() di Controller (sesuai P01)
    @Autowired
    private PackageController controller;

    private String existingId;
    private String fakeId = UUID.randomUUID().toString();

    @BeforeEach
    void setup() {
        // Ambil ID dari data dummy pertama yang dibuat di Controller
        if (!controller.getAllPackages().isEmpty()) {
            existingId = controller.getAllPackages().get(0).getId();
        } else {
            // Jika kosong, masukkan data dummy baru untuk testing
            Package p = Package.builder().id(UUID.randomUUID().toString()).packageName("Test Package").status("Pending").quota(1).price(1000000L).build();
            controller.getAllPackages().add(p);
            existingId = p.getId();
        }
    }

    // --- READ Tests ---

    @Test
    void testGetAllPackages() throws Exception {
        mockMvc.perform(get("/package"))
                .andExpect(status().isOk())
                .andExpect(view().name("package/view-all"))
                .andExpect(model().attributeExists("listPackage"));
    }

    @Test
    void testGetPackageByIdFound() throws Exception {
        mockMvc.perform(get("/package/" + existingId))
                .andExpect(status().isOk())
                .andExpect(view().name("package/detail"))
                .andExpect(model().attributeExists("currentPackage"));
    }

    @Test
    void testGetPackageByIdNotFound() throws Exception {
        mockMvc.perform(get("/package/" + fakeId))
                .andExpect(status().isOk()) // Masih 200 OK karena me-return error/404 view
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("message"));
    }
    
    // --- CREATE Tests ---

    @Test
    void testFormCreatePackage() throws Exception {
        mockMvc.perform(get("/package/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("package/form"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attributeExists("packageData"));
    }

    @Test
    void testCreatePackageSuccess() throws Exception {
        mockMvc.perform(post("/package/create")
                // Paramater yang dibutuhkan oleh Package Model
                .param("packageName", "New Test Package")
                .param("quota", "3")
                .param("price", "5000000")
                .param("startDate", "2026-01-01T10:00") // Format LocalDateTime
                .param("endDate", "2026-01-05T18:00")
                .param("userId", "testUser"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/package"));
    }

    // --- UPDATE Tests ---

    @Test
    void testFormEditPackageFound() throws Exception {
        mockMvc.perform(get("/package/update/" + existingId))
                .andExpect(status().isOk())
                .andExpect(view().name("package/form"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attributeExists("packageData"));
    }

    @Test
    void testFormEditPackageNotFound() throws Exception {
        mockMvc.perform(get("/package/update/" + fakeId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void testUpdatePackageSuccess() throws Exception {
        mockMvc.perform(post("/package/update/" + existingId) // Menggunakan POST
                .param("packageName", "Updated Package Name")
                .param("quota", "10")
                .param("price", "10000000")
                .param("status", "Processed")
                .param("startDate", "2025-11-01T09:00") 
                .param("endDate", "2025-11-07T18:00")
                .param("userId", "user001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/package"));
    }
    
    // --- DELETE Tests ---

    @Test
    void testDeletePackageFound() throws Exception {
        // Buat ID baru yang akan dihapus agar tidak mengganggu data existingId
        String idToDelete = UUID.randomUUID().toString();
        controller.getAllPackages().add(Package.builder().id(idToDelete).packageName("To Delete").build());

        mockMvc.perform(post("/package/delete/" + idToDelete)) // Menggunakan POST
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/package"));
    }

    @Test
    void testDeletePackageNotFound() throws Exception {
        mockMvc.perform(post("/package/delete/" + fakeId)) // Menggunakan POST
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/package"));
    }
}
