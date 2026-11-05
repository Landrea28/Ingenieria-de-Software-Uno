package co.edu.konrad.consultaciudadano.usuario.dto;

import co.edu.konrad.consultaciudadano.usuario.entity.Permiso;

public record PermisoResponse(Long id, String codigo, String nombre) {
    public static PermisoResponse desde(Permiso permiso) {
        return new PermisoResponse(permiso.getId(), permiso.getCodigo(), permiso.getNombre());
    }
}
