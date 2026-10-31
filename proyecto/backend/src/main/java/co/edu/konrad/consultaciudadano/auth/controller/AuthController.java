package co.edu.konrad.consultaciudadano.auth.controller;

import co.edu.konrad.consultaciudadano.auth.dto.LoginRequest;
import co.edu.konrad.consultaciudadano.auth.dto.LoginResponse;
import co.edu.konrad.consultaciudadano.auth.service.AutenticacionIntegracion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AutenticacionIntegracion integracion;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return integracion.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String autorizacion) {
        integracion.logout(autorizacion);
        return ResponseEntity.noContent().build();
    }
}
