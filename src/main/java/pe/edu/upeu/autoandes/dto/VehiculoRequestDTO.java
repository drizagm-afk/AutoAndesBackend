package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.*;

public record VehiculoRequestDTO(
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "[A-Z]{3}-[0-9]{3}", message = "La placa debe tener el formato ABC-123")
        String placa,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 40, message = "La marca admite hasta 40 caracteres")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 40, message = "El modelo admite hasta 40 caracteres")
        String modelo,

        @NotNull(message = "El año es obligatorio")
        @Min(value = 1990, message = "El año mínimo es 1990")
        @Max(value = 2027, message = "El año máximo es 2027")
        Integer anio,

        @NotNull(message = "El cliente es obligatorio")
        @Positive(message = "El identificador del cliente debe ser positivo")
        Long clienteId
) {
}
