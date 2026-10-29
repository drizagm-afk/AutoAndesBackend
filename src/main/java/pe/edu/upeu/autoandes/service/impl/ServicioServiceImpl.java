package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.service.ServicioService;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.mapper.ServicioMapper;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.exception.*;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicioServiceImpl implements ServicioService {
    private final ServicioRepository repository;
    private final DetalleOrdenRepository detalles;
    private final ServicioMapper mapper;

    @Override
    public List<ServicioResponseDTO> listar() {
        return repository.findAllByOrderByCodigoAsc().stream().map(mapper::toResponse).toList();
    }

    @Override
    public ServicioResponseDTO obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public ServicioResponseDTO crear(ServicioRequestDTO request) {
        if (repository.existsByCodigo(request.codigo()))
            throw new ReglaNegocioException("El código de servicio ya existe");

        Servicio servicio = new Servicio();
        mapper.update(request, servicio);
        servicio.setActivo(true);
        servicio = repository.saveAndFlush(servicio);

        log.info("Servicio creado: id={}, código={}", servicio.getId(), servicio.getCodigo());

        return mapper.toResponse(servicio);
    }

    @Transactional
    public ServicioResponseDTO actualizar(Long id, ServicioRequestDTO request) {
        Servicio servicio = buscar(id);
        if (repository.existsByCodigoAndIdNot(request.codigo(), id))
            throw new ReglaNegocioException("El código de servicio ya existe");

        mapper.update(request, servicio);
        repository.flush();

        log.info("Servicio actualizado: id={}", id);

        return mapper.toResponse(servicio);
    }

    @Transactional
    public void eliminar(Long id) {
        Servicio servicio = buscar(id);
        if (detalles.existsByServicioId(id))
            throw new ReglaNegocioException(
                    "El servicio está usado en órdenes. Use PATCH /api/v1/servicios/" + id + "/estado para darlo de baja"
            );

        repository.delete(servicio);
        repository.flush();

        log.info("Servicio eliminado: id={}", id);
    }

    @Transactional
    public ServicioResponseDTO cambiarEstado(Long id, EstadoServicioRequestDTO request) {
        Servicio servicio = buscar(id);
        servicio.setActivo(request.activo());
        repository.flush();

        log.info("Estado de servicio cambiado: id={}, activo={}", id, servicio.getActivo());

        return mapper.toResponse(servicio);
    }

    private Servicio buscar(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Servicio no encontrado: " + id
                ));
    }
}
