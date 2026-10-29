package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.entity.Cliente;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.exception.ReglaNegocioException;
import pe.edu.upeu.autoandes.mapper.VehiculoMapper;
import pe.edu.upeu.autoandes.repository.ClienteRepository;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;
import pe.edu.upeu.autoandes.repository.VehiculoRepository;
import pe.edu.upeu.autoandes.service.VehiculoService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehiculoServiceImpl implements VehiculoService {
    private final VehiculoRepository repository;
    private final ClienteRepository clientes;
    private final OrdenServicioRepository ordenes;
    private final VehiculoMapper mapper;

    @Override
    public List<VehiculoResponseDTO> listar(Long clienteId) {
        List<Vehiculo> vehiculos = clienteId == null
                ? repository.findAllByOrderByPlacaAsc()
                : repository.findByClienteIdOrderByPlacaAsc(clienteId);

        return vehiculos.stream().map(mapper::toResponse).toList();
    }

    @Override
    public VehiculoResponseDTO obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Override
    @Transactional
    public VehiculoResponseDTO crear(VehiculoRequestDTO request) {
        if (repository.existsByPlaca(request.placa())) {
            throw new ReglaNegocioException("La placa ya está registrada");
        }

        Vehiculo vehiculo = new Vehiculo();
        mapper.update(request, vehiculo);
        vehiculo.setCliente(cliente(request.clienteId()));
        repository.saveAndFlush(vehiculo);

        log.info("Vehículo creado: id={}, placa={}", vehiculo.getId(), vehiculo.getPlaca());
        return mapper.toResponse(vehiculo);
    }

    @Override
    @Transactional
    public VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO request) {
        Vehiculo vehiculo = buscar(id);
        if (repository.existsByPlacaAndIdNot(request.placa(), id)) {
            throw new ReglaNegocioException("La placa ya está registrada");
        }

        mapper.update(request, vehiculo);
        vehiculo.setCliente(cliente(request.clienteId()));
        repository.flush();

        log.info("Vehículo actualizado: id={}", id);
        return mapper.toResponse(vehiculo);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Vehiculo vehiculo = buscar(id);
        if (ordenes.existsByVehiculoId(id)) {
            throw new ReglaNegocioException("No se puede eliminar un vehículo con órdenes registradas");
        }

        repository.delete(vehiculo);
        repository.flush();
        log.info("Vehículo eliminado: id={}", id);
    }

    private Vehiculo buscar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("Vehículo no encontrado: " + id)
        );
    }

    private Cliente cliente(Long id) {
        return clientes.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("Cliente no encontrado: " + id)
        );
    }
}
