package apap.ti._5.tour_package_2306165963_be.controller;

import apap.ti._5.tour_package_2306165963_be.model.Package;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/package")
public class PackageController {

    // Database Dummy (sesuai UserProfileController di P01)
    private final List<Package> packageDB = new ArrayList<>();

    // Constructor untuk mengisi dummy data saat aplikasi dimulai
    public PackageController() {
        // Dummy data 1: Processed
        packageDB.add(Package.builder()
                .id(UUID.randomUUID().toString())
                .userId("user001")
                .packageName("Bali Honeymoon Bliss")
                .quota(2)
                .price(18500000L)
                .status("Processed")
                .startDate(LocalDateTime.of(2025, 11, 1, 9, 0))
                .endDate(LocalDateTime.of(2025, 11, 7, 18, 0))
                .build());

        // Dummy data 2: Pending
        packageDB.add(Package.builder()
                .id(UUID.randomUUID().toString())
                .userId("user002")
                .packageName("Lombok Island Adventure")
                .quota(5)
                .price(9200000L)
                .status("Pending")
                .startDate(LocalDateTime.of(2025, 12, 10, 10, 0))
                .endDate(LocalDateTime.of(2025, 12, 15, 20, 0))
                .build());
    }

    // Helper: Digunakan untuk Unit Test
    public List<Package> getAllPackages() {
        return packageDB;
    }

    // -- Aksi Controller (CRUD) --

    // GET /package (Read All - All Packages)
    @GetMapping
    public String getAllPackage(HttpServletRequest request, Model model) {
        // Menggunakan Thymeleaf untuk menampilkan semua paket (view-all)
        model.addAttribute("listPackage", packageDB);
        model.addAttribute("currentUri", request.getRequestURI());
        return "package/view-all";
    }

    // GET /package/{id} (Detail Package)
    @GetMapping("/{id}")
    public String getPackageById(@PathVariable String id, Model model) {
        Optional<Package> packageOptional = packageDB.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();

        if (packageOptional.isEmpty()) {
            model.addAttribute("title", "Package Not Found");
            model.addAttribute("message", "Package with ID " + id + " not found.");
            return "error/404";
        }
        
        model.addAttribute("currentPackage", packageOptional.get());
        return "package/detail";
    }

    // GET /package/create (Create Package - Form)
    @GetMapping("/create")
    public String formCreatePackage(Model model) {
        model.addAttribute("isEdit", false);
        model.addAttribute("packageData", new Package());
        return "package/form";
    }

    // POST /package/create (Create Package - Submit)
    @PostMapping("/create")
    public String createPackage(@ModelAttribute Package packageData, RedirectAttributes redirectAttributes) {
        // Set data default
        packageData.setId(UUID.randomUUID().toString());
        packageData.setStatus("Pending"); 
        
        packageDB.add(packageData);
        
        redirectAttributes.addFlashAttribute("successMessage", 
            "Successfully created new package: " + packageData.getPackageName());
            
        return "redirect:/package";
    }

    // GET /package/update/{id} (Update Package - Form)
    @GetMapping("/update/{id}")
    public String formEditPackage(@PathVariable String id, Model model) {
        Optional<Package> packageOptional = packageDB.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();

        if (packageOptional.isEmpty()) {
            model.addAttribute("title", "Package Not Found");
            model.addAttribute("message", "Package with ID " + id + " not found for editing.");
            return "error/404";
        }

        model.addAttribute("isEdit", true);
        model.addAttribute("packageId", id);
        model.addAttribute("packageData", packageOptional.get());
        return "package/form";
    }

    // POST/PUT /package/update/{id} (Update Package - Submit)
    // Spring MVC menggunakan POST untuk form submit, dan kita pakai HiddenHttpMethodFilter untuk simulasi PUT
    @PostMapping("/update/{id}")
    public String updatePackage(@PathVariable String id, @ModelAttribute Package updatedPackage, RedirectAttributes redirectAttributes) {
        
        // Cari index package yang akan diupdate
        int index = -1;
        for (int i = 0; i < packageDB.size(); i++) {
            if (packageDB.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        
        if (index != -1) {
            // Pertahankan ID lama dan Status (jika tidak diubah dari form)
            updatedPackage.setId(id);
            // Tambahkan logika bisnis lain jika perlu
            
            packageDB.set(index, updatedPackage);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Successfully updated package: " + updatedPackage.getPackageName());
        } else {
             redirectAttributes.addFlashAttribute("errorMessage", 
                "Package with ID " + id + " not found for updating.");
        }

        return "redirect:/package";
    }
    
    // POST /package/delete/{id} (Delete Package - Submit)
    // Catatan: Karena Thymeleaf tidak mendukung DELETE method di form, kita akan simulasi DELETE dengan POST
    @PostMapping("/delete/{id}") 
    public String deletePackage(@PathVariable String id, RedirectAttributes redirectAttributes) {
        
        boolean removed = packageDB.removeIf(p -> p.getId().equals(id));

        if (removed) {
            redirectAttributes.addFlashAttribute("successMessage", 
                "Successfully deleted package with ID: " + id);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Package with ID " + id + " not found for deletion.");
        }
        
        return "redirect:/package";
    }
}
