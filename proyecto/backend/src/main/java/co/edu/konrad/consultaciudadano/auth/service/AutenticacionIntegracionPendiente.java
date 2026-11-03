package co.edu.konrad.consultaciudadano.auth.service;

import co.edu.konrad.consultaciudadano.auth.dto.LoginRequest;
import co.edu.konrad.consultaciudadano.auth.dto.LoginResponse;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import org.springframework.http.HttpStatus;

public class AutenticacionIntegracionPendiente implements AutenticacionIntegracion {
    private NegocioException pendiente() {
        return new NegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                CodigoError.INTEGRACION_PENDIENTE, "Autenticacion pendiente de integracion con seguridad");
    }
    public LoginResponse login(LoginRequest request) { throw pendiente(); }
    public void logout(String autorizacion) { throw pendiente(); }
}
