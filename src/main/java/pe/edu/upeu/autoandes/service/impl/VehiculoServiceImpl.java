package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.service.VehiculoService;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.mapper.VehiculoMapper;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.exception.*;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class VehiculoServiceImpl implements VehiculoService {
    private final VehiculoRepository repository;
    private final ClienteRepository clientes;
    private final OrdenServicioRepository ordenes;
    private final VehiculoMapper mapper;
    public List<VehiculoResponseDTO> listar(Long clienteId) {
        return (clienteId == null ? repository.findAllByOrderByPlacaAsc() : repository.findByClienteIdOrderByPlacaAsc(clienteId)).stream().map(mapper::toResponse).toList();
    }
    public VehiculoResponseDTO obtener(Long id) { return mapper.toResponse(buscar(id)); }
    @Transactional
    public VehiculoResponseDTO crear(VehiculoRequestDTO r) {
        if (repository.existsByPlaca(r.placa())) throw new ReglaNegocioException("La placa ya está registrada");
        Vehiculo e = new Vehiculo(); mapper.update(r,e); e.setCliente(cliente(r.clienteId()));
        e = repository.saveAndFlush(e); log.info("Vehículo creado: id={}, placa={}",e.getId(),e.getPlaca()); return mapper.toResponse(e);
    }
    @Transactional
    public VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO r) {
        Vehiculo e = buscar(id);
        if (repository.existsByPlacaAndIdNot(r.placa(),id)) throw new ReglaNegocioException("La placa ya está registrada");
        mapper.update(r,e); e.setCliente(cliente(r.clienteId())); repository.flush();
        log.info("Vehículo actualizado: id={}",id); return mapper.toResponse(e);
    }
    @Transactional
    public void eliminar(Long id) {
        Vehiculo e = buscar(id);
        if (ordenes.existsByVehiculoId(id)) throw new ReglaNegocioException("No se puede eliminar un vehículo con órdenes registradas");
        repository.delete(e); repository.flush(); log.info("Vehículo eliminado: id={}",id);
    }
    private Vehiculo buscar(Long id) { return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado: " + id)); }
    private Cliente cliente(Long id) { return clientes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id)); }
}
