package pe.edu.upeu.autoandes.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.upeu.autoandes.config.JwtService;
import pe.edu.upeu.autoandes.security.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.service.AuthService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UsuarioDetailsService usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        UsuarioPrincipal usuario;
        try {
            usuario = usuarios.loadUserByUsername(request.username());
            if (!usuario.activo() || !encoder.matches(request.password(), usuario.password())) {
                throw new BadCredentialsException("Credenciales inválidas");
            }
        } catch (AuthenticationException | IllegalArgumentException ex) {
            log.warn("Intento de login rechazado");
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        log.info("Login correcto: usuario={}", usuario.username());

        return new LoginResponseDTO(
                jwt.generarToken(usuario), "Bearer", 3600,
                usuario.nombreCompleto(), usuario.roles(), usuario.permisos()
        );
    }

    @Override
    public UsuarioActualResponseDTO actual(UsuarioPrincipal usuario) {
        return new UsuarioActualResponseDTO(
                usuario.id(), usuario.username(), usuario.nombreCompleto(),
                usuario.roles(), usuario.permisos()
        );
    }
}
