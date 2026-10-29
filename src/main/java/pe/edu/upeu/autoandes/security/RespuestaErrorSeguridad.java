package pe.edu.upeu.autoandes.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.autoandes.dto.ErrorResponseDTO;
import tools.jackson.databind.ObjectMapper;

@Component @RequiredArgsConstructor
public class RespuestaErrorSeguridad {
    private final ObjectMapper mapper;
    private final Clock clock;
    public void escribir(HttpServletRequest r,HttpServletResponse response,HttpStatus status,String mensaje) throws IOException {
        response.setStatus(status.value());response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        if(status==HttpStatus.UNAUTHORIZED)response.setHeader(HttpHeaders.WWW_AUTHENTICATE,"Bearer");
        mapper.writeValue(response.getOutputStream(),new ErrorResponseDTO(LocalDateTime.now(clock),status.value(),
            status.getReasonPhrase(),mensaje,r.getRequestURI(),null));
    }
}
