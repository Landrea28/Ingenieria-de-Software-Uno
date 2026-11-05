package co.edu.konrad.consultaciudadano.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(
        @NotBlank @Pattern(regexp = "[0-9]{6,10}") String identificacion,
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Email @Size(max = 200) String correo,
        @NotNull @Positive Long perfilId) {}
