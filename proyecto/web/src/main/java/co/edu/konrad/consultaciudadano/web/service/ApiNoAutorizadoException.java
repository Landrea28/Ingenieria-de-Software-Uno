package co.edu.konrad.consultaciudadano.web.service;

/** HTTP 401: sin token, token vencido, revocado o credenciales inválidas. */
public class ApiNoAutorizadoException extends ApiException {

    public ApiNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
