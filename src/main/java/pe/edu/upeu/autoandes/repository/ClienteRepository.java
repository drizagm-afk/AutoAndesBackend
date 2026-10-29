package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.autoandes.entity.Cliente;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findAllByOrderByNombresAsc();
}
