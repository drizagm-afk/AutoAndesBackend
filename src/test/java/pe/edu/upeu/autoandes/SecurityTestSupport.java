package pe.edu.upeu.autoandes;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.autoandes.config.JwtService;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.security.UsuarioDetailsService;
import java.util.*;

abstract class SecurityTestSupport {
    @Autowired protected UsuarioRepository usuarios;
    @Autowired protected RolRepository roles;
    @Autowired protected PasswordEncoder encoder;
    @Autowired protected JwtService jwt;
    @Autowired protected UsuarioDetailsService detailsService;

    @BeforeEach void securityFixtures() {
        for(String name:List.of("ADMIN","ASESOR","JEFE_TALLER")) {
            if(roles.findByNombre(name).isEmpty()) {var role=new Rol();role.setNombre(name);roles.saveAndFlush(role);}
        }
        for(var entry:Map.of("admin","ADMIN","asesor","ASESOR","jefe","JEFE_TALLER","inactivo","ASESOR").entrySet()) {
            if(usuarios.findByUsername(entry.getKey()).isEmpty()) {
                var u=new Usuario();u.setUsername(entry.getKey());u.setPassword(encoder.encode("Auto2026*"));
                u.setNombreCompleto("Prueba "+entry.getKey());u.setActivo(!entry.getKey().equals("inactivo"));
                u.setRoles(new HashSet<>(Set.of(roles.findByNombre(entry.getValue()).orElseThrow())));usuarios.saveAndFlush(u);
            }
        }
    }
    protected String token(String username) {return jwt.generarToken(detailsService.loadUserByUsername(username));}
}
