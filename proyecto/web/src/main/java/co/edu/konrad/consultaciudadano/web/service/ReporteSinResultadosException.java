package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.EstadoAntecedente;

/** No hay ciudadanos con el estado pedido: el backend no genera el PDF (404 SIN_RESULTADOS). */
public class ReporteSinResultadosException extends ApiException {

    public ReporteSinResultadosException(EstadoAntecedente estado) {
        super("No hay ciudadanos con estado «" + estado.getEtiqueta() + "». No se generó el reporte.");
    }
}
