package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.service.ServicioService;
import pe.edu.upeu.autoandes.dto.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
public class ServicioController {
    private final ServicioService service;

    @GetMapping
    public List<ServicioResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ServicioResponseDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ServicioResponseDTO> crear(@Valid @RequestBody ServicioRequestDTO r) {
        var response = service.crear(r);
        return ResponseEntity.created(URI.create("/api/v1/servicios/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ServicioResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequestDTO r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ServicioResponseDTO estado(@PathVariable Long id, @Valid @RequestBody EstadoServicioRequestDTO r) {
        return service.cambiarEstado(id, r);
    }
}
