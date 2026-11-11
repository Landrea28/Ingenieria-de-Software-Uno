package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.EstadoAntecedente;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * SIMULADO: PDF de prueba. Solo perfil "mock".
 * REQUERIDO tiene un ciudadano y NO_REQUERIDO ninguno, para poder mostrar en la demo tanto el PDF
 * como el mensaje de "sin resultados".
 */
@Service
@Profile("mock")
public class ReporteApiClientMock implements ReporteApiClient {

    private static final Map<EstadoAntecedente, List<String>> IDENTIFICACIONES = Map.of(
            EstadoAntecedente.REQUERIDO, List.of("1000000002"),
            EstadoAntecedente.NO_REQUERIDO, List.of());

    private final CiudadanoApiClient ciudadanos;

    public ReporteApiClientMock(CiudadanoApiClient ciudadanos) {
        this.ciudadanos = ciudadanos;
    }

    @Override
    public byte[] ciudadanosPorEstado(EstadoAntecedente estado) {
        List<String> filas = IDENTIFICACIONES.get(estado).stream()
                .flatMap(id -> ciudadanos.buscar(id).stream())
                .map(c -> c.identificacion() + "   " + c.nombres() + "   " + c.apellidos())
                .toList();
        if (filas.isEmpty()) {
            throw new ReporteSinResultadosException(estado);
        }
        List<String> lineas = new ArrayList<>(List.of(
                "Reporte de ciudadanos: " + estado.getEtiqueta(),
                "Generado: " + LocalDate.now(),
                "DATOS SIMULADOS - PRUEBA (no provienen de la API)",
                "",
                "Identificación   Nombres   Apellidos"));
        lineas.addAll(filas);
        return PdfSimulado.generar("Sistema de Consulta Ciudadano", lineas);
    }
}
