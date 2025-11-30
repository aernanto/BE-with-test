package apap.ti._5.tour_package_2306165963_be.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class LocationServiceImpl implements LocationService {

    private final WebClient webClient;

    public LocationServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://wilayah.id/api").build();
    }

    @Override
    public Mono<List<Map<String, Object>>> getAllProvinces() {
        return this.webClient.get()
                .uri("/provinces.json")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (List<Map<String, Object>>) response.get("data"));
    }

    @Override
    public Mono<List<Map<String, Object>>> getRegenciesByProvince(String provinceCode) {
        return this.webClient.get()
                .uri("/regencies/{provinceCode}.json", provinceCode)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (List<Map<String, Object>>) response.get("data"));
    }
}
