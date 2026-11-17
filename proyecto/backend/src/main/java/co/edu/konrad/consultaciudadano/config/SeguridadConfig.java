package co.edu.konrad.consultaciudadano.config;

import co.edu.konrad.consultaciudadano.auth.service.FiltroJwt;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.ErrorResponse;
import co.edu.konrad.consultaciudadano.common.security.AuditoriaSeguridad;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import java.time.Clock;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SeguridadConfig {
    private final FiltroJwt filtroJwt;
    private final ObjectMapper objectMapper;
    private final AuditoriaSeguridad auditoria;
    @Value("${app.dev-endpoints-enabled:false}")
    private boolean endpointsDesarrollo;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(frame -> frame.deny())
                        .cacheControl(Customizer.withDefaults()))
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers(request -> endpointsDesarrollo
                                && (request.getRequestURI().startsWith("/v3/api-docs/")
                                || request.getRequestURI().equals("/v3/api-docs")
                                || request.getRequestURI().startsWith("/swagger-ui/")
                                || request.getRequestURI().equals("/swagger-ui.html")
                                || request.getRequestURI().startsWith("/openapi/")
                                || request.getRequestURI().startsWith("/actuator/health")))
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/personal/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/catalogos/fuerzas-rangos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/antecedentes/*")
                        .hasAnyRole("PERSONAL_FUERZA_PUBLICA", "ADMINISTRADOR")
                        .requestMatchers("/api/antecedentes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/ciudadanos/**", "/api/reportes/**", "/api/usuarios/**",
                                "/api/perfiles/**", "/api/permisos/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll())
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    Clock securityClock() {
        return Clock.systemUTC();
    }

    @Bean
    FilterRegistrationBean<FiltroJwt> filtroJwtSoloSpringSecurity(FiltroJwt filtro) {
        var registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, error) -> escribirError(response, HttpStatus.UNAUTHORIZED,
                CodigoError.NO_AUTENTICADO, "No autenticado", () -> auditoria.noAutorizado(request));
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, error) -> escribirError(response, HttpStatus.FORBIDDEN,
                CodigoError.ACCESO_DENEGADO, "Acceso denegado", () -> auditoria.accesoDenegado(request));
    }

    private void escribirError(jakarta.servlet.http.HttpServletResponse response, HttpStatus status,
            CodigoError codigo, String mensaje, Runnable registroAuditoria) throws IOException {
        registroAuditoria.run();
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ErrorResponse.crear(codigo, mensaje, List.of()));
    }
}
