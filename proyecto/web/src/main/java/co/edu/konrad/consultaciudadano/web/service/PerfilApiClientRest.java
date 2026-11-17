package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.PerfilDto;
import co.edu.konrad.consultaciudadano.web.dto.PermisoDto;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Implementación real: llama a /api/perfiles y /api/permisos del backend (contrato OpenAPI). */
@Service
@Profile("api")
public class PerfilApiClientRest implements PerfilApiClient {

    private static final ParameterizedTypeReference<List<PerfilDto>> PERFILES = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<PermisoDto>> PERMISOS = new ParameterizedTypeReference<>() {};

    private final RestClient api;

    public PerfilApiClientRest(RestClient api) {
        this.api = api;
    }

    @Override
    public List<PerfilDto> perfiles() {
        return api.get().uri("/api/perfiles").retrieve().body(PERFILES);
    }

    @Override
    public List<PermisoDto> permisos() {
        return api.get().uri("/api/permisos").retrieve().body(PERMISOS);
    }

    @Override
    public List<PermisoDto> permisosDe(long perfilId) {
        return api.get().uri("/api/perfiles/{id}/permisos", perfilId).retrieve().body(PERMISOS);
    }

    @Override
    public List<PermisoDto> reemplazarPermisos(long perfilId, List<Long> permisosIds) {
        return api.put()
                .uri("/api/perfiles/{id}/permisos", perfilId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ActualizarPermisosRequest(permisosIds))
                .retrieve()
                .body(PERMISOS);
    }

    /** Cuerpo de PUT /api/perfiles/{id}/permisos. */
    record ActualizarPermisosRequest(List<Long> permisosIds) {}
}
