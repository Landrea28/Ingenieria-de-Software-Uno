package co.edu.konrad.consultaciudadano.web.service;

/** HTTP 404: el recurso no existe. */
public class ApiNoEncontradoException extends ApiException {

    public ApiNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
