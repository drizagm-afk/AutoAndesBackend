package pe.edu.upeu.autoandes.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.autoandes.entity.Vehiculo;

import java.util.List;
import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    @EntityGraph(attributePaths = "cliente")
    List<Vehiculo> findAllByOrderByPlacaAsc();

    @EntityGraph(attributePaths = "cliente")
    List<Vehiculo> findByClienteIdOrderByPlacaAsc(Long clienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehiculo v where v.id=:id")
    Optional<Vehiculo> buscarParaOrden(@Param("id") Long id);

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);
}
