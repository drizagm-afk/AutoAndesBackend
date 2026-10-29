package pe.edu.upeu.autoandes.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import pe.edu.upeu.autoandes.security.UsuarioPrincipal;

@Slf4j @Service
public class JwtService {
    private final SecretKey key;
    private final Clock clock;
    public JwtService(@Value("${app.jwt.secret:}") String secret,Clock clock) {
        this.clock=clock;
        if(secret.isBlank()) {
            byte[] random=new byte[32];new SecureRandom().nextBytes(random);key=Keys.hmacShaKeyFor(random);
            log.info("Clave JWT temporal generada para desarrollo; reiniciar invalida los tokens. Configure JWT_SECRET para conservarla.");
        } else key=Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
    public String generarToken(UsuarioPrincipal u) {
        var now=clock.instant();
        return Jwts.builder().issuer("autoandes-api").subject(u.username())
            .claim("roles",u.roles()).claim("permisos",u.permisos())
            .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(3600)))
            .signWith(key,Jwts.SIG.HS256).compact();
    }
    public String extraerUsername(String token) {
        var claims=Jwts.parser().verifyWith(key).requireIssuer("autoandes-api")
            .clock(() -> Date.from(clock.instant())).build().parseSignedClaims(token).getPayload();
        if(claims.getSubject()==null || claims.getSubject().isBlank() || claims.getExpiration()==null)
            throw new MalformedJwtException("Token incompleto");
        return claims.getSubject();
    }
}
