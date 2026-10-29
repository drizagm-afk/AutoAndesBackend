package pe.edu.upeu.autoandes.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pe.edu.upeu.autoandes.dto.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // >>> BASIC EXCEPTIONS
    // BAD REQUEST: 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponseDTO> validation(
            MethodArgumentNotValidException ex,
            HttpServletRequest r
    ) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                campos.putIfAbsent(e.getField(), e.getDefaultMessage())
        );

        return error(HttpStatus.BAD_REQUEST, "Datos invalidos", r, campos);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    ResponseEntity<ErrorResponseDTO> invalid(
            Exception ex,
            HttpServletRequest r
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Solicitud invalida: revise el JSON y los tipos de datos",
                r, null
        );
    }

    @ExceptionHandler(DatosInvalidosException.class)
    ResponseEntity<ErrorResponseDTO> invalidBusiness(
            DatosInvalidosException ex,
            HttpServletRequest r
    ) {
        log.warn("Regla rechazada en {}: {}", r.getRequestURI(), ex.getMessage());

        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), r, null);
    }

    // RESOURCE NOT FOUND: 404
    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<ErrorResponseDTO> missing(
            RecursoNoEncontradoException ex,
            HttpServletRequest r
    ) {
        log.warn("Recurso requerido no encontrado en {}: {}", r.getRequestURI(), ex.getMessage());

        return error(HttpStatus.NOT_FOUND, ex.getMessage(), r, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponseDTO> missingRoute(
            NoResourceFoundException ex,
            HttpServletRequest r
    ) {
        return error(HttpStatus.NOT_FOUND, "Ruta no encontrada", r, null);
    }

    // CONFLICT: 409
    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<ErrorResponseDTO> conflict(
            ReglaNegocioException ex,
            HttpServletRequest r
    ) {
        log.warn("Regla rechazada en {}: {}", r.getRequestURI(), ex.getMessage());

        return error(HttpStatus.CONFLICT, ex.getMessage(), r, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponseDTO> integrity(
            DataIntegrityViolationException ex,
            HttpServletRequest r
    ) {
        log.warn("Conflicto de integridad en {}", r.getRequestURI());

        return error(
                HttpStatus.CONFLICT,
                "La operacion entra en conflicto con un registro existente o relacionado",
                r, null
        );
    }

    // METHOD NOT ALLOWED: 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponseDTO> method(
            Exception ex,
            HttpServletRequest r
    ) {
        return error(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Metodo no permitido",
                r, null
        );
    }

    // >>> SECURITY EXCEPTIONS
    // UNAUTHORIZED: 401
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponseDTO> authentication(
            Exception ex,
            HttpServletRequest r
    ) {
        return error(
                HttpStatus.UNAUTHORIZED,
                "Usuario o contraseña incorrectos",
                r, null
        );
    }

    // FORBIDDEN: 403
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponseDTO> denied(
            Exception ex,
            HttpServletRequest r
    ) {
        return error(
                HttpStatus.FORBIDDEN,
                "No tiene permisos para realizar esta operación",
                r, null
        );
    }

    // >>> ANY EXCEPTION
    // SERVER INTERNAL ERROR: 500
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponseDTO> unexpected(
            Exception ex,
            HttpServletRequest r
    ) {
        log.error("Error inesperado en {}", r.getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "OcurriÃƒÂ³ un error inesperado", r, null);
    }

    private ResponseEntity<ErrorResponseDTO> error(HttpStatus status, String mensaje, HttpServletRequest r, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ErrorResponseDTO(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, r.getRequestURI(), campos));
    }
}
