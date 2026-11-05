package co.edu.konrad.consultaciudadano.personal.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ServicioLimiteRegistro implements ControlIntentosRegistro {
    private static final int MAX_INTENTOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(15);
    private static final String UPSERT_POSTGRES = """
            INSERT INTO auth_registro_intento (clave, intentos, ventana_inicia)
            VALUES (?, 1, CURRENT_TIMESTAMP)
            ON CONFLICT (clave) DO UPDATE SET
                intentos = CASE
                    WHEN auth_registro_intento.ventana_inicia <= CURRENT_TIMESTAMP - INTERVAL '15 minutes'
                    THEN 1 ELSE LEAST(auth_registro_intento.intentos + 1, 6)
                END,
                ventana_inicia = CASE
                    WHEN auth_registro_intento.ventana_inicia <= CURRENT_TIMESTAMP - INTERVAL '15 minutes'
                    THEN CURRENT_TIMESTAMP ELSE auth_registro_intento.ventana_inicia
                END
            RETURNING intentos
            """;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaccionNueva;
    private final boolean postgres;
    private final Clock clock;

    @Autowired
    public ServicioLimiteRegistro(JdbcTemplate jdbc, PlatformTransactionManager transactionManager,
            DataSource dataSource, Clock clock) {
        this.jdbc = jdbc;
        this.transaccionNueva = new TransactionTemplate(transactionManager);
        this.transaccionNueva.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.clock = clock;
        try (var connection = dataSource.getConnection()) {
            this.postgres = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql");
        } catch (java.sql.SQLException error) {
            throw new IllegalStateException("No se pudo identificar la base de datos para limitar el registro", error);
        }
    }

    @Override
    public boolean permitido(String ip) {
        var clave = hash(ip);
        var intentos = transaccionNueva.execute(status ->
                postgres ? registrarPostgres(clave) : registrarPortable(clave));
        return intentos != null && intentos <= MAX_INTENTOS;
    }

    private Integer registrarPostgres(String clave) {
        return jdbc.queryForObject(UPSERT_POSTGRES, Integer.class, clave);
    }

    private Integer registrarPortable(String clave) {
        var ahora = clock.instant();
        var intentosPrevios = jdbc.query("""
                SELECT intentos, ventana_inicia FROM auth_registro_intento WHERE clave = ?
                """, resultado -> {
            if (!resultado.next()) {
                return null;
            }
            return new EstadoRegistro(resultado.getInt("intentos"),
                    resultado.getTimestamp("ventana_inicia").toInstant());
        }, clave);
        var intentos = intentosPrevios == null || !intentosPrevios.ventanaInicia().plus(VENTANA).isAfter(ahora)
                ? 1 : Math.min(MAX_INTENTOS + 1, intentosPrevios.intentos() + 1);
        var ventanaInicia = intentosPrevios == null || !intentosPrevios.ventanaInicia().plus(VENTANA).isAfter(ahora)
                ? ahora : intentosPrevios.ventanaInicia();
        jdbc.update("""
                MERGE INTO auth_registro_intento (clave, intentos, ventana_inicia)
                KEY (clave) VALUES (?, ?, ?)
                """, clave, intentos, Timestamp.from(ventanaInicia));
        return intentos;
    }

    private String hash(String ip) {
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                    .digest(ip.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 no esta disponible", error);
        }
    }

    private record EstadoRegistro(int intentos, Instant ventanaInicia) {}
}
