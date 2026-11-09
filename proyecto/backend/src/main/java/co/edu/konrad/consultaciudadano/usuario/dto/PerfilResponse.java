package co.edu.konrad.consultaciudadano.usuario.dto;

import co.edu.konrad.consultaciudadano.usuario.entity.Perfil;

public record PerfilResponse(Long id, String codigo, String nombre) {
    public static PerfilResponse desde(Perfil perfil) {
        return new PerfilResponse(perfil.getId(), perfil.getCodigo(), perfil.getNombre());
    }
}
