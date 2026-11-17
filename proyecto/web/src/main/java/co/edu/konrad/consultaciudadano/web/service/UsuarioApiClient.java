package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.PaginaUsuarios;
import co.edu.konrad.consultaciudadano.web.dto.UsuarioDatos;
import co.edu.konrad.consultaciudadano.web.dto.UsuarioDto;
import java.util.Optional;

/**
 * Contrato del cliente de la API de usuarios (/api/usuarios, solo ADMINISTRADOR).
 * Con el perfil "api" lo implementa UsuarioApiClientRest y con "mock", UsuarioApiClientMock.
 * No existe eliminar: un usuario se desactiva. Todos los métodos pueden lanzar
 * ApiNoAutorizadoException (401), ApiAccesoDenegadoException (403) o ApiNoDisponibleException (red o 5xx).
 */
public interface UsuarioApiClient {

    int TAMANO_PAGINA = 20;

    /** GET /api/usuarios?estado=&perfil=&page=&size= (page base 0). Los filtros nulos no se envían. */
    PaginaUsuarios listar(String estado, String perfil, int pagina);

    /** GET /api/usuarios/{identificacion}; vacío si no existe. */
    Optional<UsuarioDto> buscar(String identificacion);

    /**
     * POST /api/usuarios. El backend genera la contraseña temporal y la envía por correo; nunca se devuelve.
     * @throws ApiConflictoException si la identificación o el correo ya existen (409 DUPLICADO)
     * @throws ApiValidacionException si el backend rechaza los datos (400)
     * @throws ApiNoEncontradoException si el perfil no existe (404)
     */
    UsuarioDto crear(UsuarioDatos datos);

    /**
     * PUT /api/usuarios/{identificacion}: datos, perfil y estado.
     * @throws ApiConflictoException 409: correo duplicado, autodesactivación o último administrador activo
     * @throws ApiNoEncontradoException si el usuario o el perfil no existen (404)
     * @throws ApiValidacionException si el backend rechaza los datos (400)
     */
    UsuarioDto actualizar(String identificacion, UsuarioDatos datos);
}
