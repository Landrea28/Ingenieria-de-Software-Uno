package co.edu.konrad.consultaciudadano.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción de regla de negocio que lleva el código HTTP y el {@link CodigoError} con que debe responder la API.
 */
@Getter
public class NegocioException extends RuntimeException {
    private final HttpStatus estadoHttp;
    private final CodigoError codigo;

    public NegocioException(HttpStatus estadoHttp, CodigoError codigo, String mensaje) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
        this.codigo = codigo;
    }
}
