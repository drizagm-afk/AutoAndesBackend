package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.autoandes.entity.Servicio;
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    java.util.List<Servicio> findAllByOrderByCodigoAsc();
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
}
