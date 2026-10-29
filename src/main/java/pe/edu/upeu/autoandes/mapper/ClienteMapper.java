package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.entity.Cliente;
import pe.edu.upeu.autoandes.dto.ClienteResponseDTO;

@Component
public class ClienteMapper {
    public ClienteResponseDTO toResponse(Cliente e) {
        return new ClienteResponseDTO(
                e.getId(), e.getDocumento(), e.getNombres(), e.getTelefono()
        );
    }
}
