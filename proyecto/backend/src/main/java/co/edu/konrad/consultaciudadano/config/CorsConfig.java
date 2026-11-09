package co.edu.konrad.consultaciudadano.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String[] origins;

    public CorsConfig(@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200}") String origins) {
        this.origins = java.util.Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toArray(String[]::new);
        if (this.origins.length == 0 || java.util.Arrays.stream(this.origins).anyMatch(origen -> origen.contains("*"))) {
            throw new IllegalArgumentException("CORS_ALLOWED_ORIGINS debe contener origenes explicitos; no se permite '*'");
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .maxAge(3600);
    }
}
