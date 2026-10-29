package pe.edu.upeu.autoandes.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.upeu.autoandes.entity.Usuario;
import java.util.*;

public record UsuarioPrincipal(Long id,String username,String password,String nombreCompleto,
    boolean activo,List<String> roles,List<String> permisos) implements UserDetails {
    public static UsuarioPrincipal from(Usuario u) {
        return new UsuarioPrincipal(u.getId(),u.getUsername(),u.getPassword(),u.getNombreCompleto(),
            Boolean.TRUE.equals(u.getActivo()),u.getRoles().stream().map(r -> r.getNombre()).sorted().toList(),
            PermisosPorRol.permisos(u.getRoles()));
    }
    @Override public String getUsername() {return username;}
    @Override public String getPassword() {return password;}
    @Override public boolean isEnabled() {return activo;}
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return permisos.stream().map(SimpleGrantedAuthority::new).toList();
    }
    // Avoid accidental password disclosure if this object is ever logged.
    @Override public String toString() {return "UsuarioPrincipal[username="+username+"]";}
}
