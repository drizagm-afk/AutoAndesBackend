package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ServicioRequestDTO(
        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "SRV-[0-9]{3}", message = "El código debe tener el formato SRV-000")
        String codigo,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
        String nombre,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", inclusive = false, message = "El precio debe ser mayor que cero")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales")
        BigDecimal precio,
        @NotNull(message = "La duración es obligatoria")
        @Min(value = 15, message = "La duración mínima es 15 minutos")
        @Max(value = 480, message = "La duración máxima es 480 minutos")
        Integer duracionMin
) {
}
