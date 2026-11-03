package co.edu.konrad.consultaciudadano.reporte.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import co.edu.konrad.consultaciudadano.antecedente.entity.EstadoAntecedente;
import co.edu.konrad.consultaciudadano.antecedente.repository.AntecedenteRepository;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {
    @Mock AntecedenteRepository repository;
    @InjectMocks ReporteService service;

    @Test
    void generaArchivoPdfReal() {
        var ciudadano = new co.edu.konrad.consultaciudadano.ciudadano.entity.Ciudadano("9000000001");
        ciudadano.setNombres("Clara");
        ciudadano.setApellidos("Rivera");
        var registro = new co.edu.konrad.consultaciudadano.antecedente.entity.Antecedente(
                ciudadano, EstadoAntecedente.REQUERIDO);
        when(repository.listarPorEstado(EstadoAntecedente.REQUERIDO)).thenReturn(List.of(registro));
        var pdf = service.generar(EstadoAntecedente.REQUERIDO);
        assertEquals("%PDF", new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test
    void noGeneraPdfVacio() {
        when(repository.listarPorEstado(EstadoAntecedente.REQUERIDO)).thenReturn(List.of());
        var error = assertThrows(NegocioException.class,
                () -> service.generar(EstadoAntecedente.REQUERIDO));
        assertEquals(CodigoError.SIN_RESULTADOS, error.getCodigo());
    }
}
