package pe.edu.upeu.autoandes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.repository.*;
import java.net.URI;
import java.net.http.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CrudIntegrationTest {
    @Value("${local.server.port}") int port;
    @Autowired ClienteRepository clientes;
    @Autowired VehiculoRepository vehiculos;
    @Autowired ServicioRepository servicios;
    @Autowired OrdenServicioRepository ordenes;
    @Autowired DetalleOrdenRepository detalles;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1" + path));
        if (body != null) builder.header("Content-Type", "application/json");
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private String servicio(String codigo) {
        return "{\"codigo\":\"" + codigo + "\",\"nombre\":\"Servicio de prueba\",\"precio\":95.50,\"duracionMin\":45}";
    }
    @Test void servicioLifecycleAndDuplicateUpdate() throws Exception {
        assertEquals(201, request("POST", "/servicios", servicio("SRV-901")).statusCode());
        var entity = servicios.findAll().stream().filter(s -> s.getCodigo().equals("SRV-901")).findFirst().orElseThrow();
        assertEquals(409, request("POST", "/servicios", servicio("SRV-901")).statusCode());
        assertEquals(201, request("POST", "/servicios", servicio("SRV-902")).statusCode());
        assertEquals(409, request("PUT", "/servicios/" + entity.getId(), servicio("SRV-902")).statusCode());
        assertEquals(200, request("PUT", "/servicios/" + entity.getId(), servicio("SRV-903")).statusCode());
        var state = request("PATCH", "/servicios/" + entity.getId() + "/estado", "{\"activo\":false}");
        assertEquals(200, state.statusCode()); assertTrue(state.body().contains("\"activo\":false"));
        assertEquals(204, request("DELETE", "/servicios/" + entity.getId(), null).statusCode());
        assertEquals(404, request("GET", "/servicios/" + entity.getId(), null).statusCode());
    }
    @Test void validationAndMalformedInputUseCommonErrors() throws Exception {
        var invalid = request("POST", "/servicios", "{\"codigo\":\"BAD\",\"nombre\":\"X\",\"precio\":0,\"duracionMin\":1}");
        assertEquals(400, invalid.statusCode());
        for (String key : new String[]{"timestamp", "status", "error", "mensaje", "ruta", "campos", "codigo", "precio", "duracionMin"}) assertTrue(invalid.body().contains("\"" + key + "\""));
        assertEquals(400, request("POST", "/vehiculos", "{broken").statusCode());
        assertEquals(400, request("GET", "/vehiculos?clienteId=bad", null).statusCode());
        var missing = request("GET", "/servicios/999999", null);
        assertEquals(404, missing.statusCode()); assertFalse(missing.body().contains("\"campos\""));
    }
    @Test void vehiculoRelationshipFilterAndLifecycle() throws Exception {
        Cliente c = new Cliente(); c.setDocumento("TEST0001"); c.setNombres("Cliente de prueba"); c = clientes.saveAndFlush(c);
        String body = "{\"placa\":\"TST-901\",\"marca\":\"Toyota\",\"modelo\":\"Yaris\",\"anio\":2020,\"clienteId\":" + c.getId() + "}";
        var created = request("POST", "/vehiculos", body);
        assertEquals(201, created.statusCode()); assertTrue(created.body().contains("\"clienteNombre\":\"Cliente de prueba\""));
        assertFalse(created.body().contains("\"cliente\":"));
        assertEquals(409, request("POST", "/vehiculos", body).statusCode());
        Long id = vehiculos.findAll().stream().filter(v -> v.getPlaca().equals("TST-901")).findFirst().orElseThrow().getId();
        assertEquals(200, request("PUT", "/vehiculos/" + id, body.replace("Yaris", "Corolla")).statusCode());
        assertTrue(request("GET", "/vehiculos?clienteId=" + c.getId(), null).body().contains("Corolla"));
        assertEquals("[]", request("GET", "/vehiculos?clienteId=999999", null).body());
        assertEquals(404, request("POST", "/vehiculos", body.replace("TST-901", "TST-902").replace("\"clienteId\":" + c.getId(), "\"clienteId\":999999")).statusCode());
        assertEquals(204, request("DELETE", "/vehiculos/" + id, null).statusCode());
    }
    @Test void referencedServiceAndVehicleCannotBeDeleted() throws Exception {
        Cliente c = new Cliente(); c.setDocumento("TEST0002"); c.setNombres("Cliente relacionado"); c = clientes.saveAndFlush(c);
        Vehiculo v = new Vehiculo(); v.setPlaca("REF-901"); v.setMarca("Kia"); v.setModelo("Rio"); v.setAnio(2021); v.setCliente(c); v = vehiculos.saveAndFlush(v);
        Servicio s = new Servicio(); s.setCodigo("SRV-904"); s.setNombre("Servicio relacionado"); s.setPrecio(BigDecimal.TEN); s.setDuracionMin(30); s.setActivo(true); s = servicios.saveAndFlush(s);
        OrdenServicio o = new OrdenServicio(); o.setNumero("OS-999901"); o.setFecha(LocalDateTime.now()); o.setVehiculo(v); o.setKilometraje(10); o.setEstado("REGISTRADA"); o.setTotal(BigDecimal.TEN); o.setUsuarioRegistro("asesor"); o = ordenes.saveAndFlush(o);
        DetalleOrden d = new DetalleOrden(); d.setOrden(o); d.setServicio(s); d.setCantidad(1); d.setPrecioUnitario(BigDecimal.TEN); d.setSubtotal(BigDecimal.TEN); detalles.saveAndFlush(d);
        assertEquals(409, request("DELETE", "/servicios/" + s.getId(), null).statusCode());
        assertEquals(409, request("DELETE", "/vehiculos/" + v.getId(), null).statusCode());
        assertEquals(200, request("PATCH", "/servicios/" + s.getId() + "/estado", "{\"activo\":false}").statusCode());
        assertTrue(servicios.existsById(s.getId())); assertTrue(vehiculos.existsById(v.getId()));
    }
}
