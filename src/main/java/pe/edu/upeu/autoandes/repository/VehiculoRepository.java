package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.autoandes.entity.Vehiculo;
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths="cliente")
    java.util.List<Vehiculo> findAllByOrderByPlacaAsc();
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths="cliente")
    java.util.List<Vehiculo> findByClienteIdOrderByPlacaAsc(Long clienteId);
    boolean existsByPlaca(String placa);
    boolean existsByPlacaAndIdNot(String placa, Long id);
}
