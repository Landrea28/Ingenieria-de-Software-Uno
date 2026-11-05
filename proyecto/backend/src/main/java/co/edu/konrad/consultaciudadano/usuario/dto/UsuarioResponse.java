package co.edu.konrad.consultaciudadano.usuario.dto;

import co.edu.konrad.consultaciudadano.usuario.entity.Usuario;

public record UsuarioResponse(
        String identificacion,
        String nombres,
        String apellidos,
        String correo,
        String perfil,
        EstadoUsuario estado) {
    public static UsuarioResponse desde(Usuario usuario) {
        var perfil = usuario.getPerfiles().stream().findFirst().orElse(null);
        return new UsuarioResponse(usuario.getIdentificacion(), usuario.getNombres(), usuario.getApellidos(),
                usuario.getCorreo(), perfil == null ? null : perfil.getCodigo(),
                usuario.isActivo() ? EstadoUsuario.ACTIVO : EstadoUsuario.INACTIVO);
    }
}
