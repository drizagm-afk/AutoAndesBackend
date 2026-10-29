package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.*;

import java.util.List;

public interface ServicioService {
    List<ServicioResponseDTO> listar();

    ServicioResponseDTO obtener(Long id);

    ServicioResponseDTO crear(ServicioRequestDTO request);

    ServicioResponseDTO actualizar(Long id, ServicioRequestDTO request);

    void eliminar(Long id);

    ServicioResponseDTO cambiarEstado(Long id, EstadoServicioRequestDTO request);
}
