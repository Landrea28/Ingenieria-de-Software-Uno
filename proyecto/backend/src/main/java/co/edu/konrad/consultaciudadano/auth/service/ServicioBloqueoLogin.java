package co.edu.konrad.consultaciudadano.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ServicioBloqueoLogin {
    private static final int MAX_INTENTOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, EstadoIntentos> estados = new ConcurrentHashMap<>();

    public boolean estaBloqueada(String identificacion) {
        var estado = estados.get(identificacion);
        if (estado == null) {
            return false;
        }
        synchronized (estado) {
            if (estado.bloqueadoHasta != null && !estado.bloqueadoHasta.isAfter(Instant.now())) {
                estados.remove(identificacion, estado);
                return false;
            }
            if (estado.bloqueadoHasta == null && estado.ultimoIntento != null
                    && !estado.ultimoIntento.plus(DURACION_BLOQUEO).isAfter(Instant.now())) {
                estados.remove(identificacion, estado);
                return false;
            }
            return estado.bloqueadoHasta != null;
        }
    }

    public void registrarFallo(String identificacion) {
        estados.compute(identificacion, (id, estado) -> {
            var actual = estado == null ? new EstadoIntentos() : estado;
            synchronized (actual) {
                var ahora = Instant.now();
                if (actual.ultimoIntento != null
                        && !actual.ultimoIntento.plus(DURACION_BLOQUEO).isAfter(ahora)) {
                    actual.intentos = 0;
                    actual.bloqueadoHasta = null;
                }
                actual.intentos++;
                actual.ultimoIntento = ahora;
                if (actual.intentos >= MAX_INTENTOS) {
                    actual.bloqueadoHasta = ahora.plus(DURACION_BLOQUEO);
                }
            }
            return actual;
        });
    }

    public void limpiar(String identificacion) {
        estados.remove(identificacion);
    }

    @Scheduled(fixedDelayString = "PT15M")
    public void eliminarEstadosExpirados() {
        var ahora = Instant.now();
        estados.forEach((identificacion, estado) -> {
            synchronized (estado) {
                if (estado.ultimoIntento != null && !estado.ultimoIntento.plus(DURACION_BLOQUEO).isAfter(ahora)) {
                    estados.remove(identificacion, estado);
                }
            }
        });
    }

    private static final class EstadoIntentos {
        private int intentos;
        private Instant bloqueadoHasta;
        private Instant ultimoIntento;
    }
}
