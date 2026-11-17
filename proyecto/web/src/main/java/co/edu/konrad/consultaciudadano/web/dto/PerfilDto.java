package co.edu.konrad.consultaciudadano.web.dto;

/** Perfil tal como lo expone la API (esquema PerfilResponse). El catálogo de perfiles es fijo. */
public record PerfilDto(Long id, String codigo, String nombre) {

    public static final String ADMINISTRADOR = "ADMINISTRADOR";

    public boolean administrador() {
        return ADMINISTRADOR.equals(codigo);
    }
}
