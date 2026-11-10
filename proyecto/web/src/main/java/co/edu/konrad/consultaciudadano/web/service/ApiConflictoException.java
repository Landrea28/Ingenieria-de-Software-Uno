package co.edu.konrad.consultaciudadano.web.service;

/** HTTP 409: conflicto con el estado actual (p. ej. código DUPLICADO). */
public class ApiConflictoException extends ApiException {

    public ApiConflictoException(String mensaje) {
        super(mensaje);
    }
}
