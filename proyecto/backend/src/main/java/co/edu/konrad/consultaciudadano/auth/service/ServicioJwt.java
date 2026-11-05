package co.edu.konrad.consultaciudadano.auth.service;

import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServicioJwt {
    private final SecretKey clave;
    private final Duration expiracionCorta;
    private final Duration expiracionRecordar;

    public ServicioJwt(@Value("${security.jwt.secret}") String secreto,
            @Value("${security.jwt.short-ttl:PT15M}") Duration expiracionCorta,
            @Value("${security.jwt.remember-ttl:PT8H}") Duration expiracionRecordar) {
        if (secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes");
        }
        if (expiracionCorta.isNegative() || expiracionCorta.isZero()
                || expiracionRecordar.compareTo(expiracionCorta) < 0) {
            throw new IllegalArgumentException("La expiracion JWT configurada no es valida");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionCorta = expiracionCorta;
        this.expiracionRecordar = expiracionRecordar;
    }

    public TokenEmitido emitir(Usuario usuario, boolean recordar) {
        var ahora = Instant.now();
        var expira = ahora.plus(recordar ? expiracionRecordar : expiracionCorta);
        var jti = UUID.randomUUID().toString();
        var roles = usuario.getPerfiles().stream().map(perfil -> perfil.getCodigo()).distinct().toList();
        var token = Jwts.builder().subject(usuario.getIdentificacion()).id(jti)
                .claim("roles", roles).claim("version", usuario.getVersionSeguridad())
                .issuedAt(Date.from(ahora)).expiration(Date.from(expira))
                .signWith(clave).compact();
        return new TokenEmitido(token, jti, expira, Duration.between(ahora, expira).toSeconds());
    }

    public Claims validar(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }

    public record TokenEmitido(String valor, String jti, Instant expiraEn, long expiraEnSegundos) {}
}
