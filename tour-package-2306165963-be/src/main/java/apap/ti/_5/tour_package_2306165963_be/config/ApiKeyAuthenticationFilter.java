package apap.ti._5.tour_package_2306165963_be.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final List<String> PROTECTED_PATHS = Arrays.asList(
            "/api/add-points",
            "/api/use-coupon");

    @Value("${loyalty.api.key:default-loyalty-api-key}")
    private String validApiKey;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();

        // Check if this path requires API key authentication
        boolean requiresAuth = PROTECTED_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));

        if (requiresAuth && "POST".equals(requestMethod)) {
            String apiKey = request.getHeader(API_KEY_HEADER);

            if (apiKey == null || apiKey.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":401,\"message\":\"API key is required\",\"timestamp\":\""
                        + new java.util.Date() + "\"}");
                return;
            }

            if (!validApiKey.equals(apiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":401,\"message\":\"Invalid API key\",\"timestamp\":\""
                        + new java.util.Date() + "\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
