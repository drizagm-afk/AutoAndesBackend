package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.security.UsuarioPrincipal;
public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
    UsuarioActualResponseDTO actual(UsuarioPrincipal usuario);
}
