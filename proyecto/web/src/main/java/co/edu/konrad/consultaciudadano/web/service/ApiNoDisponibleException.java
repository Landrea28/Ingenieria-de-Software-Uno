package co.edu.konrad.consultaciudadano.web.service;

/** La API no respondió (red, tiempo de espera) o respondió con un error 5xx. */
public class ApiNoDisponibleException extends ApiException {

    public static final String MENSAJE = "No fue posible conectar con el servidor. Intente de nuevo en unos minutos.";

    public ApiNoDisponibleException(Throwable causa) {
        super(MENSAJE, null, causa);
    }

    public ApiNoDisponibleException() {
        super(MENSAJE);
    }
}
