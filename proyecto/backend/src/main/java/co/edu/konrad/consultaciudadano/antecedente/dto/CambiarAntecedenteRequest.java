package co.edu.konrad.consultaciudadano.antecedente.dto;

import co.edu.konrad.consultaciudadano.antecedente.entity.EstadoAntecedente;
import jakarta.validation.constraints.NotNull;

public record CambiarAntecedenteRequest(@NotNull EstadoAntecedente estado) {}
