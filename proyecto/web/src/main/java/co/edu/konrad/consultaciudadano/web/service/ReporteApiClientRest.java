package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.EstadoAntecedente;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Implementación real de /api/reportes (PDF generado por el backend). */
@Service
@Profile("api")
public class ReporteApiClientRest implements ReporteApiClient {

    private final RestClient api;

    public ReporteApiClientRest(RestClient api) {
        this.api = api;
    }

    @Override
    public byte[] ciudadanosPorEstado(EstadoAntecedente estado) {
        ResponseEntity<byte[]> respuesta;
        try {
            respuesta = api.get()
                    .uri("/api/reportes/ciudadanos?estado={estado}", estado.name())
                    // También JSON: así el backend puede responder sus errores (ErrorResponse).
                    .accept(MediaType.APPLICATION_PDF, MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(byte[].class);
        } catch (ApiNoEncontradoException e) {
            if (e.sinResultados()) {
                throw new ReporteSinResultadosException(estado);
            }
            // 404 sin SIN_RESULTADOS: la ruta no existe (backend sin el endpoint).
            throw new ApiNoDisponibleException(e);
        }
        MediaType tipo = respuesta.getHeaders().getContentType();
        byte[] pdf = respuesta.getBody();
        if (pdf == null || pdf.length == 0 || tipo == null || !MediaType.APPLICATION_PDF.isCompatibleWith(tipo)) {
            throw new ApiNoDisponibleException();
        }
        return pdf;
    }
}
