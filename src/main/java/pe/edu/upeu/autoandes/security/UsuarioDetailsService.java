package pe.edu.upeu.autoandes.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.*;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    @Override
    @Transactional(readOnly = true)
    public UsuarioPrincipal loadUserByUsername(String username) {
        return usuarios
                .findByUsername(username)
                .map(UsuarioPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    }
}
