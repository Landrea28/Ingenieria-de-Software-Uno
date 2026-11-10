package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.LoginResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Implementación real de /api/auth (seguridad de Julián). */
@Service
@Profile("api")
public class AuthApiClientRest implements AuthApiClient {

    private final RestClient api;

    public AuthApiClientRest(RestClient api) {
        this.api = api;
    }

    @Override
    public LoginResponse login(String identificacion, String contrasena, boolean recordarContrasena) {
        return api.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(identificacion, contrasena, recordarContrasena))
                .retrieve()
                .body(LoginResponse.class);
    }

    @Override
    public void logout() {
        api.post().uri("/api/auth/logout").retrieve().toBodilessEntity();
    }

    /** Cuerpo de POST /api/auth/login (esquema LoginRequest). */
    record LoginRequest(String identificacion, String contrasena, boolean recordarContrasena) {}
}
