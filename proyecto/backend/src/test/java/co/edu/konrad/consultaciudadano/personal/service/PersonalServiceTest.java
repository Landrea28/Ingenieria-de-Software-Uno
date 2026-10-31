package co.edu.konrad.consultaciudadano.personal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import co.edu.konrad.consultaciudadano.catalogo.repository.FuerzaPublicaRepository;
import co.edu.konrad.consultaciudadano.catalogo.repository.RangoRepository;
import co.edu.konrad.consultaciudadano.catalogo.service.CatalogoService;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;
import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;
import co.edu.konrad.consultaciudadano.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonalServiceTest {
    @Mock CatalogoService catalogo;
    @Mock FuerzaPublicaRepository fuerzas;
    @Mock RangoRepository rangos;
    @Mock UsuarioRepository usuarios;

    @Test
    void noPersisteUsuarioSiFaltaValidacionDeSeguridad() {
        var service = new PersonalService(catalogo, fuerzas, rangos, usuarios,
                new RegistroPersonalIntegracionPendiente());
        var request = new RegistroPersonalRequest("8000000001", "Usuario", "Prueba1", "POLICIA", 1L,
                "DEMO-001", "demo@example.test");
        var error = assertThrows(NegocioException.class, () -> service.registrar(request));
        assertEquals(CodigoError.INTEGRACION_PENDIENTE, error.getCodigo());
        verify(usuarios, never()).saveAndFlush(any(Usuario.class));
    }
}
