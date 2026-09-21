package SystemITR.JosueGuinea.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${security.jwt.secret}")
    private String jwtSecreto;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.expiration}")
    private long expirationMs;

    private final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    /***
     * Construye y firma un token JWT compacto con los claims básicos y personalizados
     * @param id        Identificador único de la sesión o del registro del usuario
     * @param username  Nombre del usuario (Subject)
     * @param rol       Rol asignado al usuario dentro del sistema
     * @param mensaje   Mensaje o información adicional (opcional)
     * @return
     */
    public String create(String id, String username, String rol, String mensaje){
        SecretKey signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecreto));
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);
        //Construcción y firma del payload del JWT
        return Jwts.builder()
                .setId(id)
                .setIssuedAt(now)               //Establece la fecha de emisión del token
                .setSubject(username)           //Define el usuario dueño del token
                .claim("id", id)            // Claim personalizado: id
                .claim("rol", rol)          //claim personalizado: rol del usuario
                .setIssuer(issuer)              // Define el emisor del
                .setExpiration(expirationMs >= 0 ? expiration : null)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseTokenAndClaims(String token) throws ExpiredJwtException, MalformedJwtException {
        // Validar la firma del token utilizando la clave pública/privada configurada
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecreto)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractRol(String token) {
        Claims claims = parseTokenAndClaims(token);
        return claims.get("rol", String.class);
    }
}
