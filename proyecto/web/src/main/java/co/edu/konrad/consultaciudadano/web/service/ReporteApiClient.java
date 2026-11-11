package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.EstadoAntecedente;

/** Contrato del cliente de reportes (/api/reportes). El PDF lo genera el backend. */
public interface ReporteApiClient {

    /**
     * GET /api/reportes/ciudadanos?estado=. Devuelve los bytes del PDF.
     * @throws ReporteSinResultadosException si no hay ciudadanos con ese estado (404 SIN_RESULTADOS)
     * @throws ApiNoDisponibleException si la API no responde o no devuelve un PDF
     */
    byte[] ciudadanosPorEstado(EstadoAntecedente estado);
}
