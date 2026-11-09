package co.edu.konrad.consultaciudadano.auth.service;

import co.edu.konrad.consultaciudadano.auth.dto.LoginRequest;
import co.edu.konrad.consultaciudadano.auth.dto.LoginResponse;

public interface AutenticacionIntegracion {
    LoginResponse login(LoginRequest request);
    void logout(String autorizacion);
}
