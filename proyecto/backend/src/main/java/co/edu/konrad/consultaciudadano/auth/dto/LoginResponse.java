package co.edu.konrad.consultaciudadano.auth.dto;

public record LoginResponse(String token, String tipo, Long expiraEnSegundos) {}
