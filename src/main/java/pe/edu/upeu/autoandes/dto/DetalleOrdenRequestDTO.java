package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.*;

public record DetalleOrdenRequestDTO(
        @NotNull(message = "El servicio es obligatorio")
        @Positive(message = "El servicio debe ser positivo")
        Long servicioId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 5, message = "La cantidad máxima es 5")
        Integer cantidad
) {
}
