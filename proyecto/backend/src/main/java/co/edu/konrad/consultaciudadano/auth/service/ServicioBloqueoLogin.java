package co.edu.konrad.consultaciudadano.auth.service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ServicioBloqueoLogin {
    private static final int MAX_INTENTOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);
    private static final String SQL_POSTGRES_UPSERT = """
            INSERT INTO auth_login_intento (identificacion, fallos, ultimo_intento, bloqueado_hasta)
            VALUES (?, 1, CURRENT_TIMESTAMP, NULL)
            ON CONFLICT (identificacion) DO UPDATE SET
                fallos = CASE
                    WHEN auth_login_intento.ultimo_intento <= CURRENT_TIMESTAMP - INTERVAL '15 minutes'
                        OR (auth_login_intento.bloqueado_hasta IS NOT NULL
                            AND auth_login_intento.bloqueado_hasta <= CURRENT_TIMESTAMP)
                    THEN 1 ELSE LEAST(auth_login_intento.fallos + 1, 5)
                END,
                bloqueado_hasta = CASE
                    WHEN CASE
                        WHEN auth_login_intento.ultimo_intento <= CURRENT_TIMESTAMP - INTERVAL '15 minutes'
                            OR (auth_login_intento.bloqueado_hasta IS NOT NULL
                                AND auth_login_intento.bloqueado_hasta <= CURRENT_TIMESTAMP)
                        THEN 1 ELSE LEAST(auth_login_intento.fallos + 1, 5)
                    END >= 5
                    THEN CURRENT_TIMESTAMP + INTERVAL '15 minutes'
                    ELSE NULL
                END,
                ultimo_intento = CURRENT_TIMESTAMP
            """;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaccionNueva;
    private final boolean postgres;

    public ServicioBloqueoLogin(JdbcTemplate jdbc, PlatformTransactionManager transactionManager,
            DataSource dataSource) {
        this.jdbc = jdbc;
        this.transaccionNueva = new TransactionTemplate(transactionManager);
        this.transaccionNueva.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        try (var connection = dataSource.getConnection()) {
            this.postgres = connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql");
        } catch (java.sql.SQLException error) {
            throw new IllegalStateException("No se pudo identificar la base de datos para el bloqueo de login", error);
        }
    }

    public boolean estaBloqueada(String identificacion) {
        var bloqueadaHasta = jdbc.query("SELECT bloqueado_hasta FROM auth_login_intento WHERE identificacion = ?",
                resultado -> resultado.next() ? resultado.getTimestamp(1) : null, identificacion);
        return bloqueadaHasta != null && bloqueadaHasta.toInstant().isAfter(Instant.now());
    }

    public void registrarFallo(String identificacion) {
        transaccionNueva.executeWithoutResult(status -> {
            if (postgres) {
                jdbc.update(SQL_POSTGRES_UPSERT, identificacion);
            } else {
                registrarFalloPortable(identificacion);
            }
        });
    }

    public void limpiar(String identificacion) {
        transaccionNueva.executeWithoutResult(status ->
                jdbc.update("DELETE FROM auth_login_intento WHERE identificacion = ?", identificacion));
    }

    @Scheduled(fixedDelayString = "PT15M")
    public void eliminarEstadosExpirados() {
        jdbc.update("""
                DELETE FROM auth_login_intento
                WHERE ultimo_intento <= ?
                  AND (bloqueado_hasta IS NULL OR bloqueado_hasta <= CURRENT_TIMESTAMP)
                """, Timestamp.from(Instant.now().minus(DURACION_BLOQUEO)));
    }

    private void registrarFalloPortable(String identificacion) {
        var ahora = Instant.now();
        var intentosPrevios = jdbc.query("""
                SELECT fallos, ultimo_intento, bloqueado_hasta
                FROM auth_login_intento WHERE identificacion = ?
                """, resultado -> {
            if (!resultado.next()) {
                return null;
            }
            var ultimoIntento = resultado.getTimestamp("ultimo_intento").toInstant();
            var bloqueadoHasta = resultado.getTimestamp("bloqueado_hasta");
            return new EstadoIntentos(resultado.getInt("fallos"), ultimoIntento,
                    bloqueadoHasta == null ? null : bloqueadoHasta.toInstant());
        }, identificacion);

        var intentos = intentosPrevios == null
                || !intentosPrevios.ultimoIntento().plus(DURACION_BLOQUEO).isAfter(ahora)
                || (intentosPrevios.bloqueadoHasta() != null
                        && !intentosPrevios.bloqueadoHasta().isAfter(ahora))
                ? 1 : Math.min(MAX_INTENTOS, intentosPrevios.fallos() + 1);
        var bloqueadoHasta = intentos >= MAX_INTENTOS ? ahora.plus(DURACION_BLOQUEO) : null;
        jdbc.update("""
                MERGE INTO auth_login_intento (identificacion, fallos, ultimo_intento, bloqueado_hasta)
                KEY (identificacion) VALUES (?, ?, ?, ?)
                """, identificacion, intentos, Timestamp.from(ahora),
                bloqueadoHasta == null ? null : Timestamp.from(bloqueadoHasta));
    }

    private record EstadoIntentos(int fallos, Instant ultimoIntento, Instant bloqueadoHasta) {}
}
