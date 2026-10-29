package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import pe.edu.upeu.autoandes.security.UsuarioPrincipal;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.service.AuthService;
@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService service;
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @PostMapping("/login") public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO r) {return service.login(r);}
    @GetMapping("/me") public UsuarioActualResponseDTO actual(@AuthenticationPrincipal UsuarioPrincipal u) {return service.actual(u);}
}
