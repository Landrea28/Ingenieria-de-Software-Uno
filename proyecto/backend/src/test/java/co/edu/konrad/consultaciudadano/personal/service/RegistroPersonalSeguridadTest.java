package co.edu.konrad.consultaciudadano.personal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import co.edu.konrad.consultaciudadano.auth.service.GeneradorContrasena;
import co.edu.konrad.consultaciudadano.auth.service.ValidadorContrasena;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistroPersonalSeguridadTest {
    @Mock ValidadorDefensa defensa;
    @Mock ValidadorContrasena validador;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ServicioCorreo correo;

    @Test
    void defensaRechazadaProduceRespuestaControlada400() {
        when(defensa.validar(solicitud())).thenReturn(ResultadoValidacionDefensa.RECHAZADO);
        var error = assertThrows(NegocioException.class, () -> integracion().validarContraDefensa(solicitud()));
        assertEquals(HttpStatus.BAD_REQUEST, error.getEstadoHttp());
        assertEquals(CodigoError.VALIDACION, error.getCodigo());
    }

    @Test
    void defensaNoDisponibleProduceRespuestaControlada503Nunca500() {
        when(defensa.validar(solicitud())).thenReturn(ResultadoValidacionDefensa.NO_DISPONIBLE);
        var error = assertThrows(NegocioException.class, () -> integracion().validarContraDefensa(solicitud()));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getEstadoHttp());
        assertEquals(CodigoError.INTEGRACION_PENDIENTE, error.getCodigo());
    }

    private RegistroPersonalSeguridad integracion() {
        return new RegistroPersonalSeguridad(defensa, new GeneradorContrasena(), validador, passwordEncoder, correo);
    }

    private RegistroPersonalRequest solicitud() {
        return new RegistroPersonalRequest("8000000001", "Usuario", "Prueba", "POLICIA", 1L,
                "DEMO-001", "usuario@example.test");
    }
}
