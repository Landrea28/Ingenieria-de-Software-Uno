package co.edu.konrad.consultaciudadano.web.service;

import co.edu.konrad.consultaciudadano.web.dto.CiudadanoDto;
import co.edu.konrad.consultaciudadano.web.dto.PaginaCiudadanos;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Implementación real: llama a /api/ciudadanos del backend (contrato OpenAPI). */
@Service
@Profile("api")
public class CiudadanoApiClientRest implements CiudadanoApiClient {

    private final RestClient api;

    public CiudadanoApiClientRest(RestClient api) {
        this.api = api;
    }

    @Override
    public PaginaCiudadanos listar(String busqueda, int pagina) {
        String texto = busqueda == null || busqueda.isBlank() ? null : busqueda.trim();
        return api.get()
                .uri(uri -> {
                    uri.path("/api/ciudadanos").queryParam("pagina", pagina).queryParam("tamano", TAMANO_PAGINA);
                    if (texto == null) {
                        return uri.build();
                    }
                    // Como variable {busqueda} para que caracteres como &, + o espacios se codifiquen.
                    return uri.queryParam("busqueda", "{busqueda}").build(texto);
                })
                .retrieve()
                .body(PaginaCiudadanos.class);
    }

    @Override
    public Optional<CiudadanoDto> buscar(String identificacion) {
        try {
            return Optional.ofNullable(api.get()
                    .uri("/api/ciudadanos/{identificacion}", identificacion)
                    .retrieve()
                    .body(CiudadanoDto.class));
        } catch (ApiNoEncontradoException | ApiValidacionException e) {
            // 404 = no existe; 400 = identificación con formato inválido: en ambos casos no hay ciudadano.
            return Optional.empty();
        }
    }

    @Override
    public CiudadanoDto registrar(CiudadanoDto ciudadano) {
        try {
            return api.post()
                    .uri("/api/ciudadanos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CiudadanoRequest.desde(ciudadano))
                    .retrieve()
                    .body(CiudadanoDto.class);
        } catch (ApiConflictoException e) {
            throw new CiudadanoDuplicadoException(ciudadano.identificacion());
        }
    }

    @Override
    public boolean esSimulado() {
        return false;
    }

    /** Cuerpo de POST /api/ciudadanos (esquema CiudadanoRequest): los 10 campos, sin id. */
    record CiudadanoRequest(String identificacion, String nombres, String apellidos, LocalDate fechaNacimiento,
            String lugarNacimiento, LocalDate fechaExpedicion, String lugarExpedicion, String rh,
            String grupoSanguineo, BigDecimal estatura) {

        static CiudadanoRequest desde(CiudadanoDto c) {
            return new CiudadanoRequest(c.identificacion(), c.nombres(), c.apellidos(), c.fechaNacimiento(),
                    c.lugarNacimiento(), c.fechaExpedicion(), c.lugarExpedicion(), c.rh(), c.grupoSanguineo(),
                    c.estatura());
        }
    }
}
