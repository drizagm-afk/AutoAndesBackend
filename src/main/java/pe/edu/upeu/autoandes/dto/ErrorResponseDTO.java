package pe.edu.upeu.autoandes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, String> campos
) {
}
