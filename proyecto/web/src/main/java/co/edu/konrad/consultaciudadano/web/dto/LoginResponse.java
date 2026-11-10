package co.edu.konrad.consultaciudadano.web.dto;

/** Respuesta de POST /api/auth/login (mismos nombres que el backend). */
public record LoginResponse(String token, String tipo, Long expiraEnSegundos) {}
