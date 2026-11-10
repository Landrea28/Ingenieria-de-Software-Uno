package co.edu.konrad.consultaciudadano.web.config;

import co.edu.konrad.consultaciudadano.web.service.ApiNoDisponibleException;
import co.edu.konrad.consultaciudadano.web.sesion.SesionAdmin;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP hacia el backend (solo con el perfil "api"). Los tiempos de espera vienen de
 * spring.http.client.* en application.yml.
 */
@Configuration
@Profile("api")
public class ApiRestConfig {

    @Bean
    RestClient apiRestClient(RestClient.Builder builder, ApiProperties api, SesionAdmin sesion,
            ObjectMapper objectMapper) {
        return configurar(builder, api.baseUrl(), sesion::getToken, objectMapper).build();
    }

    /**
     * Separado del @Bean para reutilizarlo en las pruebas con MockRestServiceServer.
     * - Agrega "Authorization: Bearer <token>" si hay sesión.
     * - Convierte errores de red (servidor apagado, tiempo agotado) en ApiNoDisponibleException.
     * - Convierte respuestas 4xx/5xx en excepciones con nombre (TraductorErroresApi).
     * - Usa el ObjectMapper de Spring Boot para el JSON: fechas como "1995-02-14" (formato del
     *   contrato), no como arreglo [1995,2,14].
     */
    public static RestClient.Builder configurar(RestClient.Builder builder, String baseUrl,
            Supplier<String> token, ObjectMapper objectMapper) {
        TraductorErroresApi traductor = new TraductorErroresApi(objectMapper);
        return builder
                .baseUrl(baseUrl)
                .messageConverters(convertidores -> {
                    convertidores.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
                    convertidores.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
                })
                .requestInterceptor((request, body, execution) -> {
                    String valor = token.get();
                    if (valor != null) {
                        request.getHeaders().setBearerAuth(valor);
                    }
                    try {
                        return execution.execute(request, body);
                    } catch (IOException e) {
                        throw new ApiNoDisponibleException(e);
                    }
                })
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw traductor.traducir(response);
                });
    }
}
