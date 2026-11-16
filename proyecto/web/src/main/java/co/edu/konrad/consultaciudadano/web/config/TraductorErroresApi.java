package co.edu.konrad.consultaciudadano.web.config;

import co.edu.konrad.consultaciudadano.web.dto.ErrorApi;
import co.edu.konrad.consultaciudadano.web.service.ApiAccesoDenegadoException;
import co.edu.konrad.consultaciudadano.web.service.ApiConflictoException;
import co.edu.konrad.consultaciudadano.web.service.ApiException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoAutorizadoException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
import co.edu.konrad.consultaciudadano.web.service.ApiNoEncontradoException;
import co.edu.konrad.consultaciudadano.web.service.ApiValidacionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.springframework.http.client.ClientHttpResponse;

/** Convierte una respuesta de error de la API (4xx/5xx con ErrorResponse) en una excepción con nombre. */
public class TraductorErroresApi {

    private final ObjectMapper objectMapper;

    public TraductorErroresApi(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ApiException traducir(ClientHttpResponse response) throws IOException {
        int estado = response.getStatusCode().value();
        ErrorApi error = leer(response);
        String mensaje = error != null && error.mensaje() != null ? error.mensaje() : "Error HTTP " + estado;
        List<String> detalles = error != null ? error.detalles() : List.of();
        return switch (estado) {
            case 400 -> new ApiValidacionException(mensaje, detalles);
            case 401 -> new ApiNoAutorizadoException(mensaje);
            case 403 -> new ApiAccesoDenegadoException(mensaje);
            case 404 -> new ApiNoEncontradoException(mensaje, error != null ? error.codigo() : null);
            case 409 -> new ApiConflictoException(mensaje);
            // 5xx (incluye 503 INTEGRACION_PENDIENTE) y cualquier otro: el servidor no pudo atender.
            default -> new ApiNoDisponibleException();
        };
    }

    // Si el cuerpo no es el JSON esperado (p. ej. una página HTML de un proxy), se ignora.
    private ErrorApi leer(ClientHttpResponse response) {
        try {
            return objectMapper.readValue(response.getBody(), ErrorApi.class);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
