package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.CiudadanoDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * DATOS SIMULADOS DE PRUEBA. Solo se activa con el perfil "mock".
 * Las identificaciones 1000000001..3 no existen en el seed del backend (9000000001..)
 * y los nombres son ficticios.
 */
@Service
@Profile("mock")
public class CiudadanoApiClientMock implements CiudadanoApiClient {

    private static final List<CiudadanoDto> CIUDADANOS = List.of(
            new CiudadanoDto(1L, "1000000001", "Prueba Uno", "Simulado Mock",
                    LocalDate.of(1990, 1, 15), "Bogota", LocalDate.of(2008, 2, 20), "Bogota",
                    "+", "O", new BigDecimal("1.70")),
            new CiudadanoDto(2L, "1000000002", "Prueba Dos", "Simulado Mock",
                    LocalDate.of(1985, 6, 3), "Medellin", LocalDate.of(2003, 7, 10), "Medellin",
                    "-", "A", new BigDecimal("1.62")),
            new CiudadanoDto(3L, "1000000003", "Prueba Tres", "Simulado Mock",
                    LocalDate.of(2000, 11, 28), "Cali", LocalDate.of(2018, 12, 5), "Bogota",
                    "+", "AB", new BigDecimal("1.81")));

    @Override
    public List<CiudadanoDto> listar(String busqueda) {
        if (busqueda == null || busqueda.isBlank()) {
            return CIUDADANOS;
        }
        String texto = busqueda.trim().toLowerCase(Locale.ROOT);
        return CIUDADANOS.stream()
                .filter(c -> c.identificacion().contains(texto)
                        || (c.nombres() + " " + c.apellidos()).toLowerCase(Locale.ROOT).contains(texto))
                .toList();
    }

    @Override
    public Optional<CiudadanoDto> buscar(String identificacion) {
        return CIUDADANOS.stream().filter(c -> c.identificacion().equals(identificacion)).findFirst();
    }

    @Override
    public boolean esSimulado() {
        return true;
    }
}
