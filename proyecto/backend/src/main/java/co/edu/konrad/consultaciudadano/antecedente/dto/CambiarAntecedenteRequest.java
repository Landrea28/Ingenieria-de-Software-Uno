package co.edu.konrad.consultaciudadano.antecedente.dto;

import co.edu.konrad.consultaciudadano.antecedente.entity.EstadoAntecedente;
import jakarta.validation.constraints.NotNull;

/**
 * Petición para establecer el estado de antecedentes de un ciudadano en el entorno de demostración.
 */
public record CambiarAntecedenteRequest(@NotNull EstadoAntecedente estado) {}
