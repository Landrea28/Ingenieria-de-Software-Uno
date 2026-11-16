package co.edu.konrad.consultaciudadano.usuario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Lista de permisos que reemplaza a los que tiene asignados un perfil.
 */
public record ActualizarPermisosRequest(
        @NotNull @Size(max = 100) List<@NotNull @Positive Long> permisosIds) {}
