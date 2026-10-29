package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.autoandes.entity.OrdenServicio;
public interface OrdenServicioRepository extends JpaRepository<OrdenServicio, Long> {
    boolean existsByVehiculoId(Long vehiculoId);
}
