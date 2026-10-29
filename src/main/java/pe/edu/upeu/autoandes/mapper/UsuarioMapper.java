package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.security.UsuarioPrincipal;
import pe.edu.upeu.autoandes.dto.UsuarioActualResponseDTO;
@Component
public class UsuarioMapper {
    public UsuarioActualResponseDTO toResponse(UsuarioPrincipal u) {
        return new UsuarioActualResponseDTO(u.id(),u.username(),u.nombreCompleto(),u.roles(),u.permisos());
    }
}
