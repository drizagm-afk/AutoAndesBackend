package pe.edu.upeu.autoandes.dto;

import java.util.List;

public record UsuarioActualResponseDTO(
        Long id,
        String username,
        String nombreCompleto,
        List<String> roles,
        List<String> permisos
) {
}
