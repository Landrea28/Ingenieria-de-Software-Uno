package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.PerfilDto;
import co.edu.konrad.consultaciudadano.web.dto.PermisoDto;
import java.util.List;

/**
 * Contrato del cliente de perfiles y permisos (/api/perfiles y /api/permisos, solo ADMINISTRADOR).
 * El catálogo de perfiles es fijo en el backend: no hay crear ni eliminar perfiles.
 * Todos los métodos pueden lanzar ApiNoAutorizadoException (401), ApiAccesoDenegadoException (403)
 * o ApiNoDisponibleException (red o 5xx).
 */
public interface PerfilApiClient {

    /** GET /api/perfiles. */
    List<PerfilDto> perfiles();

    /** GET /api/permisos: catálogo completo. */
    List<PermisoDto> permisos();

    /**
     * GET /api/perfiles/{id}/permisos.
     * @throws ApiNoEncontradoException si el perfil no existe (404)
     */
    List<PermisoDto> permisosDe(long perfilId);

    /**
     * PUT /api/perfiles/{id}/permisos: la lista reemplaza a la anterior.
     * @throws ApiConflictoException si se le quita SEGURIDAD_ADMINISTRAR al perfil ADMINISTRADOR (409)
     * @throws ApiNoEncontradoException si el perfil o algún permiso no existen (404)
     */
    List<PermisoDto> reemplazarPermisos(long perfilId, List<Long> permisosIds);
}
