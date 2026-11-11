package co.edu.konrad.consultaciudadano.web.service;

/** HTTP 403: el usuario está autenticado pero no tiene el rol requerido (ADMINISTRADOR). */
public class ApiAccesoDenegadoException extends ApiException {

    public ApiAccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
