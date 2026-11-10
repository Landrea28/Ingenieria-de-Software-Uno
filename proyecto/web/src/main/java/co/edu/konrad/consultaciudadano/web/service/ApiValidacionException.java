package co.edu.konrad.consultaciudadano.web.service;

import java.util.List;

/** HTTP 400: el backend rechazó los datos (código VALIDACION). */
public class ApiValidacionException extends ApiException {

    public ApiValidacionException(String mensaje, List<String> detalles) {
        super(mensaje, detalles, null);
    }
}
