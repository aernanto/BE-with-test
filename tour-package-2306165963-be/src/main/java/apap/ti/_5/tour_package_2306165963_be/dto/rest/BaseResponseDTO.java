package apap.ti._5.tour_package_2306165963_be.dto.rest;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponseDTO<T> {

    private int status;
    private T data;
    private String message;
    private Date timestamp;
}
