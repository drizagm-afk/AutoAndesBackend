package pe.edu.upeu.autoandes.security;

import jakarta.servlet.http.*;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final RespuestaErrorSeguridad errores;

    @Override
    public void handle(
            HttpServletRequest r,
            HttpServletResponse s,
            AccessDeniedException ex
    ) throws IOException {
        errores.escribir(
                r, s, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación"
        );
    }
}
