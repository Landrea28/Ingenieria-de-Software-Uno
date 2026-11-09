package co.edu.konrad.consultaciudadano.ciudadano.dto;

import co.edu.konrad.consultaciudadano.ciudadano.entity.GrupoSanguineo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CiudadanoUpdateRequest(
        @NotBlank @Size(max = 100) @Pattern(regexp = "[\\p{L} ]+") String nombres,
        @NotBlank @Size(max = 100) @Pattern(regexp = "[\\p{L} ]+") String apellidos,
        @NotNull @PastOrPresent LocalDate fechaNacimiento,
        @NotBlank @Size(max = 150) String lugarNacimiento,
        @NotNull @PastOrPresent LocalDate fechaExpedicion,
        @NotBlank @Size(max = 150) String lugarExpedicion,
        @NotNull @Pattern(regexp = "[+-]") String rh,
        @NotNull GrupoSanguineo grupoSanguineo,
        @NotNull @Digits(integer = 1, fraction = 2) @DecimalMin("0.50") @DecimalMax("2.50") BigDecimal estatura) {}
