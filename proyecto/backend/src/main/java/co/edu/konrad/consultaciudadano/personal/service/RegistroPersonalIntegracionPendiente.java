package co.edu.konrad.consultaciudadano.personal.service;

import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;
import org.springframework.http.HttpStatus;

public class RegistroPersonalIntegracionPendiente implements RegistroPersonalIntegracion {
    private NegocioException pendiente() {
        return new NegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                CodigoError.INTEGRACION_PENDIENTE, "Registro pendiente de integracion con seguridad");
    }

    public void validarContraDefensa(RegistroPersonalRequest solicitud) { throw pendiente(); }
    public String generarContrasena() { throw pendiente(); }
    public String hashearContrasena(String contrasena) { throw pendiente(); }
    public void enviarCorreo(String correo, String contrasena) { throw pendiente(); }
}
