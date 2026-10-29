package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.LoginRequestDTO;
import pe.edu.upeu.autoandes.dto.LoginResponseDTO;
import pe.edu.upeu.autoandes.dto.UsuarioActualResponseDTO;
import pe.edu.upeu.autoandes.security.UsuarioPrincipal;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);

    UsuarioActualResponseDTO actual(UsuarioPrincipal usuario);
}
