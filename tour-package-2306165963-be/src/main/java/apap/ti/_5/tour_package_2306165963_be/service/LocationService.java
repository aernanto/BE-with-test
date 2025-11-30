package apap.ti._5.tour_package_2306165963_be.service;

import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

public interface LocationService {
    Mono<List<Map<String, Object>>> getAllProvinces();

    Mono<List<Map<String, Object>>> getRegenciesByProvince(String provinceCode);
}
