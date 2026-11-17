package co.edu.konrad.consultaciudadano.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ciudadano tal como lo expone la API (mismos nombres que CiudadanoResponse del backend
 * y que las columnas de la tabla ciudadano). rh: "+" o "-"; grupoSanguineo: A, B, AB u O;
 * estatura en metros (0.50 - 2.50).
 */
public record CiudadanoDto(
        Long id,
        String identificacion,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String lugarNacimiento,
        LocalDate fechaExpedicion,
        String lugarExpedicion,
        String rh,
        String grupoSanguineo,
        BigDecimal estatura) {}
