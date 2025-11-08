package apap.ti._5.tour_package_2306165963_be;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173") // Vite default
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}