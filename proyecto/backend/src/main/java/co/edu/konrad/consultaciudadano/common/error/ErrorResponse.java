package co.edu.konrad.consultaciudadano.common.error;

import java.util.List;

public record ErrorResponse(CodigoError codigo, String mensaje, List<String> detalles) {
    public static ErrorResponse crear(CodigoError codigo, String mensaje, List<String> detalles) {
        return new ErrorResponse(codigo, mensaje, detalles);
    }
}
