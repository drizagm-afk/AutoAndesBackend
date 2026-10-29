package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.EstadoOrden;
import pe.edu.upeu.autoandes.service.OrdenService;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
public class OrdenController {
    private final OrdenService service;

    @PostMapping
    public ResponseEntity<OrdenResponseDTO> registrar(
            @Valid @RequestBody OrdenRequestDTO r, Principal principal
    ) {
        // Question 2 permits anonymous evaluation; Question 3 requires a verified JWT principal.
        var response = service.registrar(r, principal == null ? "asesor" : principal.getName());
        return ResponseEntity.created(URI.create("/api/v1/ordenes/" + response.id())).body(response);
    }

    @GetMapping
    public List<OrdenResponseDTO> consultar(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) EstadoOrden estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return service.consultar(placa, estado, desde, hasta);
    }

    @GetMapping("/{id}")
    public OrdenResponseDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PatchMapping("/{id}/anular")
    public OrdenResponseDTO anular(@PathVariable Long id, @Valid @RequestBody AnularOrdenRequestDTO r) {
        return service.anular(id, r);
    }
}
