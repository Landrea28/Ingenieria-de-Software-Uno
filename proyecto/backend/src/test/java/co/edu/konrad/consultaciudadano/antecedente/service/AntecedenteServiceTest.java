package co.edu.konrad.consultaciudadano.antecedente.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import co.edu.konrad.consultaciudadano.antecedente.repository.AntecedenteRepository;
import co.edu.konrad.consultaciudadano.ciudadano.repository.CiudadanoRepository;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AntecedenteServiceTest {
    @Mock AntecedenteRepository antecedentes;
    @Mock CiudadanoRepository ciudadanos;
    @InjectMocks AntecedenteService service;

    @Test
    void ciudadanoSinEstadoNoSeClasificaComoNoRequerido() {
        when(antecedentes.buscarPorIdentificacion("9000000020")).thenReturn(Optional.empty());
        var error = assertThrows(NegocioException.class, () -> service.consultar("9000000020"));
        assertEquals(CodigoError.SIN_INFORMACION, error.getCodigo());
    }
}
