package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.EstadoOrden;
import java.time.LocalDate;
import java.util.List;
public interface OrdenService {
    OrdenResponseDTO registrar(OrdenRequestDTO request,String usuario);
    OrdenResponseDTO obtener(Long id);
    List<OrdenResponseDTO> consultar(String placa,EstadoOrden estado,LocalDate desde,LocalDate hasta);
    OrdenResponseDTO anular(Long id,AnularOrdenRequestDTO request);
}
