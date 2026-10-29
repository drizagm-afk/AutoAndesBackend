package pe.edu.upeu.autoandes.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import pe.edu.upeu.autoandes.config.JwtService;
import pe.edu.upeu.autoandes.security.*;
import pe.edu.upeu.autoandes.mapper.UsuarioMapper;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.service.AuthService;

@Slf4j @Service
public class AuthServiceImpl implements AuthService {
    private final UsuarioDetailsService usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final UsuarioMapper mapper;
    private final String dummyHash;
    public AuthServiceImpl(UsuarioDetailsService usuarios,PasswordEncoder encoder,JwtService jwt,UsuarioMapper mapper) {
        this.usuarios=usuarios;this.encoder=encoder;this.jwt=jwt;this.mapper=mapper;
        dummyHash=encoder.encode("dummy-authentication-password");
    }
    public LoginResponseDTO login(LoginRequestDTO r) {
        UsuarioPrincipal user=null;
        try {user=usuarios.loadUserByUsername(r.username());}catch(UsernameNotFoundException ignored) { /* Same response and BCrypt work as a failed password. */ }
        boolean matches;
        try {
            matches=encoder.matches(r.password(),user==null?dummyHash:user.password());
        } catch (IllegalArgumentException ex) {
            // BCrypt limits UTF-8 bytes, while Bean Validation counts characters.
            matches=false;
        }
        if(user==null || !matches || !user.activo()) {
            log.warn("Intento de login rechazado");throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }
        log.info("Login correcto: usuario={}",user.username());
        return new LoginResponseDTO(jwt.generarToken(user),"Bearer",3600,user.nombreCompleto(),user.roles(),user.permisos());
    }
    public UsuarioActualResponseDTO actual(UsuarioPrincipal u) {return mapper.toResponse(u);}
}
