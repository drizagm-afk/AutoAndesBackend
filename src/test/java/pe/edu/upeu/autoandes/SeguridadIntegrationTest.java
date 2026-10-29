package pe.edu.upeu.autoandes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.security.PermisosPorRol;
import tools.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SeguridadIntegrationTest extends SecurityTestSupport {
    @Value("${local.server.port}") int port;
    @Value("${app.jwt.secret}") String secret;
    @Autowired ObjectMapper json;
    private HttpResponse<String> http(String method,String path,String body,String token) throws Exception {
        var request=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path));
        if(token!=null)request.header("Authorization","Bearer "+token);
        if(body!=null)request.header("Content-Type","application/json");
        return HttpClient.newHttpClient().send(request.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> login(String username,String password) throws Exception {
        return http("POST","/api/v1/auth/login","{\"username\":\""+username+"\",\"password\":\""+password+"\"}",null);
    }
    private void commonError(HttpResponse<String> r,int status) {
        assertEquals(status,r.statusCode(),r.body());var e=json.readTree(r.body());
        for(var name:List.of("timestamp","status","error","mensaje","ruta"))assertTrue(e.has(name));
        assertFalse(e.has("campos"));assertEquals(status,e.get("status").asInt());
    }
    @Test void loginAndCurrentUserNeverExposePasswords() throws Exception {
        for(String username:List.of("admin","asesor","jefe")) {
            var response=login(username,"Auto2026*");assertEquals(200,response.statusCode(),response.body());
            var body=json.readTree(response.body());assertEquals("Bearer",body.get("tipo").asString());assertEquals(3600,body.get("expiraEnSegundos").asLong());
            assertTrue(body.has("nombreCompleto"));assertTrue(body.has("roles"));assertTrue(body.has("permisos"));
            assertFalse(body.has("password"));assertFalse(response.body().contains("$2a$"));
            var me=http("GET","/api/v1/auth/me",null,body.get("token").asString());assertEquals(200,me.statusCode());
            assertEquals(username,json.readTree(me.body()).get("username").asString());assertFalse(json.readTree(me.body()).has("password"));
            assertTrue(encoder.matches("Auto2026*",usuarios.findByUsername(username).orElseThrow().getPassword()));
        }
        var invalid=login("admin","");assertEquals(400,invalid.statusCode());assertTrue(json.readTree(invalid.body()).has("campos"));
    }
    @Test void badPasswordMissingAndInactiveHaveIdentical401Messages() throws Exception {
        String message=null;
        for(var pair:List.of(new String[]{"admin","wrong"},new String[]{"missing","Auto2026*"},new String[]{"inactivo","Auto2026*"},new String[]{"admin","漢".repeat(30)})) {
            var response=login(pair[0],pair[1]);commonError(response,401);
            var actual=json.readTree(response.body()).get("mensaje").asString();
            if(message==null)message=actual;else assertEquals(message,actual);
        }
    }
    @Test void allBusinessRoutesRequireTokenBeforeInputValidation() throws Exception {
        for(var pair:List.of(new String[]{"GET","/auth/me"},new String[]{"GET","/clientes"},new String[]{"GET","/servicios"},
            new String[]{"POST","/servicios"},new String[]{"PUT","/servicios/1"},new String[]{"DELETE","/servicios/1"},new String[]{"PATCH","/servicios/1/estado"},
            new String[]{"GET","/vehiculos"},new String[]{"POST","/vehiculos"},new String[]{"PUT","/vehiculos/1"},new String[]{"DELETE","/vehiculos/1"},
            new String[]{"GET","/ordenes"},new String[]{"POST","/ordenes"},new String[]{"GET","/ordenes/1"},new String[]{"PATCH","/ordenes/1/anular"},
            new String[]{"GET","/reportes/ingresos-por-servicio"}))commonError(http(pair[0],"/api/v1"+pair[1],null,null),401);
        assertEquals(200,http("GET","/v3/api-docs",null,null).statusCode());
    }
    @Test void alteredExpiredAndMissingExpirationTokensAreRejected() throws Exception {
        String valid=token("admin");String[] parts=valid.split("\\.");
        String changed=parts[0]+"."+(parts[1].charAt(0)=='A'?'B':'A')+parts[1].substring(1)+"."+parts[2];
        commonError(http("GET","/api/v1/auth/me",null,changed),401);
        var key=Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        String expired=Jwts.builder().subject("admin").issuer("autoandes-api").issuedAt(Date.from(Instant.now().minusSeconds(7200))).expiration(Date.from(Instant.now().minusSeconds(3600))).signWith(key).compact();
        commonError(http("GET","/api/v1/auth/me",null,expired),401);
        String noExpiry=Jwts.builder().subject("admin").issuer("autoandes-api").signWith(key).compact();
        commonError(http("GET","/api/v1/auth/me",null,noExpiry),401);
        commonError(http("GET","/api/v1/auth/me",null,"invalid"),401);
    }
    @Test void permissionMatrixEnforcedAnd403DoesNotInvalidateToken() throws Exception {
        String asesor=token("asesor"),jefe=token("jefe");
        for(var pair:List.of(new String[]{"POST","/servicios"},new String[]{"PUT","/servicios/1"},new String[]{"DELETE","/servicios/1"},
            new String[]{"PATCH","/servicios/1/estado"},new String[]{"PATCH","/ordenes/1/anular"},new String[]{"GET","/reportes/ingresos-por-servicio"}))
            commonError(http(pair[0],"/api/v1"+pair[1],null,asesor),403);
        for(var pair:List.of(new String[]{"POST","/vehiculos"},new String[]{"PUT","/vehiculos/1"},new String[]{"DELETE","/vehiculos/1"},new String[]{"POST","/ordenes"}))
            commonError(http(pair[0],"/api/v1"+pair[1],null,jefe),403);
        assertEquals(200,http("GET","/api/v1/auth/me",null,asesor).statusCode());
        assertEquals(200,http("GET","/api/v1/reportes/ingresos-por-servicio",null,jefe).statusCode());
        assertEquals(400,http("POST","/api/v1/servicios","{}",jefe).statusCode());
        assertEquals(400,http("POST","/api/v1/ordenes","{}",asesor).statusCode());
    }
    @Test void multipleRolesReceiveUnionAndDisabledAccountLosesAccess() throws Exception {
        Usuario u=new Usuario();u.setUsername("multi");u.setNombreCompleto("Usuario con dos roles");u.setActivo(true);u.setPassword(encoder.encode("Auto2026*"));
        u.setRoles(new HashSet<>(List.of(roles.findByNombre("ASESOR").orElseThrow(),roles.findByNombre("JEFE_TALLER").orElseThrow())));u=usuarios.saveAndFlush(u);
        assertEquals(8,PermisosPorRol.permisos(u.getRoles()).size());
        String token=token("multi");
        assertEquals(400,http("POST","/api/v1/ordenes","{}",token).statusCode());
        assertEquals(200,http("GET","/api/v1/reportes/ingresos-por-servicio",null,token).statusCode());
        u.setActivo(false);usuarios.saveAndFlush(u);commonError(http("GET","/api/v1/auth/me",null,token),401);
    }
}
