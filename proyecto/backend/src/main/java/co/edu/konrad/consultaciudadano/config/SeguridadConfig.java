package co.edu.konrad.consultaciudadano.config;

import co.edu.konrad.consultaciudadano.auth.service.FiltroJwt;
import co.edu.konrad.consultaciudadano.common.error.CodigoError;
import co.edu.konrad.consultaciudadano.common.error.ErrorResponse;
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

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SeguridadConfig {
    private final FiltroJwt filtroJwt;
    private final ObjectMapper objectMapper;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/personal/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/catalogos/fuerzas-rangos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/antecedentes/*")
                        .hasAnyRole("PERSONAL_FUERZA_PUBLICA", "ADMINISTRADOR")
                        .requestMatchers("/api/antecedentes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/ciudadanos/**", "/api/reportes/**", "/api/usuarios/**", "/api/perfiles/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
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
    FilterRegistrationBean<FiltroJwt> filtroJwtSoloSpringSecurity(FiltroJwt filtro) {
        var registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, error) -> escribirError(response, HttpStatus.UNAUTHORIZED,
                CodigoError.NO_AUTENTICADO, "No autenticado");
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, error) -> escribirError(response, HttpStatus.FORBIDDEN,
                CodigoError.ACCESO_DENEGADO, "Acceso denegado");
    }

    private void escribirError(jakarta.servlet.http.HttpServletResponse response, HttpStatus status,
            CodigoError codigo, String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ErrorResponse.crear(codigo, mensaje, List.of()));
    }
}
