package co.edu.konrad.consultaciudadano.web.dto;

/** Permiso tal como lo expone la API (esquema PermisoResponse). */
public record PermisoDto(Long id, String codigo, String nombre) {

    /** Permiso que el perfil ADMINISTRADOR no puede perder (regla del backend). */
    public static final String SEGURIDAD_ADMINISTRAR = "SEGURIDAD_ADMINISTRAR";

    /**
     * Opción del sistema a la que pertenece el permiso. La API no expone las opciones, pero el código
     * de cada permiso empieza por el de su opción (CIUDADANOS_LEER → CIUDADANOS).
     */
    public String opcion() {
        int separador = codigo == null ? -1 : codigo.indexOf('_');
        return separador > 0 ? codigo.substring(0, separador) : "OTROS";
    }
}
