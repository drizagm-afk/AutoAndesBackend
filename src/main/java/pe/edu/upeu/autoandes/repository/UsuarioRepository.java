package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.*;
import pe.edu.upeu.autoandes.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findByUsername(String username);
}
