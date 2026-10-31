package co.edu.konrad.consultaciudadano.common.error;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(CodigoError codigo, String mensaje, List<String> detalles, OffsetDateTime fecha) {
    public static ErrorResponse crear(CodigoError codigo, String mensaje, List<String> detalles) {
        return new ErrorResponse(codigo, mensaje, detalles, OffsetDateTime.now());
    }
}
