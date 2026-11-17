package co.edu.konrad.consultaciudadano.ciudadano.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import co.edu.konrad.consultaciudadano.ciudadano.dto.CiudadanoRequest;
import co.edu.konrad.consultaciudadano.ciudadano.dto.CiudadanoUpdateRequest;
import co.edu.konrad.consultaciudadano.ciudadano.entity.Ciudadano;
import co.edu.konrad.consultaciudadano.ciudadano.entity.GrupoSanguineo;
import co.edu.konrad.consultaciudadano.ciudadano.repository.CiudadanoRepository;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CiudadanoServiceTest {
    @Mock
    CiudadanoRepository repository;
    CiudadanoService service;

    @BeforeEach
    void setup() { service = new CiudadanoService(repository); }

    @Test
    void rechazaIdentificacionDuplicada() {
        when(repository.existsByIdentificacion("9000000001")).thenReturn(true);
        var error = assertThrows(NegocioException.class, () -> service.crear(crearRequest()));
        assertEquals(CodigoError.DUPLICADO, error.getCodigo());
    }

    @Test
    void rechazaFechaExpedicionAnterior() {
        var request = new CiudadanoRequest("9000000001", "Clara", "Rivera", LocalDate.of(1995, 2, 14),
                "Bogota", LocalDate.of(1990, 2, 14), "Bogota", "+", GrupoSanguineo.A,
                new BigDecimal("1.71"));
        var error = assertThrows(NegocioException.class, () -> service.crear(request));
        assertEquals(CodigoError.VALIDACION, error.getCodigo());
    }

    @Test
    void permiteCrearCiudadanoValido() {
        when(repository.saveAndFlush(any(Ciudadano.class))).thenAnswer(call -> call.getArgument(0));
        var respuesta = service.crear(crearRequest());
        assertEquals("9000000001", respuesta.identificacion());
        assertEquals("Clara", respuesta.nombres());
    }

    @Test
    void actualizaSinCambiarIdentificacion() {
        var ciudadano = new Ciudadano("9000000001");
        when(repository.findByIdentificacion("9000000001")).thenReturn(Optional.of(ciudadano));
        when(repository.save(any(Ciudadano.class))).thenAnswer(call -> call.getArgument(0));
        var request = new CiudadanoUpdateRequest("Maria", "Lopez", LocalDate.of(1995, 2, 14),
                "Bogota", LocalDate.of(2013, 3, 22), "Bogota", "-", GrupoSanguineo.B,
                new BigDecimal("1.70"));
        var respuesta = service.actualizar("9000000001", request);
        assertEquals("9000000001", respuesta.identificacion());
        assertEquals("Maria", respuesta.nombres());
    }

    private CiudadanoRequest crearRequest() {
        return new CiudadanoRequest("9000000001", "Clara", "Rivera", LocalDate.of(1995, 2, 14),
                "Bogota", LocalDate.of(2013, 3, 22), "Bogota", "+", GrupoSanguineo.A,
                new BigDecimal("1.71"));
    }
}
