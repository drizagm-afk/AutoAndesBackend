package pe.edu.upeu.autoandes.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.exception.*;
import pe.edu.upeu.autoandes.mapper.OrdenMapper;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.service.OrdenService;
import pe.edu.upeu.autoandes.service.support.RangoFechas;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdenServiceImpl implements OrdenService {
    private final OrdenServicioRepository ordenes;
    private final DetalleOrdenRepository detalles;
    private final VehiculoRepository vehiculos;
    private final ServicioRepository servicios;
    private final OrdenMapper mapper;

    @Transactional
    public OrdenResponseDTO registrar(OrdenRequestDTO request, String usuario) {
        Set<Long> ids = new HashSet<>();
        for (var detalle : request.detalles())
            if (!ids.add(detalle.servicioId()))
                throw new DatosInvalidosException("No se puede repetir un servicio en la orden");

        // Serialize registrations for the same vehicle so concurrent requests cannot bypass RN-04.
        Vehiculo vehiculo = vehiculos
                .buscarParaOrden(request.vehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Vehículo no encontrado: " + request.vehiculoId())
                );

        ordenes.findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(
                vehiculo.getId(), "REGISTRADA"
        ).ifPresent(ultima -> {
            if (request.kilometraje() < ultima.getKilometraje())
                throw new ReglaNegocioException(
                        "El kilometraje no puede ser menor que el último registrado: " + ultima.getKilometraje()
                );
        });

        OrdenServicio orden = new OrdenServicio();
        orden.setNumero("OS-%06d".formatted(ordenes.siguienteNumero()));
        orden.setFecha(LocalDateTime.now());
        orden.setVehiculo(vehiculo);
        orden.setKilometraje(request.kilometraje());
        orden.setEstado("REGISTRADA");
        orden.setTotal(BigDecimal.ZERO);
        orden.setUsuarioRegistro(usuario);
        ordenes.saveAndFlush(orden);

        List<DetalleOrden> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var item : request.detalles()) {
            Servicio servicio = servicios
                    .findById(item.servicioId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Servicio no encontrado: " + item.servicioId())
                    );

            if (!servicio.getActivo())
                throw new ReglaNegocioException("El servicio " + servicio.getCodigo() + " está inactivo");

            DetalleOrden detalle = new DetalleOrden();
            detalle.setOrden(orden);
            detalle.setServicio(servicio);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(servicio.getPrecio());
            detalle.setSubtotal(servicio.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
            // Any later failure rolls these inserts and the header back in the same transaction.
            detalles.save(detalle);
            lineas.add(detalle);

            total = total.add(detalle.getSubtotal());
        }
        if (total.compareTo(new BigDecimal("99999999.99")) > 0)
            throw new ReglaNegocioException("El total supera el importe máximo permitido");

        orden.setTotal(total);
        ordenes.flush();
        log.info("Orden registrada: id={}, número={}, total={}", orden.getId(), orden.getNumero(), total);

        return mapper.toResponse(orden, lineas);
    }

    @Override
    public OrdenResponseDTO obtener(Long id) {
        return respuesta(buscar(id));
    }

    public List<OrdenResponseDTO> consultar(
            String placa, EstadoOrden estado, LocalDate desde, LocalDate hasta
    ) {
        var rango = RangoFechas.of(desde, hasta);
        String filtro = placa == null || placa.isBlank() ? null : "%" + placa
                .strip().toUpperCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_") + "%";

        return ordenes.consultar(
                filtro,
                estado == null ? null : estado.name(), rango.desde(), rango.hastaExclusivo()
        ).stream().map(this::respuesta).toList();
    }

    @Transactional
    public OrdenResponseDTO anular(Long id, AnularOrdenRequestDTO request) {
        OrdenServicio orden = ordenes.buscarParaActualizar(id).orElseThrow(
                () -> new RecursoNoEncontradoException("Orden no encontrada: " + id)
        );

        if (!orden.getEstado().equals("REGISTRADA"))
            throw new ReglaNegocioException("Solo se puede anular una orden REGISTRADA");

        if (!orden.getFecha().toLocalDate().equals(LocalDate.now()))
            throw new ReglaNegocioException("Solo se puede anular una orden el mismo día de su registro");

        String motivo = request.motivo().strip();
        if (motivo.length() < 10)
            throw new DatosInvalidosException("El motivo debe tener entre 10 y 200 caracteres sin espacios de relleno");

        orden.setEstado("ANULADA");
        orden.setMotivoAnulacion(motivo);
        ordenes.flush();
        log.info("Orden anulada: id={}, número={}", id, orden.getNumero());

        return respuesta(orden);
    }

    private OrdenServicio buscar(Long id) {
        return ordenes.findById(id).orElseThrow(
                () -> new RecursoNoEncontradoException("Orden no encontrada: " + id)
        );
    }

    private OrdenResponseDTO respuesta(OrdenServicio orden) {
        return mapper.toResponse(orden, detalles.findByOrdenIdOrderByIdAsc(orden.getId()));
    }
}
