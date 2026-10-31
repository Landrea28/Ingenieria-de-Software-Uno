package co.edu.konrad.consultaciudadano.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(@NotBlank @Pattern(regexp = "[0-9]{6,10}") String identificacion,
        @NotBlank String contrasena, @NotNull Boolean recordarContrasena) {}
