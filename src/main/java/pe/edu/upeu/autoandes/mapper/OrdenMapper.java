package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.dto.*;

import java.util.List;

@Component
public class OrdenMapper {
    public OrdenResponseDTO toResponse(OrdenServicio o, List<DetalleOrden> detalles) {
        var v = o.getVehiculo();

        return new OrdenResponseDTO(
                o.getId(), o.getNumero(), o.getFecha(), v.getId(), v.getPlaca(),
                v.getCliente().getNombres(), o.getKilometraje(), o.getEstado(), o.getTotal(), o.getUsuarioRegistro(),
                o.getMotivoAnulacion(),
                detalles.stream().map(d -> new DetalleOrdenResponseDTO(
                        d.getId(), d.getServicio().getId(), d.getServicio().getCodigo(),
                        d.getServicio().getNombre(), d.getCantidad(), d.getPrecioUnitario(),
                        d.getSubtotal()
                )).toList()
        );
    }
}
