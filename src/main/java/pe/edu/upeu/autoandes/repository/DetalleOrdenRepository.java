package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.autoandes.entity.DetalleOrden;
import pe.edu.upeu.autoandes.dto.IngresoPorServicioDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden,Long> {
    boolean existsByServicioId(Long servicioId);
    long countByOrdenId(Long ordenId);
    @EntityGraph(attributePaths="servicio")
    List<DetalleOrden> findByOrdenIdOrderByIdAsc(Long ordenId);
    @Query("""
        select new pe.edu.upeu.autoandes.dto.IngresoPorServicioDTO(
            d.servicio.codigo,d.servicio.nombre,sum(d.cantidad),sum(d.subtotal))
        from DetalleOrden d
        where d.orden.estado='REGISTRADA'
          and (:desde is null or d.orden.fecha>=:desde)
          and (:hasta is null or d.orden.fecha<:hasta)
        group by d.servicio.codigo,d.servicio.nombre
        order by sum(d.subtotal) desc,d.servicio.codigo asc
        """)
    List<IngresoPorServicioDTO> ingresos(@Param("desde") LocalDateTime desde,@Param("hasta") LocalDateTime hasta);
}
