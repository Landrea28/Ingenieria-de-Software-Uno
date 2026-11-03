package co.edu.konrad.consultaciudadano.personal.service;

import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;

public interface RegistroPersonalIntegracion {
    void validarContraDefensa(RegistroPersonalRequest solicitud);
    String generarContrasena();
    String hashearContrasena(String contrasena);
    void enviarCorreo(String correo, String contrasena);
}
