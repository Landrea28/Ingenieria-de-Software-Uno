package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.CiudadanoDto;
import java.util.List;
import java.util.Optional;

/**
 * Contrato del cliente de la API de ciudadanos (/api/ciudadanos).
 * Los controladores dependen solo de esta interfaz; hoy la implementa un mock
 * y más adelante una versión que llama al backend con RestClient.
 */
public interface CiudadanoApiClient {

    /** GET /api/ciudadanos?busqueda= */
    List<CiudadanoDto> listar(String busqueda);

    /** GET /api/ciudadanos/{identificacion} */
    Optional<CiudadanoDto> buscar(String identificacion);

    /** true si los datos son simulados (se muestra un aviso en pantalla). */
    boolean esSimulado();
}
