package pe.edu.upeu.autoandes;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.exception.*;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.service.*;
import java.net.URI;
import java.net.http.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrdenIntegrationTest extends SecurityTestSupport {
    @Autowired OrdenService service;
    @Autowired ReporteService reporte;
    @Autowired ClienteRepository clientes;
    @Autowired VehiculoRepository vehiculos;
    @Autowired ServicioRepository servicios;
    @Autowired OrdenServicioRepository ordenes;
    @Autowired DetalleOrdenRepository detalles;
    @Autowired JdbcTemplate jdbc;
    @Value("${local.server.port}") int port;
    static final AtomicInteger ids=new AtomicInteger(920);

    @BeforeEach void sequence() { jdbc.execute("CREATE SEQUENCE IF NOT EXISTS orden_numero_seq START WITH 100"); }
    private Vehiculo vehiculo() {
        int n=ids.getAndIncrement();
        Cliente c=new Cliente(); c.setDocumento("DOC"+n); c.setNombres("Cliente órdenes "+n); c=clientes.saveAndFlush(c);
        Vehiculo v=new Vehiculo(); v.setPlaca("ITP-"+n); v.setMarca("Toyota"); v.setModelo("Yaris"); v.setAnio(2020); v.setCliente(c);
        return vehiculos.saveAndFlush(v);
    }
    private Servicio servicio(boolean activo, String precio) {
        int n=ids.getAndIncrement();
        Servicio s=new Servicio(); s.setCodigo("SRV-"+n); s.setNombre("Servicio "+n); s.setPrecio(new BigDecimal(precio)); s.setDuracionMin(30); s.setActivo(activo);
        return servicios.saveAndFlush(s);
    }
    private OrdenRequestDTO request(Vehiculo v,int km,Servicio... services) {
        return new OrdenRequestDTO(v.getId(),km,Arrays.stream(services).map(s -> new DetalleOrdenRequestDTO(s.getId(),2)).toList());
    }
    private OrdenServicio previa(Vehiculo v,int km,String estado,LocalDateTime fecha,Servicio s,int cantidad) {
        OrdenServicio o=new OrdenServicio(); o.setNumero("OS-%06d".formatted(ids.getAndIncrement())); o.setFecha(fecha); o.setVehiculo(v); o.setKilometraje(km); o.setEstado(estado);
        o.setTotal(s.getPrecio().multiply(BigDecimal.valueOf(cantidad))); o.setUsuarioRegistro("asesor"); o=ordenes.saveAndFlush(o);
        DetalleOrden d=new DetalleOrden(); d.setOrden(o); d.setServicio(s); d.setCantidad(cantidad); d.setPrecioUnitario(s.getPrecio()); d.setSubtotal(o.getTotal()); detalles.saveAndFlush(d);
        return o;
    }
    @Test void thirdInactiveDetailRollsBackActualHeaderAndPriorDetails() {
        var v=vehiculo(); var a=servicio(true,"12.35"); var b=servicio(true,"20.10"); var inactive=servicio(false,"99.00");
        long headers=ordenes.count(), lines=detalles.count();
        assertThrows(ReglaNegocioException.class,() -> service.registrar(request(v,100,a,b,inactive),"asesor"));
        assertEquals(headers,ordenes.count()); assertEquals(lines,detalles.count());
        var result=service.registrar(request(v,100,a,b),"asesor");
        assertEquals(0,new BigDecimal("64.90").compareTo(result.total()));
        assertEquals(2,result.detalles().size()); assertEquals("REGISTRADA",result.estado());
        assertEquals(0,a.getPrecio().compareTo(result.detalles().getFirst().precioUnitario()));
        assertEquals(headers+1,ordenes.count()); assertEquals(lines+2,detalles.count());
    }
    @Test void missingLaterServiceAlsoRollsBack() {
        var v=vehiculo(); var s=servicio(true,"10.00");
        long headers=ordenes.count(), lines=detalles.count();
        var r=new OrdenRequestDTO(v.getId(),10,List.of(new DetalleOrdenRequestDTO(s.getId(),1),new DetalleOrdenRequestDTO(999999999L,1)));
        assertThrows(RecursoNoEncontradoException.class,() -> service.registrar(r,"asesor"));
        assertEquals(headers,ordenes.count()); assertEquals(lines,detalles.count());
    }
    @Test void mileageUsesLatestRegisteredOrderAndAllowsEqualValue() {
        var v=vehiculo(); var s=servicio(true,"10.00");
        var now=LocalDateTime.now();
        previa(v,45000,"REGISTRADA",now.minusDays(2),s,1);
        previa(v,90000,"ANULADA",now.minusDays(1),s,1);
        var ex=assertThrows(ReglaNegocioException.class,() -> service.registrar(request(v,44999,s),"asesor"));
        assertTrue(ex.getMessage().contains("45000"));
        var result=service.registrar(request(v,45000,s),"asesor");
        assertEquals(45000,result.kilometraje());
    }
    @Test void cancellationSameDayStateAndReasonRules() {
        var v=vehiculo(); var s=servicio(true,"10.00");
        var o=service.registrar(request(v,10,s),"asesor");
        assertThrows(DatosInvalidosException.class,() -> service.anular(o.id(),new AnularOrdenRequestDTO("         x")));
        var canceled=service.anular(o.id(),new AnularOrdenRequestDTO("Cancelación solicitada por el cliente"));
        assertEquals("ANULADA",canceled.estado()); assertNotNull(canceled.motivoAnulacion());
        assertEquals(1,canceled.detalles().size());
        assertThrows(ReglaNegocioException.class,() -> service.anular(o.id(),new AnularOrdenRequestDTO("Intento de anular nuevamente")));
        var old=previa(v,5,"REGISTRADA",LocalDateTime.now().minusDays(1),s,1);
        assertThrows(ReglaNegocioException.class,() -> service.anular(old.getId(),new AnularOrdenRequestDTO("Anulación fuera de plazo")));
    }
    @Test void dateFiltersAndJpqlReportExcludeCanceledOrders() {
        var v=vehiculo(); var a=servicio(true,"10.25"); var b=servicio(true,"20.00");
        LocalDate day=LocalDate.of(2001,2,3);
        previa(v,10,"REGISTRADA",day.atStartOfDay(),a,2);
        previa(v,20,"REGISTRADA",day.atTime(23,59,59),a,3);
        previa(v,30,"REGISTRADA",day.atTime(12,0),b,4);
        previa(v,40,"ANULADA",day.atTime(13,0),a,5);
        previa(v,50,"REGISTRADA",day.plusDays(1).atStartOfDay(),a,5);
        var report=reporte.ingresos(day,day);
        assertEquals(2,report.size()); assertEquals(b.getCodigo(),report.getFirst().codigo());
        assertEquals(0,new BigDecimal("80.00").compareTo(report.getFirst().importeTotal()));
        assertEquals(5L,report.get(1).cantidadTotal());
        assertEquals(0,new BigDecimal("51.25").compareTo(report.get(1).importeTotal()));
        var rows=service.consultar(v.getPlaca().toLowerCase(),EstadoOrden.REGISTRADA,day,day);
        assertEquals(3,rows.size()); assertTrue(rows.getFirst().fecha().isAfter(rows.getLast().fecha()));
        assertEquals(4,service.consultar(v.getPlaca(),null,day,day).size());
        assertTrue(service.consultar("NO-MATCH",null,null,null).isEmpty());
        assertTrue(reporte.ingresos(day.minusDays(1),day.minusDays(1)).isEmpty());
        assertThrows(DatosInvalidosException.class,() -> reporte.ingresos(day.plusDays(1),day));
    }
    private HttpResponse<String> http(String method,String path,String body) throws Exception {
        var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1"+path));
        b.header("Authorization","Bearer "+token("admin"));
        if(body!=null)b.header("Content-Type","application/json");
        return HttpClient.newHttpClient().send(b.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    @Test void httpValidationAndCors() throws Exception {
        var invalid=http("POST","/ordenes","{\"vehiculoId\":1,\"kilometraje\":-1,\"detalles\":[]}");
        assertEquals(400,invalid.statusCode()); assertTrue(invalid.body().contains("\"campos\""));
        var duplicate=http("POST","/ordenes","{\"vehiculoId\":1,\"kilometraje\":1,\"detalles\":[{\"servicioId\":1,\"cantidad\":1},{\"servicioId\":1,\"cantidad\":1}]}");
        assertEquals(400,duplicate.statusCode()); assertTrue(duplicate.body().contains("repetir"));
        assertEquals(400,http("GET","/ordenes?estado=INVALIDO",null).statusCode());
        assertEquals(400,http("GET","/ordenes?desde=bad",null).statusCode());
        assertEquals(404,http("GET","/ordenes/999999999",null).statusCode());
        var req=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1/ordenes"))
            .header("Origin","http://localhost:4200").header("Access-Control-Request-Method","POST")
            .header("Access-Control-Request-Headers","authorization,content-type").method("OPTIONS",HttpRequest.BodyPublishers.noBody()).build();
        var cors=HttpClient.newHttpClient().send(req,HttpResponse.BodyHandlers.ofString());
        assertEquals(200,cors.statusCode()); assertEquals("http://localhost:4200",cors.headers().firstValue("access-control-allow-origin").orElseThrow());
        assertTrue(cors.headers().firstValue("access-control-allow-methods").orElseThrow().contains("PATCH"));
    }
}
