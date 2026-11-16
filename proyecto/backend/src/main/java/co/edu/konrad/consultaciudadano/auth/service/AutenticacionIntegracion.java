package co.edu.konrad.consultaciudadano.auth.service;

import co.edu.konrad.consultaciudadano.auth.dto.LoginRequest;
import co.edu.konrad.consultaciudadano.auth.dto.LoginResponse;

/**
 * Contrato de la autenticación que usa el controlador: inicio y cierre de sesión.
 * Separa la capa HTTP de la implementación de seguridad.
 */
public interface AutenticacionIntegracion {
    LoginResponse login(LoginRequest request);
    void logout(String autorizacion);
}
