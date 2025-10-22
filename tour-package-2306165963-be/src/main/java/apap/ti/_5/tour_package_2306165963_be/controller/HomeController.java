package apap.ti._5.tour_package_2306165963_be.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    /**
     * Handles GET request for the home page (root path: /)
     * URL: http://localhost:8080/
     */
    @GetMapping({"/"})
    public String home(HttpServletRequest request, Model model) {
        // Mengembalikan nama view index.html
        model.addAttribute("currentUri", request.getRequestURI());
        return "index";
    }
}