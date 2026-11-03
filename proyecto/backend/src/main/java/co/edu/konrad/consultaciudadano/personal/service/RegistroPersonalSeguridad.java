package co.edu.konrad.consultaciudadano.personal.service;

import co.edu.konrad.consultaciudadano.auth.service.GeneradorContrasena;
import co.edu.konrad.consultaciudadano.auth.service.ValidadorContrasena;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.NegocioException;
import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistroPersonalSeguridad implements RegistroPersonalIntegracion {
    private final ValidadorDefensa validadorDefensa;
    private final GeneradorContrasena generadorContrasena;
    private final ValidadorContrasena validadorContrasena;
    private final PasswordEncoder passwordEncoder;
    private final ServicioCorreo servicioCorreo;

    public RegistroPersonalSeguridad(ValidadorDefensa validadorDefensa, GeneradorContrasena generadorContrasena,
            ValidadorContrasena validadorContrasena, PasswordEncoder passwordEncoder, ServicioCorreo servicioCorreo) {
        this.validadorDefensa = validadorDefensa;
        this.generadorContrasena = generadorContrasena;
        this.validadorContrasena = validadorContrasena;
        this.passwordEncoder = passwordEncoder;
        this.servicioCorreo = servicioCorreo;
    }

    @Override
    public void validarContraDefensa(RegistroPersonalRequest solicitud) {
        switch (validadorDefensa.validar(solicitud)) {
            case ACEPTADO -> { }
            case RECHAZADO -> throw new NegocioException(HttpStatus.BAD_REQUEST, CodigoError.VALIDACION,
                    "Los datos no pudieron ser validados");
            case NO_DISPONIBLE -> throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                    CodigoError.INTEGRACION_PENDIENTE, "El validador SIMULADO no está disponible");
        }
    }

    @Override
    public String generarContrasena() {
        var contrasena = generadorContrasena.generar();
        if (!validadorContrasena.esValida(contrasena)) {
            throw new IllegalStateException("El generador produjo una contraseña inválida");
        }
        return contrasena;
    }

    @Override
    public String hashearContrasena(String contrasena) {
        return passwordEncoder.encode(contrasena);
    }

    @Override
    public void enviarCorreo(String correo, String contrasena) {
        servicioCorreo.enviarCredenciales(correo, contrasena);
    }
}
