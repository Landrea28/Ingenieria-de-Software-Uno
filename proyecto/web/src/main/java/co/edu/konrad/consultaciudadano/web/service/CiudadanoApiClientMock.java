package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.CiudadanoDto;
import co.edu.konrad.consultaciudadano.web.dto.PaginaCiudadanos;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * DATOS SIMULADOS DE PRUEBA. Solo se activa con el perfil "mock".
 * Las identificaciones 1000000001..3 no existen en el seed del backend (9000000001..)
 * y los nombres son ficticios. Lo que se registra vive en memoria y se pierde al reiniciar.
 */
@Service
@Profile("mock")
public class CiudadanoApiClientMock implements CiudadanoApiClient {

    private final List<CiudadanoDto> ciudadanos = new CopyOnWriteArrayList<>(List.of(
            new CiudadanoDto(1L, "1000000001", "Prueba Uno", "Simulado Mock",
                    LocalDate.of(1990, 1, 15), "Bogota", LocalDate.of(2008, 2, 20), "Bogota",
                    "+", "O", new BigDecimal("1.70")),
            new CiudadanoDto(2L, "1000000002", "Prueba Dos", "Simulado Mock",
                    LocalDate.of(1985, 6, 3), "Medellin", LocalDate.of(2003, 7, 10), "Medellin",
                    "-", "A", new BigDecimal("1.62")),
            new CiudadanoDto(3L, "1000000003", "Prueba Tres", "Simulado Mock",
                    LocalDate.of(2000, 11, 28), "Cali", LocalDate.of(2018, 12, 5), "Bogota",
                    "+", "AB", new BigDecimal("1.81"))));

    private final AtomicLong siguienteId = new AtomicLong(ciudadanos.size() + 1);

    // Imita al backend: filtra, ordena por apellidos y nombres, y pagina.
    @Override
    public PaginaCiudadanos listar(String busqueda, int pagina) {
        String texto = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);
        List<CiudadanoDto> filtrados = ciudadanos.stream()
                .filter(c -> texto.isEmpty() || c.identificacion().contains(texto)
                        || (c.nombres() + " " + c.apellidos()).toLowerCase(Locale.ROOT).contains(texto))
                .sorted(Comparator.comparing(CiudadanoDto::apellidos).thenComparing(CiudadanoDto::nombres))
                .toList();
        int total = filtrados.size();
        int desde = Math.min(pagina * TAMANO_PAGINA, total);
        int hasta = Math.min(desde + TAMANO_PAGINA, total);
        int totalPaginas = (total + TAMANO_PAGINA - 1) / TAMANO_PAGINA;
        return new PaginaCiudadanos(filtrados.subList(desde, hasta), total, totalPaginas, TAMANO_PAGINA, pagina,
                pagina == 0, pagina >= totalPaginas - 1);
    }

    @Override
    public Optional<CiudadanoDto> buscar(String identificacion) {
        return ciudadanos.stream().filter(c -> c.identificacion().equals(identificacion)).findFirst();
    }

    // synchronized: la verificación de duplicado y el alta deben ocurrir juntas.
    @Override
    public synchronized CiudadanoDto registrar(CiudadanoDto c) {
        if (buscar(c.identificacion()).isPresent()) {
            throw new CiudadanoDuplicadoException(c.identificacion());
        }
        CiudadanoDto creado = new CiudadanoDto(siguienteId.getAndIncrement(), c.identificacion(), c.nombres(),
                c.apellidos(), c.fechaNacimiento(), c.lugarNacimiento(), c.fechaExpedicion(), c.lugarExpedicion(),
                c.rh(), c.grupoSanguineo(), c.estatura());
        ciudadanos.add(creado);
        return creado;
    }

    @Override
    public boolean esSimulado() {
        return true;
    }
}
