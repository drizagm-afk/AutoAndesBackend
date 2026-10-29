package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.*;

public record AnularOrdenRequestDTO(
        @NotBlank(message = "El motivo es obligatorio")
        @Size(min = 10, max = 200, message = "El motivo debe tener entre 10 y 200 caracteres")
        String motivo
) {
}