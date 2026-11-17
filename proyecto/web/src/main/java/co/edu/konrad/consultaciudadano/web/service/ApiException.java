package co.edu.konrad.consultaciudadano.web.service;

import java.util.List;

/** Error al llamar a la API REST. Las subclases indican el tipo de error. */
public class ApiException extends RuntimeException {

    private final List<String> detalles;

    public ApiException(String mensaje, List<String> detalles, Throwable causa) {
        super(mensaje, causa);
        this.detalles = detalles == null ? List.of() : List.copyOf(detalles);
    }

    public ApiException(String mensaje) {
        this(mensaje, List.of(), null);
    }

    /** Errores de validación con el formato "campo: mensaje". */
    public List<String> getDetalles() {
        return detalles;
    }
}
