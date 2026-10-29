package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.ClienteResponseDTO;

import java.util.List;

public interface ClienteService {
    List<ClienteResponseDTO> listar();
}
