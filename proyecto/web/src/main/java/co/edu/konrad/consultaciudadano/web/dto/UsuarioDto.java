package co.edu.konrad.consultaciudadano.web.dto;

/**
 * Usuario tal como lo expone la API (esquema UsuarioResponse). Nunca trae la contraseña ni su hash.
 * perfil: código del perfil (ADMINISTRADOR o PERSONAL_FUERZA_PUBLICA); estado: ACTIVO o INACTIVO.
 */
public record UsuarioDto(
        String identificacion,
        String nombres,
        String apellidos,
        String correo,
        String perfil,
        String estado) {

    public static final String ACTIVO = "ACTIVO";
    public static final String INACTIVO = "INACTIVO";

    public boolean activo() {
        return ACTIVO.equals(estado);
    }
}
