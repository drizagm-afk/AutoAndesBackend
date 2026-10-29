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

@Slf4j @Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class OrdenServiceImpl implements OrdenService {
    private final OrdenServicioRepository ordenes;
    private final DetalleOrdenRepository detalles;
    private final VehiculoRepository vehiculos;
    private final ServicioRepository servicios;
    private final OrdenMapper mapper;
    private final Clock clock;

    @Transactional
    public OrdenResponseDTO registrar(OrdenRequestDTO r,String usuario) {
        Set<Long> ids=new HashSet<>();
        for (var d:r.detalles()) if (!ids.add(d.servicioId())) throw new DatosInvalidosException("No se puede repetir un servicio en la orden");
        // Serialize registrations for the same vehicle so concurrent requests cannot bypass RN-04.
        Vehiculo vehiculo=vehiculos.buscarParaOrden(r.vehiculoId()).orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado: "+r.vehiculoId()));
        ordenes.findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(vehiculo.getId(),"REGISTRADA").ifPresent(ultima -> {
            if (r.kilometraje()<ultima.getKilometraje()) throw new ReglaNegocioException("El kilometraje no puede ser menor que el último registrado: "+ultima.getKilometraje());
        });
        OrdenServicio o=new OrdenServicio();
        o.setNumero("OS-%06d".formatted(ordenes.siguienteNumero()));
        o.setFecha(LocalDateTime.now(clock)); o.setVehiculo(vehiculo); o.setKilometraje(r.kilometraje());
        o.setEstado("REGISTRADA"); o.setTotal(BigDecimal.ZERO);
        o.setUsuarioRegistro(usuario);
        ordenes.saveAndFlush(o);
        List<DetalleOrden> lineas=new ArrayList<>();
        BigDecimal total=BigDecimal.ZERO;
        for (var item:r.detalles()) {
            Servicio s=servicios.findById(item.servicioId()).orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado: "+item.servicioId()));
            if (!s.getActivo()) throw new ReglaNegocioException("El servicio "+s.getCodigo()+" está inactivo");
            DetalleOrden d=new DetalleOrden(); d.setOrden(o); d.setServicio(s); d.setCantidad(item.cantidad());
            d.setPrecioUnitario(s.getPrecio()); d.setSubtotal(s.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
            // Any later failure rolls these inserts and the header back in the same transaction.
            detalles.save(d); lineas.add(d); total=total.add(d.getSubtotal());
        }
        if (total.compareTo(new BigDecimal("99999999.99"))>0) throw new ReglaNegocioException("El total supera el importe máximo permitido");
        o.setTotal(total); ordenes.flush();
        log.info("Orden registrada: id={}, número={}, total={}",o.getId(),o.getNumero(),total);
        return mapper.toResponse(o,lineas);
    }
    public OrdenResponseDTO obtener(Long id) { return respuesta(buscar(id)); }
    public List<OrdenResponseDTO> consultar(String placa,EstadoOrden estado,LocalDate desde,LocalDate hasta) {
        var rango=RangoFechas.of(desde,hasta);
        String filtro=placa==null || placa.isBlank()?null:"%"+placa.strip().toUpperCase(Locale.ROOT)
            .replace("!","!!").replace("%","!%").replace("_","!_")+"%";
        return ordenes.consultar(filtro,estado==null?null:estado.name(),rango.desde(),rango.hastaExclusivo()).stream().map(this::respuesta).toList();
    }
    @Transactional
    public OrdenResponseDTO anular(Long id,AnularOrdenRequestDTO r) {
        OrdenServicio o=ordenes.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada: "+id));
        if (!o.getEstado().equals("REGISTRADA")) throw new ReglaNegocioException("Solo se puede anular una orden REGISTRADA");
        if (!o.getFecha().toLocalDate().equals(LocalDate.now(clock))) throw new ReglaNegocioException("Solo se puede anular una orden el mismo día de su registro");
        String motivo=r.motivo().strip();
        if (motivo.length()<10) throw new DatosInvalidosException("El motivo debe tener entre 10 y 200 caracteres sin espacios de relleno");
        o.setEstado("ANULADA"); o.setMotivoAnulacion(motivo); ordenes.flush();
        log.info("Orden anulada: id={}, número={}",id,o.getNumero());
        return respuesta(o);
    }
    private OrdenServicio buscar(Long id) { return ordenes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada: "+id)); }
    private OrdenResponseDTO respuesta(OrdenServicio o) { return mapper.toResponse(o,detalles.findByOrdenIdOrderByIdAsc(o.getId())); }
}
