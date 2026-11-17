package co.edu.konrad.consultaciudadano;

import static org.junit.jupiter.api.Assertions.assertEquals;
import co.edu.konrad.consultaciudadano.ciudadano.repository.CiudadanoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationSmokeTest {
    @Autowired CiudadanoRepository ciudadanos;

    @Test
    void cargaContextoConFlywayYPostgresqlSimulado() {
        assertEquals(20, ciudadanos.count());
    }
}
