package co.edu.konrad.consultaciudadano.personal.service;

import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;

/**
 * Contrato de los pasos de seguridad del registro: validar contra Defensa, generar y cifrar la contraseña y enviar el correo.
 */
public interface RegistroPersonalIntegracion {
    void validarContraDefensa(RegistroPersonalRequest solicitud);
    String generarContrasena();
    String hashearContrasena(String contrasena);
    void enviarCorreo(String correo, String contrasena);
}
