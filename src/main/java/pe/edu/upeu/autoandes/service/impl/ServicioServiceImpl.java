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

@Slf4j @Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ServicioServiceImpl implements ServicioService {
    private final ServicioRepository repository;
    private final DetalleOrdenRepository detalles;
    private final ServicioMapper mapper;
    public List<ServicioResponseDTO> listar() { return repository.findAllByOrderByCodigoAsc().stream().map(mapper::toResponse).toList(); }
    public ServicioResponseDTO obtener(Long id) { return mapper.toResponse(buscar(id)); }
    @Transactional
    public ServicioResponseDTO crear(ServicioRequestDTO r) {
        if (repository.existsByCodigo(r.codigo())) throw new ReglaNegocioException("El código de servicio ya existe");
        Servicio e = new Servicio(); mapper.update(r,e); e.setActivo(true);
        e = repository.saveAndFlush(e); log.info("Servicio creado: id={}, código={}", e.getId(), e.getCodigo());
        return mapper.toResponse(e);
    }
    @Transactional
    public ServicioResponseDTO actualizar(Long id, ServicioRequestDTO r) {
        Servicio e = buscar(id);
        if (repository.existsByCodigoAndIdNot(r.codigo(),id)) throw new ReglaNegocioException("El código de servicio ya existe");
        mapper.update(r,e); repository.flush(); log.info("Servicio actualizado: id={}",id);
        return mapper.toResponse(e);
    }
    @Transactional
    public void eliminar(Long id) {
        Servicio e = buscar(id);
        if (detalles.existsByServicioId(id)) throw new ReglaNegocioException("El servicio está usado en órdenes. Use PATCH /api/v1/servicios/" + id + "/estado para darlo de baja");
        repository.delete(e); repository.flush(); log.info("Servicio eliminado: id={}",id);
    }
    @Transactional
    public ServicioResponseDTO cambiarEstado(Long id, EstadoServicioRequestDTO r) {
        Servicio e = buscar(id); e.setActivo(r.activo()); repository.flush();
        log.info("Estado de servicio cambiado: id={}, activo={}",id,e.getActivo()); return mapper.toResponse(e);
    }
    private Servicio buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado: " + id));
    }
}
