package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.NotNull;
public record EstadoServicioRequestDTO(@NotNull(message="El estado activo es obligatorio") Boolean activo) {}
