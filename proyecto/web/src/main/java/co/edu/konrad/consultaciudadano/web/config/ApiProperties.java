package co.edu.konrad.consultaciudadano.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuración de la API REST del backend (propiedad api.base-url / variable API_BASE_URL). */
@ConfigurationProperties(prefix = "api")
public record ApiProperties(String baseUrl) {}
