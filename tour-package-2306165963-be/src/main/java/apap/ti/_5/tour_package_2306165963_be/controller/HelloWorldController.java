package apap.ti._5.tour_package_2306165963_be.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A simple controller to demonstrate Spring Boot MVC basics
 */
@Controller
@RequestMapping("/intro")
public class HelloWorldController {

    /**
     * Handles GET requests to the root path (Panduan)
     * URL: http://localhost:8080/
     */
    @GetMapping
    public String helloWorld (Model model) {
        model.addAttribute("message", "Hello World from Spring Boot MVC!");
        model.addAttribute("title", "Spring Boot MVC Introduction");
        return "hello";
    }

    /**
     * Handles GET requests with a path parameter (Latihan)
     * URL Contoh: http://localhost:8080/Aimee
     */
    @GetMapping("/{name}")
    public String helloWithPathParam (@PathVariable("name") String name, Model model) {
        model.addAttribute("message", "Hello " + name + "! Welcome to Spring Boot MVC!");
        model.addAttribute("title", "Spring Boot MVC Path Param");
        return "hello"; 
    }
}