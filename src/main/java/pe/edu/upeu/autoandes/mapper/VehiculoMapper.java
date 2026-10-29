package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.dto.*;

@Component
public class VehiculoMapper {
    public VehiculoResponseDTO toResponse(Vehiculo e) {
        return new VehiculoResponseDTO(
                e.getId(), e.getPlaca(), e.getMarca(), e.getModelo(), e.getAnio(),
                e.getCliente().getId(), e.getCliente().getNombres()
        );
    }

    public void update(VehiculoRequestDTO r, Vehiculo e) {
        e.setPlaca(r.placa());
        e.setMarca(r.marca().strip());
        e.setModelo(r.modelo().strip());
        e.setAnio(r.anio());
    }
}
