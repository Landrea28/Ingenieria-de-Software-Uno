package co.edu.konrad.consultaciudadano.auth.dto;

/**
 * Respuesta del inicio de sesión: token, tipo (Bearer) y segundos de vigencia. Nunca incluye la contraseña.
 */
public record LoginResponse(String token, String tipo, Long expiraEnSegundos) {}
