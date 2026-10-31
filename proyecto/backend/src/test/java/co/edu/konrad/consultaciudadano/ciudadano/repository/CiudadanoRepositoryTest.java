package co.edu.konrad.consultaciudadano.ciudadano.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import co.edu.konrad.consultaciudadano.antecedente.repository.AntecedenteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CiudadanoRepositoryTest {
    @Autowired CiudadanoRepository ciudadanos;
    @Autowired AntecedenteRepository antecedentes;

    @Test
    void encuentraDatosFicticiosYBuscaPorNombre() {
        assertEquals(20, ciudadanos.count());
        assertTrue(ciudadanos.existsByIdentificacion("9000000001"));
        var pagina = ciudadanos.findByIdentificacionContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                "Clara", "Clara", "Clara", PageRequest.of(0, 10));
        assertEquals(1, pagina.getTotalElements());
    }

    @Test
    void diferenciaFaltaDeAntecedentes() {
        assertTrue(antecedentes.buscarPorIdentificacion("9000000020").isEmpty());
        assertTrue(antecedentes.buscarPorIdentificacion("9000000001").isPresent());
    }
}
