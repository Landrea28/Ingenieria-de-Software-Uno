package co.edu.konrad.consultaciudadano.common.error;

import java.util.List;

/**
 * Formato uniforme de error de la API: código, mensaje y lista de detalles.
 */
public record ErrorResponse(CodigoError codigo, String mensaje, List<String> detalles) {
    public static ErrorResponse crear(CodigoError codigo, String mensaje, List<String> detalles) {
        return new ErrorResponse(codigo, mensaje, detalles);
    }
}
