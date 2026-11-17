package co.edu.konrad.consultaciudadano.auth.service;

import co.edu.konrad.consultaciudadano.auth.entity.TokenRevocado;
import co.edu.konrad.consultaciudadano.auth.repository.TokenRevocadoRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Revoca tokens al cerrar sesión, consulta si un token está revocado y elimina las revocaciones ya expiradas.
 */
@Service
@RequiredArgsConstructor
public class ServicioRevocacionToken {
    private final TokenRevocadoRepository tokens;

    @Transactional(readOnly = true)
    public boolean estaRevocado(String jti) {
        return tokens.existsById(jti);
    }

    @Transactional
    public void revocar(String jti, Instant expiraEn) {
        var ahora = OffsetDateTime.now(ZoneOffset.UTC);
        tokens.deleteAllByExpiraEnBefore(ahora);
        tokens.save(new TokenRevocado(jti, OffsetDateTime.ofInstant(expiraEn, ZoneOffset.UTC)));
    }

    @Scheduled(fixedDelayString = "PT1H")
    @Transactional
    public void eliminarRevocacionesExpiradas() {
        tokens.deleteAllByExpiraEnBefore(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
