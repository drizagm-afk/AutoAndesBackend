package pe.edu.upeu.autoandes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.service.ClienteService;
import pe.edu.upeu.autoandes.dto.ClienteResponseDTO;
import java.util.List;
@RestController @RequestMapping("/api/v1/clientes") @RequiredArgsConstructor
public class ClienteController {
    private final ClienteService service;
    @GetMapping public List<ClienteResponseDTO> listar() { return service.listar(); }
}
