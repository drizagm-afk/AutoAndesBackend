package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.dto.*;

@Component
public class ServicioMapper {
    public ServicioResponseDTO toResponse(Servicio e) {
        return new ServicioResponseDTO(
                e.getId(), e.getCodigo(), e.getNombre(), e.getPrecio(),
                e.getDuracionMin(), e.getActivo()
        );
    }

    public void update(ServicioRequestDTO r, Servicio e) {
        e.setCodigo(r.codigo());
        e.setNombre(r.nombre().strip());
        e.setPrecio(r.precio());
        e.setDuracionMin(r.duracionMin());
    }
}
