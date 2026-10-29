package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.*;
public record LoginRequestDTO(
    @NotBlank(message="El usuario es obligatorio") @Size(max=30,message="El usuario admite hasta 30 caracteres") String username,
    @NotBlank(message="La contraseña es obligatoria") @Size(max=72,message="La contraseña admite hasta 72 caracteres") String password
) {}
