package co.edu.konrad.consultaciudadano.personal.dto;

/**
 * Respuesta del registro: solo un mensaje. La contraseña nunca viaja en la respuesta, únicamente por correo.
 */
public record RegistroPersonalResponse(String mensaje) {}
