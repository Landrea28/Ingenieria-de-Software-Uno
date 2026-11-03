package co.edu.konrad.consultaciudadano.personal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroPersonalRequest(
        @NotBlank @Pattern(regexp = "[0-9]{6,10}") String identificacion,
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank String fuerzaCodigo,
        @NotNull Long rangoId,
        @NotBlank @Size(max = 40) String idFuerza,
        @NotBlank @Email @Size(max = 200) String correo) {}
