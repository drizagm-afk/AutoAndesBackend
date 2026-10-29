package pe.edu.upeu.autoandes.security;

import jakarta.servlet.http.*;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final RespuestaErrorSeguridad errores;

    @Override
    public void commence(
            HttpServletRequest r,
            HttpServletResponse s,
            AuthenticationException ex
    ) throws IOException {
        errores.escribir(
                r, s, HttpStatus.UNAUTHORIZED, "Se requiere un token válido y vigente"
        );
    }
}
