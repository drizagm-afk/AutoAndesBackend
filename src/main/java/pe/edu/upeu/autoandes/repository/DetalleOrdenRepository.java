package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.autoandes.entity.DetalleOrden;
public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {
    boolean existsByServicioId(Long servicioId);
}
