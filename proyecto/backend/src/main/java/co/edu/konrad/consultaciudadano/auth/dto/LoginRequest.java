package co.edu.konrad.consultaciudadano.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank @Pattern(regexp = "[0-9]{6,10}") String identificacion,
        @NotBlank @Size(max = 128) String contrasena, @NotNull Boolean recordarContrasena) {}
