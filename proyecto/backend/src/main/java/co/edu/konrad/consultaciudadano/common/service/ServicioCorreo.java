package co.edu.konrad.consultaciudadano.common.service;

/**
 * Contrato para enviar al usuario sus credenciales temporales por correo.
 */
public interface ServicioCorreo {
    void enviarCredenciales(String correo, String contrasena);
}
