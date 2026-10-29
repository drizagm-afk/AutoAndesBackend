package pe.edu.upeu.autoandes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record OrdenRequestDTO(
        @NotNull(message = "El vehículo es obligatorio")
        @Positive(message = "El vehículo debe ser positivo")
        Long vehiculoId,

        @NotNull(message = "El kilometraje es obligatorio")
        @PositiveOrZero(message = "El kilometraje no puede ser negativo")
        @Max(value = 9999999, message = "El kilometraje admite hasta 7 dígitos")
        Integer kilometraje,

        @NotNull(message = "Los detalles son obligatorios")
        @Size(min = 1, max = 10, message = "La orden debe tener entre 1 y 10 detalles")
        List<@NotNull(message = "El detalle no puede ser nulo") @Valid DetalleOrdenRequestDTO> detalles
) {
}