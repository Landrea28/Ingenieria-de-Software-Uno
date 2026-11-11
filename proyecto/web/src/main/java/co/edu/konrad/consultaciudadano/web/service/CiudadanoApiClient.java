package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.CiudadanoDto;
import co.edu.konrad.consultaciudadano.web.dto.PaginaCiudadanos;
import java.util.Optional;

/**
 * Contrato del cliente de la API de ciudadanos (/api/ciudadanos).
 * Los controladores dependen solo de esta interfaz: con el perfil "api" la implementa
 * CiudadanoApiClientRest (backend real) y con "mock", CiudadanoApiClientMock.
 * Todos los métodos pueden lanzar ApiNoAutorizadoException (401), ApiAccesoDenegadoException (403)
 * o ApiNoDisponibleException (red o 5xx).
 */
public interface CiudadanoApiClient {

    int TAMANO_PAGINA = 20;

    /** GET /api/ciudadanos?busqueda=&pagina=&tamano= (pagina base 0). */
    PaginaCiudadanos listar(String busqueda, int pagina);

    /** GET /api/ciudadanos/{identificacion}; vacío si no existe. */
    Optional<CiudadanoDto> buscar(String identificacion);

    /**
     * POST /api/ciudadanos. Devuelve el ciudadano creado (con id).
     * @throws CiudadanoDuplicadoException si la identificación ya existe (409)
     * @throws ApiValidacionException si el backend rechaza los datos (400)
     */
    CiudadanoDto registrar(CiudadanoDto ciudadano);

    /**
     * PUT /api/ciudadanos/{identificacion}. La identificación se toma de la ruta y no cambia;
     * se envían los otros 9 campos. Devuelve el ciudadano actualizado.
     * @throws ApiNoEncontradoException si el ciudadano no existe (404)
     * @throws ApiValidacionException si el backend rechaza los datos (400)
     */
    CiudadanoDto actualizar(String identificacion, CiudadanoDto datos);

    /** true si los datos son simulados (se muestra un aviso en pantalla). */
    boolean esSimulado();
}
