package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.service.VehiculoService;
import pe.edu.upeu.autoandes.dto.*;
import java.net.URI;
import java.util.List;

@RestController @RequestMapping("/api/v1/vehiculos") @RequiredArgsConstructor
public class VehiculoController {
    private final VehiculoService service;
    @GetMapping
    public List<VehiculoResponseDTO> listar(@RequestParam(required=false) Long clienteId) { return service.listar(clienteId); }
    @GetMapping("/{id}")
    public VehiculoResponseDTO obtener(@PathVariable Long id) { return service.obtener(id); }
    @PostMapping
    public ResponseEntity<VehiculoResponseDTO> crear(@Valid @RequestBody VehiculoRequestDTO r) {
        var response = service.crear(r); return ResponseEntity.created(URI.create("/api/v1/vehiculos/" + response.id())).body(response);
    }
    @PutMapping("/{id}")
    public VehiculoResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody VehiculoRequestDTO r) { return service.actualizar(id,r); }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
    
}
