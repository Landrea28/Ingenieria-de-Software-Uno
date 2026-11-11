package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.LoginResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** SIMULADO: acepta cualquier credencial no vacía y entrega un token falso. Solo perfil "mock". */
@Service
@Profile("mock")
public class AuthApiClientMock implements AuthApiClient {

    private static final long OCHO_HORAS = 8 * 60 * 60;

    @Override
    public LoginResponse login(String identificacion, String contrasena, boolean recordarContrasena) {
        return new LoginResponse("token-simulado", "Bearer", OCHO_HORAS);
    }

    @Override
    public void logout() {
        // Nada que revocar en el mock.
    }
}
