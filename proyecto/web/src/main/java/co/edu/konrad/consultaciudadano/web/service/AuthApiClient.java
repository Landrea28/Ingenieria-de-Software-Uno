package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.LoginResponse;

/** Contrato del cliente de autenticación (/api/auth). */
public interface AuthApiClient {

    /**
     * POST /api/auth/login.
     * @throws ApiNoAutorizadoException credenciales inválidas o cuenta bloqueada (401)
     */
    LoginResponse login(String identificacion, String contrasena, boolean recordarContrasena);

    /** POST /api/auth/logout: revoca el token de la sesión actual. */
    void logout();
}
