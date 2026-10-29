package pe.edu.upeu.autoandes.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upeu.autoandes.security.*;

@Slf4j @RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UsuarioDetailsService usuarios;
    private final RestAuthenticationEntryPoint entryPoint;
    @Override protected boolean shouldNotFilter(HttpServletRequest r) {
        String path=r.getServletPath();
        return "OPTIONS".equals(r.getMethod()) || ("POST".equals(r.getMethod()) && path.equals("/api/v1/auth/login"))
            || path.equals("/swagger-ui.html") || path.startsWith("/swagger-ui/") || path.equals("/v3/api-docs") || path.startsWith("/v3/api-docs/");
    }
    @Override protected void doFilterInternal(HttpServletRequest r,HttpServletResponse s,FilterChain chain) throws ServletException,IOException {
        String header=r.getHeader("Authorization");
        if(header==null) {chain.doFilter(r,s);return;}
        try {
            if(!header.regionMatches(true,0,"Bearer ",0,7))throw new BadCredentialsException("Esquema inválido");
            String username=jwt.extraerUsername(header.substring(7).strip());
            var user=usuarios.loadUserByUsername(username);
            if(!user.isEnabled())throw new BadCredentialsException("Cuenta inactiva");
            var auth=UsernamePasswordAuthenticationToken.authenticated(user,null,user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(r));
            var context=SecurityContextHolder.createEmptyContext();context.setAuthentication(auth);SecurityContextHolder.setContext(context);
        } catch(JwtException | IllegalArgumentException | UsernameNotFoundException | BadCredentialsException ex) {
            SecurityContextHolder.clearContext();log.warn("Autenticación JWT rechazada en {}",r.getRequestURI());
            entryPoint.commence(r,s,new BadCredentialsException("Token inválido",ex));return;
        }
        chain.doFilter(r,s);
    }
}
