package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.PaginaUsuarios;
import co.edu.konrad.consultaciudadano.web.dto.UsuarioDatos;
import co.edu.konrad.consultaciudadano.web.dto.UsuarioDto;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Implementación real: llama a /api/usuarios del backend (contrato OpenAPI). */
@Service
@Profile("api")
public class UsuarioApiClientRest implements UsuarioApiClient {

    private final RestClient api;

    public UsuarioApiClientRest(RestClient api) {
        this.api = api;
    }

    @Override
    public PaginaUsuarios listar(String estado, String perfil, int pagina) {
        return api.get()
                .uri(uri -> {
                    uri.path("/api/usuarios").queryParam("page", pagina).queryParam("size", TAMANO_PAGINA);
                    if (estado != null) {
                        uri.queryParam("estado", estado);
                    }
                    if (perfil != null) {
                        uri.queryParam("perfil", perfil);
                    }
                    return uri.build();
                })
                .retrieve()
                .body(PaginaUsuarios.class);
    }

    @Override
    public Optional<UsuarioDto> buscar(String identificacion) {
        try {
            return Optional.ofNullable(api.get()
                    .uri("/api/usuarios/{identificacion}", identificacion)
                    .retrieve()
                    .body(UsuarioDto.class));
        } catch (ApiNoEncontradoException | ApiValidacionException e) {
            // 404 = no existe; 400 = identificación con formato inválido: en ambos casos no hay usuario.
            return Optional.empty();
        }
    }

    @Override
    public UsuarioDto crear(UsuarioDatos d) {
        return api.post()
                .uri("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CrearUsuarioRequest(d.identificacion(), d.nombres(), d.apellidos(), d.correo(), d.perfilId()))
                .retrieve()
                .body(UsuarioDto.class);
    }

    @Override
    public UsuarioDto actualizar(String identificacion, UsuarioDatos d) {
        return api.put()
                .uri("/api/usuarios/{identificacion}", identificacion)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ActualizarUsuarioRequest(d.nombres(), d.apellidos(), d.correo(), d.perfilId(), d.estado()))
                .retrieve()
                .body(UsuarioDto.class);
    }

    /** Cuerpo de POST /api/usuarios: sin estado (nace ACTIVO) ni contraseña (la genera el backend). */
    record CrearUsuarioRequest(String identificacion, String nombres, String apellidos, String correo, Long perfilId) {}

    /** Cuerpo de PUT /api/usuarios/{identificacion}: sin identificación (va en la ruta). */
    record ActualizarUsuarioRequest(String nombres, String apellidos, String correo, Long perfilId, String estado) {}
}
